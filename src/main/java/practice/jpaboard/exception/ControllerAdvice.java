package practice.jpaboard.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import practice.jpaboard.exception.board.BoardException;
import practice.jpaboard.exception.member.MemberException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ControllerAdvice {

    @ExceptionHandler(MemberException.class)
    public ResponseEntity<Map<String, String>> memberException(MemberException e) {
        Map<String, String> errorBody = new HashMap<>();

        String message = e.getMemberError().getMessage();
        errorBody.put("message", message);

        return ResponseEntity.status(e.getMemberError().getHttpStatus()).body(errorBody);
    }

    @ExceptionHandler(BoardException.class)
    public ResponseEntity<Map<String, String>> memberException(BoardException e) {
        Map<String, String> errorBody = new HashMap<>();

        String message = e.getBoardError().getMessage();
        errorBody.put("message", message);

        return ResponseEntity.status(e.getBoardError().getHttpStatus()).body(errorBody);
    }
}
