package practice.jpaboard.security.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        //Access Token 추출
        String accessToken = resolveToken(request);

        /**
         * 인증이 되어 SecurityContextHolder에 SecurityContext가 담기게 된다.
         * 만약 토큰 검증이 되지 않으면 SecurityContext에 authentication 객체가 없기 때문에
         * 엄밀히 말하면 SecurityContextHolder에 해당 요청에 대한 SecurityContext가 없기때문에
         * 해당 리소스에 접근하려하면 자동으로 반환한다.
         *
         */
        try {
            if (StringUtils.hasText(accessToken) && jwtTokenProvider.validateAccessToken(accessToken)) {

                Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("JwtAuthentication.doFilterInternal - Save Authentication In SecurityContext");
            } else {
                //Invalidate SecurityContext
                SecurityContextHolder.clearContext();
            }
        } catch (ExpiredJwtException e) { //만료된 토큰일 경우
            SecurityContextHolder.clearContext();
            request.setAttribute("exception", "ExpiredJwtException");
            request.setAttribute("message", "만료된 토큰입니다. redirect = " + "/reissue");
        } catch (JwtException e) { // 회원을 찾을 수 없을 경우
            SecurityContextHolder.clearContext();
            request.setAttribute("exception", "JwtException");
            request.setAttribute("message", "유효하지 않은 토큰입니다. 다시 로그인해주세요. redirect = " + "/login");
        } catch (UsernameNotFoundException e) {
            SecurityContextHolder.clearContext();
            request.setAttribute("exception", "UsernameNotFoundException");
            request.setAttribute("message", e.getMessage());
        } catch (AuthenticationException e) {
            SecurityContextHolder.clearContext();
            request.setAttribute("exception", "AuthenticationException");
            request.setAttribute("message", "비밀번호가 일치하지 않습니다. 비밀번호를 다시 확인해주세요. redirect = " + "/login");
        }

        filterChain.doFilter(request, response);
    }

    /**
     * header에서 Token을 가져오는 메서드
     */
    public String resolveToken(HttpServletRequest httpServletRequest) {
        String bearerToken = httpServletRequest.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
}
