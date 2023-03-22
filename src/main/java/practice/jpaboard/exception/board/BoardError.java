package practice.jpaboard.exception.board;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum BoardError {
    NOT_EXIST_BOARD(HttpStatus.BAD_REQUEST, "존재하지 않는 게시글입니다.");

    private HttpStatus httpStatus;
    private String message;

}
