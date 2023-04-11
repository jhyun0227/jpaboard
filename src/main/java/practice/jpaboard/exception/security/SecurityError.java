package practice.jpaboard.exception.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SecurityError {
    JWT_AUTHENTICATION_ENTRY_POINT(HttpStatus.UNAUTHORIZED, "잘못된 접근입니다. 로그인을 해주세요,"),
    JWT_ACCESS_DENIED_HANDLER(HttpStatus.FORBIDDEN, "권한이 없습니다."),
    INCORRECT_CLAIM(HttpStatus.BAD_REQUEST, "잘못된 토큰입니다."),
    USERNAME_NOT_FOUND_EXCEPTION(HttpStatus.BAD_REQUEST, "사용자를 찾을 수 없습니다.");


    private HttpStatus httpStatus;
    private String message;

    public void setMessage(String message) {
        this.message = message;
    }
}
