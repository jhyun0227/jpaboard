package practice.jpaboard.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import practice.jpaboard.exception.board.BoardException;
import practice.jpaboard.exception.member.MemberException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ControllerAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> methodArgumentNotValidException(MethodArgumentNotValidException e) {
        Map<String, String> errorBody = new HashMap<>();

        e.getBindingResult().getAllErrors()
                .forEach(error -> {
                    String fieldName = ((FieldError) error).getField();
                    String errorMessage = error.getDefaultMessage();
                    errorBody.put(fieldName, errorMessage);
                });
        /*
        log.info("e.getMessage() = {}", e.getMessage());
        log.info("e.getBindingResult() = {}", e.getBindingResult());
        log.info("e.getTarget() = {}", e.getTarget());
        log.info("e.getErrorCount() = {}", e.getErrorCount());
        log.info("e.getFieldError = {}", e.getFieldError());
        log.info("e.getParameter() = {}", e.getParameter());
        log.info("e.getStackTrace() = {}", e.getStackTrace());
        */

        return ResponseEntity.badRequest().body(errorBody);
    }

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
