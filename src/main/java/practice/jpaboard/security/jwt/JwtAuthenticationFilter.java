package practice.jpaboard.security.jwt;

import io.jsonwebtoken.IncorrectClaimException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

        try {
            if (accessToken != null && jwtTokenProvider.validateAccessToken(accessToken)) {

                Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("JwtAuthentication.doFilterInternal - Save Authentication In SecurityContext");

            }
        } catch (IncorrectClaimException e) {
            SecurityContextHolder.clearContext();
            response.sendError(403, "유효하지 않은 토큰입니다.");
        } catch (UsernameNotFoundException e) {
            SecurityContextHolder.clearContext();
            response.sendError(403, "존재하지 않는 회원입니다.");
        }

        filterChain.doFilter(request, response);
    }

    /**
     * header에서 Token을 가져오는 메서드
     */
    private String resolveToken(HttpServletRequest httpServletRequest) {
        String bearerToken = httpServletRequest.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
}
