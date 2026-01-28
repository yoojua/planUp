package com.planup.board.ui;

import com.planup.board.application.BoardService;
import com.planup.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    // 1. 게시글 작성
    @PostMapping
    public ApiResponse<Long> createBoard(@RequestBody BoardRequestDto requestDto, Principal principal) {
        // Principal이 null이면 JWT 필터 문제거나 로그인을 안 한 상태
        return ApiResponse.success(boardService.createBoard(requestDto, principal.getName()));
    }

    // 2. 게시글 목록 조회 (페이징: ?page=0&size=10)
    // 예시: /api/boards?page=0&size=10&sort=createdAt,desc
    @GetMapping
    public ApiResponse<Page<BoardResponseDto>> getBoardList(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.success(boardService.getBoardList(pageable));
    }

    // 3. 게시글 상세 조회
    @GetMapping("/{id}")
    public ApiResponse<BoardResponseDto> getBoard(@PathVariable Long id) {
        return ApiResponse.success(boardService.getBoard(id));
    }

    // 4. 게시글 수정
    @PutMapping("/{id}")
    public ApiResponse<Long> updateBoard(@PathVariable Long id, @RequestBody BoardRequestDto requestDto, Principal principal) {
        return ApiResponse.success(boardService.updateBoard(id, requestDto, principal.getName()));
    }

    // 5. 게시글 삭제
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteBoard(@PathVariable Long id, Principal principal) {
        boardService.deleteBoard(id, principal.getName());
        return ApiResponse.success();
    }
}