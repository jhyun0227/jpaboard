package practice.jpaboard.board.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import practice.jpaboard.board.dto.BoardAddDto;
import practice.jpaboard.board.dto.BoardDetailDto;
import practice.jpaboard.board.dto.BoardPageDto;
import practice.jpaboard.board.dto.BoardSearchCond;
import practice.jpaboard.board.service.BoardService;
import practice.jpaboard.security.auth.UserDetailsImpl;

import javax.annotation.PostConstruct;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardController {

    private final BoardService boardService;

    @PostMapping("/add")
    public Long addBoard(@RequestBody BoardAddDto boardAddDto, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return boardService.addBoard(boardAddDto, userDetails);
    }

    @GetMapping("/{boardId}")
    public BoardDetailDto boardDetail(@PathVariable Long boardId) {
        return boardService.boardDetail(boardId);
    }

    @GetMapping("/list")
    public Page<BoardPageDto> boardPageList(@ModelAttribute BoardSearchCond boardSearchCond) {
        System.out.println("boardSearchCond.getKind() = " + boardSearchCond.getKind());
        System.out.println("boardSearchCond.getInputText() = " + boardSearchCond.getInputText());
        return boardService.boardPageList(boardSearchCond);
    }

}
