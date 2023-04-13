package practice.jpaboard.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import practice.jpaboard.exception.board.BoardException;
import practice.jpaboard.exception.member.MemberException;
import practice.jpaboard.util.dto.ResponseDto;
import practice.jpaboard.util.dto.StatusCode;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ControllerAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> methodArgumentNotValidException(MethodArgumentNotValidException e) {
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

        ResponseDto<?> result
                = ResponseDto.failDto(StatusCode.FAIL, errorBody, "예외 발생");

        return ResponseEntity.badRequest().body(result);
    }

    @ExceptionHandler(MemberException.class)
    public ResponseEntity<?> memberException(MemberException e) {

        Map<String, String> errorBody = new HashMap<>();
        String message = e.getMemberError().getMessage();
        errorBody.put("message", message);

        ResponseDto<?> result
                = ResponseDto.failDto(StatusCode.FAIL, errorBody, "예외 발생");

        return ResponseEntity.status(e.getMemberError().getHttpStatus()).body(result);
    }

    @ExceptionHandler(BoardException.class)
    public ResponseEntity<?> memberException(BoardException e) {

        Map<String, String> errorBody = new HashMap<>();
        String message = e.getBoardError().getMessage();
        errorBody.put("message", message);

        ResponseDto<?> result
                = ResponseDto.failDto(StatusCode.FAIL, errorBody, "예외 발생");

        return ResponseEntity.status(e.getBoardError().getHttpStatus()).body(result);
    }
}
