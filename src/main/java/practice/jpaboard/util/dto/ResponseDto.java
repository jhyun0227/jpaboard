package practice.jpaboard.util.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDto<T> {

    private StatusCode statusCode;
    private T data;
    private T errorData;
    private String message;

    /**
     * 성공 메서드
     */
    public static <T> ResponseDto<?> successDto(StatusCode statusCode, T data, String message) {
        ResponseDto<T> successResponseDto = new ResponseDto<>();
        successResponseDto.setStatusCode(statusCode);
        successResponseDto.setData(data);
        successResponseDto.setMessage(message);

        return successResponseDto;
    }

    /**
     * 실패 메서드
     */
    public static <T> ResponseDto<?> failDto(StatusCode statusCode, T errorData, String message) {
        ResponseDto<T> failResponseDto = new ResponseDto<>();
        failResponseDto.setStatusCode(statusCode);
        failResponseDto.setErrorData(errorData);
        failResponseDto.setMessage(message);

        return failResponseDto;
    }
}
