package practice.jpaboard.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import practice.jpaboard.board.entity.Board;

public interface BoardRepository extends JpaRepository<Board, Long>, QuerydslBoardRepository {
}
