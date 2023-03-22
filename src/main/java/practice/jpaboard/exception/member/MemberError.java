package practice.jpaboard.exception.member;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MemberError {
    DUPLICATED_MEMBER_LOGIN_ID(HttpStatus.BAD_REQUEST, "이미 존재하는 ID입니다."),
    NOT_EXIST_MEMBER(HttpStatus.BAD_REQUEST, "존재하지 않는 ID입니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "일치하지 않는 비밀번호입니다.");

    private HttpStatus httpStatus;
    private String message;

}
