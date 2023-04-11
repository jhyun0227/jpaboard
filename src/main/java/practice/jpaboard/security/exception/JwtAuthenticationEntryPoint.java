package practice.jpaboard.security.exception;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setCharacterEncoding("UTF-8");
        response.sendError(401, "잘못된 접근입니다. 로그인을 해주세요.");

        //내가 만든 RuntimeException을 발생시키려했지만 좋지 않은 방법
//        throw new SecurityException(SecurityError.JWT_AUTHENTICATION_ENTRY_POINT);
    }
}
