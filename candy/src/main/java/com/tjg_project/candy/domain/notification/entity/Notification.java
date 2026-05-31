package com.tjg_project.candy.domain.notification.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String title;

    @Column(length = 500, nullable = false)
    private String content;

    @Column(length = 30, nullable = false)
    private String type = "INFO";

    @Column(length = 255)
    private String linkUrl;

    private boolean active = true;

    private LocalDateTime createdAt = LocalDateTime.now();
}
