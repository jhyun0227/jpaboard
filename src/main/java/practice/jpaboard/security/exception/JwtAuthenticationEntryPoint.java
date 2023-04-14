package practice.jpaboard.security.exception;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import practice.jpaboard.util.dto.ResponseDto;
import practice.jpaboard.util.dto.StatusCode;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        String exception = (String) request.getAttribute("exception");
        String message = (String) request.getAttribute("message");

        if (!StringUtils.hasText(exception)) {
            setResponse(response, "유효하지 않은 토큰입니다. Redirection = " + "/login");
        }

        setResponse(response, message);
    }

    private void setResponse(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(401);

        ResponseDto<?> result =
                ResponseDto.failDto(StatusCode.FAIL, null, message);

        response.getWriter().println(result);
    }
}
