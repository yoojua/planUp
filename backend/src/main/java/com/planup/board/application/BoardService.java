package com.planup.board.application;

import com.planup.board.ui.BoardRequestDto;
import com.planup.board.ui.BoardResponseDto;
import com.planup.board.domain.Board;
import com.planup.board.domain.BoardRepository;
import com.planup.user.domain.User;
import com.planup.user.domain.UserRepository;
import com.planup.global.exception.CustomException;
import com.planup.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본적으로 읽기 전용 (성능 최적화)
public class BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository; // 작성자를 찾아야 하므로

    // 1. 게시글 작성
    @Transactional
    public Long createBoard(BoardRequestDto requestDto, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Board board = requestDto.toEntity(user);
        return boardRepository.save(board).getId();
    }

    // 2. 게시글 전체 조회 (페이징)
    public Page<BoardResponseDto> getBoardList(Pageable pageable) {
        return boardRepository.findAll(pageable)
                .map(BoardResponseDto::new); // Entity -> DTO 변환
    }

    // 3. 게시글 상세 조회
    @Transactional
    public BoardResponseDto getBoard(Long id) {
        Board board = findBoardById(id);
        board.increaseViewCount(); // 조회수 증가 (Dirty Checking)
        return new BoardResponseDto(board);
    }

    // 4. 게시글 수정
    @Transactional
    public Long updateBoard(Long id, BoardRequestDto requestDto, String email) {
        Board board = findBoardById(id);
        validateWriter(board, email); // 작성자 확인

        board.update(requestDto.getTitle(), requestDto.getContent()); // 수정
        return board.getId();
    }

    // 5. 게시글 삭제
    @Transactional
    public void deleteBoard(Long id, String email) {
        Board board = findBoardById(id);
        validateWriter(board, email); // 작성자 확인

        boardRepository.delete(board);
    }

    // == 공통 메서드 == //

    // ID로 게시글 찾기 (없으면 에러)
    private Board findBoardById(Long id) {
        return boardRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }

    // 작성자 검증 (로그인한 사람 == 글쓴이?)
    private void validateWriter(Board board, String email) {
        if (!board.getUser().getEmail().equals(email)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE); // 권한 없음 (나중에 FORBIDDEN 추가 추천)
        }
    }
}