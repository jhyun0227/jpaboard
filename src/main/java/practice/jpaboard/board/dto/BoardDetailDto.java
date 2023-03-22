package practice.jpaboard.board.dto;

import lombok.Data;
import practice.jpaboard.board.entity.Board;

import java.time.LocalDateTime;

@Data
public class BoardDetailDto {

    private Long boardId;
    private String boardTitle;
    private String boardContent;
    private String memberNickname;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;

    public void fromEntity(Board board) {
        this.boardId = board.getBoardId();
        this.boardTitle = board.getBoardTitle();
        this.boardContent = board.getBoardContent();
        this.memberNickname = board.getMember().getMemberNickname();
        this.createdDate = board.getCreatedDate();
        this.lastModifiedDate = board.getLastModifiedDate();
    }
}
