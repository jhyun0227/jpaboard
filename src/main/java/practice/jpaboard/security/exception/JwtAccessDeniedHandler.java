package practice.jpaboard.security.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import practice.jpaboard.util.dto.ResponseDto;
import practice.jpaboard.util.dto.StatusCode;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(403);

        ResponseDto<?> result =
                ResponseDto.failDto(StatusCode.FAIL, null, "권한이 없습니다.");

        response.getWriter().write(new ObjectMapper().writeValueAsString(result));
    }
}
