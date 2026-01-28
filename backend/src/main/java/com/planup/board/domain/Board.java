package com.planup.board.domain;

import com.planup.comment.domain.Comment;
import com.planup.global.common.BaseTimeEntity;
import com.planup.likes.domain.BoardLike;
import com.planup.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자 접근 제한 (안전성 UP)
@SQLDelete(sql = "UPDATE board SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Board extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false) // 긴 본문 저장
    private String content;

    private int viewCount; // 조회수

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // 작성자 (N:1 관계)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // DB에는 user_id로 저장됨
    private User user;

    // 댓글 목록 (게시글 삭제 시 댓글도 자동 삭제)
    @OneToMany(mappedBy = "board", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    // 좋아요 목록 (게시글 삭제 시 좋아요도 자동 삭제)
    @OneToMany(mappedBy = "board", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<BoardLike> likes = new ArrayList<>();


    @Builder
    public Board(String title, String content, User user) {
        this.title = title;
        this.content = content;
        this.user = user;
        this.viewCount = 0;
    }

    // 게시글 수정 메서드 (Setter 대신 명확한 메서드 사용)
    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    // 조회수 증가 메서드
    public void increaseViewCount() {
        this.viewCount++;
    }
}