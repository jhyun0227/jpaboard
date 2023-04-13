package practice.jpaboard.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import practice.jpaboard.member.dto.MemberLoginDto;
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

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final RedisService redisService;

    /**
     * 로그인한 사용자에게 토큰을 발급하는 메서드
     * 1. Refresh Token을 Redis에 저장한다.
     */
    public TokenDto login(MemberLoginDto memberLoginDto) {
        UsernamePasswordAuthenticationToken authenticationToken
                = new UsernamePasswordAuthenticationToken(memberLoginDto.getMemberLoginId(), memberLoginDto.getMemberPassword());

        //UserDetailsService.loadUserByUsername() 호출
        //인증의 역할만 한다.
        Authentication authenticate = authenticationManager.authenticate(authenticationToken);

        String memberLoginId = authenticate.getName();
        String authorities = getAuthorities(authenticate);

        return generateToken(SecurityProperties.SERVER, memberLoginId, authorities);
    }

    /**
     * 로그아웃 메서드
     * 1. Redis에서 Refresh Token 삭제
     * 2. Redis에 AccessToken을 key 값으로 'logout'을 value 값으로 등록 만료시간도 함께 저장
     */
    public void logout(String accessToken, String refreshToken) {
        //1. accessToken으로 authentication 조회
        String resolveAccessToken = resolveAccessToken(accessToken);
        Authentication authentication = jwtTokenProvider.getAuthentication(resolveAccessToken);
        UserDetailsImpl userDetailsImpl = (UserDetailsImpl) authentication.getPrincipal();
        String memberLoginId = userDetailsImpl.getUsername();

        //2. redis에서 Refresh Token 삭제
        String refreshTokenInRedis
                = redisService.getValues("RT(" + SecurityProperties.SERVER + "):" + memberLoginId);

        if (refreshTokenInRedis != null) {
            redisService.deleteValues("RT(" + SecurityProperties.SERVER + "):" + memberLoginId);
        }

        //3. redis에 Access Token 저장 with 만료시간
        //Redis 만료시간 = 토큰 만료까지 남은 시간 - 현재 시간
        long expiration = jwtTokenProvider.getTokenExpirationTime(resolveAccessToken) - new Date().getTime();
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

        //AccessToken, RefreshToken 발급 후 Redis에 RefreshToken 저장
        TokenDto tokenDto = jwtTokenProvider.createToken(memberLoginId, authority);
        saveRefreshToken(provider, memberLoginId, tokenDto.getRefreshToken());
        return tokenDto;
    }

    /**
     * AccessToken을 재발급하는 메서드
     * 1. Refresh Token이 유효한지 우선 조회
     * 2. Refresh Token이 유효하다면 Access Token만 재발급한다.
     * 3. Refresh Token이 유효하지 않거나 만료되었다면, 재로그인을 유도한다.
     */
    public TokenDto reissueToken(String accessToken, String refreshToken) {
        String resolveAccessToken = resolveAccessToken(accessToken);

        Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
        String memberLoginId = getUserDetailsName(resolveAccessToken);

        //조회한 usename으로 redis에 있는 refresh토큰을 조회
        String refreshTokenInRedis = redisService.getValues("RT(" + SecurityProperties.SERVER + "):" + memberLoginId);
        if (refreshTokenInRedis == null) {
            //Redis에 refreshToken이 없을 경우 재로그인을 요청하도록 한다.
            return null;
        }

        if (!jwtTokenProvider.validateRefreshToken(refreshToken) || !refreshTokenInRedis.equals(refreshToken)) {
            //요청된 RefreshToken의 유효성검사와 Redis내부의 refreshToken과 요청된 refreshToken 같은지 비교
            //탈취의 가능성 존재
            redisService.deleteValues("RT(" + SecurityProperties.SERVER + "):" + memberLoginId);
            return null;
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String authorities = getAuthorities(authentication);

        //토큰 재발급 및 Redis 업데이트
        redisService.deleteValues("RT(" + SecurityProperties.SERVER + "):" + memberLoginId); //기존 RefreshToken 삭제
        TokenDto tokenDto = jwtTokenProvider.createToken(memberLoginId, authorities);
        saveRefreshToken(SecurityProperties.SERVER, memberLoginId, tokenDto.getRefreshToken());
        return tokenDto;
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
    public boolean accessTokenValidate(String accessToken) {
        String resolveAccessToken = resolveAccessToken(accessToken);
        return jwtTokenProvider.validateAccessTokenOnlyExpired(resolveAccessToken);
    }

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
