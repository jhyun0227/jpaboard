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
import practice.jpaboard.member.dto.MemberLoginDto;
import practice.jpaboard.security.jwt.JwtTokenProvider;
import practice.jpaboard.security.jwt.TokenDto;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MemberLoginService {

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    /**
     * 로그인한 사용자에게 토큰을 발급하는 메서드
     */
    @Transactional(readOnly = true)
    public TokenDto login(MemberLoginDto memberLoginDto) {
        UsernamePasswordAuthenticationToken authenticationToken
                = new UsernamePasswordAuthenticationToken(memberLoginDto.getMemberLoginId(), memberLoginDto.getMemberPassword());

        //UserDetailsService.loadUserByUsername() 호출
        //인증의 역할만 한다.
        Authentication authenticate = authenticationManager.authenticate(authenticationToken);

//        SecurityContextHolder.getContext().setAuthentication(authenticate);

        String memberLoginId = authenticate.getName();
        String authorities = getAuthorities(authenticate);

        log.info("authentication.getName() = {}", memberLoginId);
        log.info("authentication.getAuthorities = {}", authorities);

        return jwtTokenProvider.createToken(memberLoginId, authorities);
    }

    /**
     * Authentication 객체에서 권한을 가져오는 메서드
     */
    public String getAuthorities(Authentication authentication) {
        return authentication.getAuthorities().iterator().next().getAuthority();
    }
}
