package com.tjg_project.candy.domain.notification.controller;

import com.tjg_project.candy.domain.notification.entity.Notification;
import com.tjg_project.candy.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public List<Notification> getNotifications() {
        return notificationService.getActiveNotifications();
    }
}
