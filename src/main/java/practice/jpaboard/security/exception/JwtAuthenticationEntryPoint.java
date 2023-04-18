package practice.jpaboard.security.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
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

@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        String exception = (String) request.getAttribute("exception");
        String message = (String) request.getAttribute("message");

        if (!StringUtils.hasText(exception)) {
            setResponse(response, "원인을 알수없는 에러. Redirection = " + "/login");
        }

        setResponse(response, message);
    }

    private void setResponse(HttpServletResponse response, String message) throws IOException {
        int index = message.indexOf("/");
        String subString = message.substring(index);

        log.info("subString = {}", subString);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(401);
        response.setHeader(HttpHeaders.LOCATION, subString);

        ResponseDto<?> result =
                ResponseDto.failDto(StatusCode.FAIL, null, message);

        response.getWriter().write(new ObjectMapper().writeValueAsString(result));
    }
}
