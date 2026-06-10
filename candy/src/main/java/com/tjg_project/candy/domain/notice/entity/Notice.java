package com.tjg_project.candy.domain.notice.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "notice")
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 공지 ID (PK)

    @Column(length = 200, nullable = false)
    private String title; // 공지 제목

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; // 공지 내용

    @Column(length = 30)
    private String writer; // 작성자 (옵션)

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // 등록일 (자동 생성)

    @Column(name = "is_pinned")
    private Boolean isPinned = false; // 상단 고정 여부 (선택적)
}
