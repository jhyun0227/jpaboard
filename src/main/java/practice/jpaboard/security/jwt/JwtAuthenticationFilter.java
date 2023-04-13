package practice.jpaboard.security.jwt;

import io.jsonwebtoken.IncorrectClaimException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;
import practice.jpaboard.security.SecurityProperties;

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
         */
        try {
            if (accessToken != null && jwtTokenProvider.validateAccessToken(accessToken)) {

                Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("JwtAuthentication.doFilterInternal - Save Authentication In SecurityContext");

            } else {
                //Invalidate SecurityContext
                SecurityContextHolder.clearContext();
            }
        } catch (IncorrectClaimException e) {
            SecurityContextHolder.clearContext();
            response.sendError(403, "유효하지 않은 토큰입니다.");
        } catch (UsernameNotFoundException e) {
            SecurityContextHolder.clearContext();
            response.sendError(403, e.getMessage());
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
