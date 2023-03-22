package practice.jpaboard.board.dto;

import lombok.Data;

@Data
public class BoardAddDto {

    private String boardTitle;
    private String boardContent;
    private Long memberId;
}
