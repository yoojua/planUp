package com.planup.board.ui;

import com.planup.board.domain.Board;
import com.planup.user.domain.User;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BoardRequestDto {
    private String title;
    private String content;

    // DTO -> Entity 변환 (Service에서 사용)
    public Board toEntity(User user) {
        return Board.builder()
                .title(title)
                .content(content)
                .user(user)
                .build();
    }
}