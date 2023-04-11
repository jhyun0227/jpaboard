package practice.jpaboard.board.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class BoardAddDto {

    @NotBlank(message = "제목은 필수 입력값입니다.")
    private String boardTitle;
    private String boardContent;

}
