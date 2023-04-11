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
import practice.jpaboard.util.dto.ResponseDto;
import practice.jpaboard.util.dto.StatusCode;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardController {

    private final BoardService boardService;

    @PostMapping("/add")
    public ResponseDto<Map<String, Long>> addBoard(@RequestBody BoardAddDto boardAddDto, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long boardId = boardService.addBoard(boardAddDto, userDetails);

        HashMap<String, Long> data = new HashMap<>();
        data.put("boastId", boardId);

        return new ResponseDto<>(StatusCode.SUCCESS, data, "정상적으로 작성되었습니다.", null);
    }

    @GetMapping("/{boardId}")
    public ResponseDto<BoardDetailDto> boardDetail(@PathVariable Long boardId) {
        BoardDetailDto boardDetailDto = boardService.boardDetail(boardId);

        return new ResponseDto<>(StatusCode.SUCCESS, boardDetailDto, "정상 조회", null);
    }

    @GetMapping("/list")
    public ResponseDto<Page<BoardPageDto>> boardPageList(@ModelAttribute BoardSearchCond boardSearchCond) {
        Page<BoardPageDto> boardPageDtos = boardService.boardPageList(boardSearchCond);

        return new ResponseDto<>(StatusCode.SUCCESS, boardPageDtos, "정상 조회", null);
    }

}
