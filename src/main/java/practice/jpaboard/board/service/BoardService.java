package practice.jpaboard.board.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import practice.jpaboard.board.dto.BoardAddDto;
import practice.jpaboard.board.dto.BoardDetailDto;
import practice.jpaboard.board.dto.BoardPageDto;
import practice.jpaboard.board.dto.BoardSearchCond;
import practice.jpaboard.board.entity.Board;
import practice.jpaboard.board.repository.BoardRepository;
import practice.jpaboard.exception.board.BoardError;
import practice.jpaboard.exception.board.BoardException;
import practice.jpaboard.exception.member.MemberError;
import practice.jpaboard.exception.member.MemberException;
import practice.jpaboard.member.entity.Member;
import practice.jpaboard.member.repository.MemberRepository;

import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BoardService {

    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;

    public Long addBoard(BoardAddDto boardAddDto) {
        Member findMember = memberRepository.findById(boardAddDto.getMemberId())
                .orElseThrow(() -> new MemberException(MemberError.NOT_EXIST_MEMBER));

        Board board = Board.builder()
                .boardTitle(boardAddDto.getBoardTitle())
                .boardContent(boardAddDto.getBoardContent())
                .member(findMember)
                .build();

        boardRepository.save(board);

        return board.getBoardId();
    }

    public BoardDetailDto boardDetail(Long boardId) {
        Board findBoard = boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardException(BoardError.NOT_EXIST_BOARD));

        BoardDetailDto boardDetailDto = new BoardDetailDto();
        boardDetailDto.fromEntity(findBoard);

        return boardDetailDto;
    }


    public Page<BoardPageDto> boardPageList(BoardSearchCond boardSearchCond) {
        PageRequest pageRequest = PageRequest.of(boardSearchCond.getPage(), boardSearchCond.getSize());

        Page<Board> boardList = boardRepository.findBoardList(boardSearchCond, pageRequest);

        return boardList.map(board -> {
            BoardPageDto boardPageDto = new BoardPageDto();
            boardPageDto.fromEntity(board);
            return boardPageDto;
        });
    }
}
