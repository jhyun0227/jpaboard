package practice.jpaboard.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import practice.jpaboard.exception.member.MemberError;
import practice.jpaboard.exception.member.MemberException;
import practice.jpaboard.member.dto.MemberLoginDto;
import practice.jpaboard.member.repository.MemberRepository;
import practice.jpaboard.security.SecurityProperties;
import practice.jpaboard.security.auth.UserDetailsImpl;
import practice.jpaboard.security.jwt.JwtTokenProvider;
import practice.jpaboard.security.jwt.TokenDto;

import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MemberLoginService {

    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final RedisService redisService;

    /**
     * 로그인한 사용자에게 토큰을 발급하는 메서드
     * 1. Refresh Token을 Redis에 저장한다.
     */
    public TokenDto login(MemberLoginDto memberLoginDto) {
        //우선 존재하는 ID인지 확인
        memberRepository.findByMemberLoginId(memberLoginDto.getMemberLoginId())
                .orElseThrow(() -> new MemberException(MemberError.NOT_EXIST_MEMBER));

        //다음 비밀번호 확인, 인증되면 토큰 발급까지
        try {
            UsernamePasswordAuthenticationToken authenticationToken
                    = new UsernamePasswordAuthenticationToken(memberLoginDto.getMemberLoginId(), memberLoginDto.getMemberPassword());

            //UserDetailsService.loadUserByUsername() 호출
            //인증의 역할만 한다.
            Authentication authenticate = authenticationManager.authenticate(authenticationToken);

            String memberLoginId = authenticate.getName();
            String authorities = getAuthorities(authenticate);

            return generateToken(SecurityProperties.SERVER, memberLoginId, authorities);
        } catch (BadCredentialsException e) {
            throw new MemberException(MemberError.INVALID_PASSWORD);
        }
    }

    /**
     * 로그아웃 메서드
     * 1. Redis에서 Refresh Token 삭제
     * 2. Redis에 AccessToken을 key 값으로 'logout'을 value 값으로 등록 만료시간도 함께 저장
     */
    public void logout(UserDetailsImpl userDetailsImpl, String accessToken, String refreshToken) {
        //1.SecurityContextHolder에서 memberLoginId 조회
        String memberLoginId = userDetailsImpl.getUsername();

        //2. redis에서 Refresh Token 삭제
        String refreshTokenInRedis
                = redisService.getValues("RT(" + SecurityProperties.SERVER + "):" + memberLoginId);

        if (StringUtils.hasText(refreshTokenInRedis) && refreshToken.equals(refreshTokenInRedis)) {
            redisService.deleteValues("RT(" + SecurityProperties.SERVER + "):" + memberLoginId);
        }

        //3. redis에 Access Token 저장 with 만료시간
        //Redis 만료시간 = 토큰 만료까지 남은 시간 - 현재 시간
        String resolveAccessToken = resolveAccessToken(accessToken);
        long expiration = jwtTokenProvider.getTokenExpirationTime(resolveAccessToken) - new Date().getTime();

        log.info("expiration = {}",  expiration);
        log.info("resolveAccessToken.length = {}", refreshToken.length());

        redisService.setValuesWithTimeout(resolveAccessToken,
                "logout",
                expiration);
    }

    /**
     * Token을 발급하는 메서드
     */
    public TokenDto generateToken(String provider, String memberLoginId, String authority) {
        //RefreshToken이 Redis에 있을 경우
        //다른 기기에서 로그인하면..?
        if (redisService.getValues("RT(" + provider + "):" + memberLoginId) != null) {
            redisService.deleteValues("RT(" + provider + "):" + memberLoginId);
        }

        //AccessToken, RefreshToken 발급
        TokenDto tokenDto =
                new TokenDto(jwtTokenProvider.createAccessToken(memberLoginId, authority),
                        jwtTokenProvider.createRefreshToken());

        //Redis에 RefreshToken 저장
        saveRefreshToken(provider, memberLoginId, tokenDto.getRefreshToken());
        return tokenDto;
    }

    /**
     * 토큰을 재발급 하는 메서드
     * 1. Access Token이 만료되고 Refresh Token은 만료가 안된 경우 -> Access Token만 재발급
     * 2. 둘다 만료가 된 경우 -> 로그인 유도
     */
    public String reissueToken(String accessToken, String refreshToken) {
        //1. Refresh Token 만료 여부 확인
        String resolveAccessToken = resolveAccessToken(accessToken);
        Authentication authentication = jwtTokenProvider.getAuthentication(resolveAccessToken);
        UserDetailsImpl userDetailsImpl = (UserDetailsImpl) authentication.getPrincipal();
        String memberLoginId = userDetailsImpl.getUsername();

        String refreshTokenInRedis =
                redisService.getValues("RT(" + SecurityProperties.SERVER + "):" + memberLoginId);

        //2. 두개의 경우는 null을 반환해서 login을 유도한다.
        if (refreshTokenInRedis == null) {
            //토큰이 만료
            return null;
        }

        if (!jwtTokenProvider.validateRefreshToken(refreshToken) || !refreshTokenInRedis.equals(refreshToken)) {
            //요청된 RefreshToken의 유효성검사와 Redis내부의 refreshToken과 요청된 refreshToken 같은지 비교
            //탈취의 가능성 존재
            redisService.deleteValues("RT(" + SecurityProperties.SERVER + "):" + memberLoginId);
            return null;
        }

        String authorities = getAuthorities(authentication);

        //토큰 재발급 및 Redis 업데이트
        return jwtTokenProvider.createAccessToken(memberLoginId, authorities);
    }

    /**
     * RefreshToken를 저장하는 메서드
     */
    private void saveRefreshToken(String provider, String memberLoginId, String refreshToken) {
        redisService.setValuesWithTimeout("RT(" + provider + "):" + memberLoginId, //key
                refreshToken, //value
                jwtTokenProvider.getTokenExpirationTime(refreshToken));
    }

    /**
     * AccessToken으로 부터 Authentication.principal(UserDetails)을 추출하고
     * UserDetails.getName()을 호출해서 username(memberLoginId) 추출하는 메서드
     */
    public String getUserDetailsName(String accessToken) {
        return jwtTokenProvider.getAuthentication(accessToken).getName();
    }

    /**
     * AccessToken이 만료일자만 초과한 유효한 토큰인지 검사
     */
    /*
    public boolean accessTokenValidate(String accessToken) {
        String resolveAccessToken = resolveAccessToken(accessToken);
        return jwtTokenProvider.validateAccessTokenOnlyExpired(resolveAccessToken);
    }
    */

    /**
     * AccessToken에서 Bearer 제거후 토큰만 추출하는 메서드
     */
    public String resolveAccessToken(String accessToken) {
        if (accessToken != null && accessToken.startsWith("Bearer ")) {
            return accessToken.substring(7);
        }

        return null;
    }

    /**
     * Authentication 객체에서 권한을 가져오는 메서드
     * 이건 솔직히 왜필요한지 모르겠네..
     * 어차피 Filter에서는 memberId만 쓰는데 말이야... 나중에 지워도 될듯
     */
    public String getAuthorities(Authentication authentication) {
        return authentication.getAuthorities().iterator().next().getAuthority();
    }
}
