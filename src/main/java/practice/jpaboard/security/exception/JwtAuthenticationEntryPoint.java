package practice.jpaboard.security.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import practice.jpaboard.exception.security.SecurityError;
import practice.jpaboard.exception.security.SecurityException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.sendError(401, "잘못된 접근입니다.");
//        throw new SecurityException(SecurityError.JWT_AUTHENTICATION_ENTRY_POINT);
    }
}
