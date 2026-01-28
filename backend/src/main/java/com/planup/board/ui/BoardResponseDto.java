package com.planup.board.ui;

import com.planup.board.domain.Board;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class BoardResponseDto {
    private Long id;
    private String title;
    private String content;
    private String writer; // 작성자 이름 (User 객체 전체 노출 X)
    private int viewCount;
    private int likeCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Entity -> DTO 변환 (생성자)
    public BoardResponseDto(Board board) {
        this.id = board.getId();
        this.title = board.getTitle();
        this.content = board.getContent();
        this.writer = board.getUser().getNickname(); // User 엔티티의 닉네임만 가져옴
        this.viewCount = board.getViewCount();
        this.likeCount = board.getLikes().size(); // Lazy Loading으로 가져옴
        this.createdAt = board.getCreatedAt();
    }
}