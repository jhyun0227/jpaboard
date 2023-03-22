package practice.jpaboard.exception.board;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BoardException extends RuntimeException {
    private BoardError boardError;
}
