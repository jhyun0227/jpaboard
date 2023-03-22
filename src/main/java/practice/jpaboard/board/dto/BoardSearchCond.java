package practice.jpaboard.board.dto;

import lombok.Data;

@Data
public class BoardSearchCond {

    private Integer page;
    private Integer size;
    private String kind;
    private String inputText;
}
