package practice.jpaboard.util.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDto<T> {

    private StatusCode statusCode;
    private T data;
    private String message;
    private String errorMessage;

}
