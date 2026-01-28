package com.planup.board.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long> {
    // 페이징 처리된 목록 조회 (최신순 등은 Pageable로 제어)
    Page<Board> findAll(Pageable pageable);
}