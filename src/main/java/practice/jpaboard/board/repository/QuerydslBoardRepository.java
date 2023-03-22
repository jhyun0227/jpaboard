package practice.jpaboard.board.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import practice.jpaboard.board.dto.BoardSearchCond;
import practice.jpaboard.board.entity.Board;

public interface QuerydslBoardRepository {
    Page<Board> findBoardList(BoardSearchCond boardSearchCond, Pageable pageable);
}
