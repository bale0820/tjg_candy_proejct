package com.tjg_project.candy.domain.notification.service;

import com.tjg_project.candy.domain.notification.entity.Notification;
import com.tjg_project.candy.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public List<Notification> getActiveNotifications() {
        return notificationRepository.findByActiveTrueOrderByCreatedAtDesc();
    }
}
