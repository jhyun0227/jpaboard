package practice.jpaboard.security.exception;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setCharacterEncoding("UTF-8");
        response.sendError(403, "접근 권한이 없습니다.");

        //내가 만든 RuntimeException을 발생시키려했지만 좋지 않은 방법
//        throw new SecurityException(SecurityError.JWT_ACCESS_DENIED_HANDLER);
    }
}
