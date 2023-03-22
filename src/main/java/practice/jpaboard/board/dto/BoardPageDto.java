package practice.jpaboard.board.dto;

import lombok.Data;
import practice.jpaboard.board.entity.Board;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class BoardPageDto {

    private Long boardId;
    private String boardTitle;
    private LocalDateTime createdDate;
    private String memberNickname;

    public void fromEntity(Board board) {
        this.boardId = board.getBoardId();
        this.boardTitle = board.getBoardTitle();
        this.createdDate = board.getCreatedDate();
        this.memberNickname = board.getMember().getMemberNickname();
    }
}
