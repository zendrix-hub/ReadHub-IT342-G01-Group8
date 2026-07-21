package com.readhub.bookmanagement.service;

import com.readhub.bookmanagement.dto.NotificationDto;
import com.readhub.bookmanagement.model.Notification;
import com.readhub.bookmanagement.model.Transaction;
import com.readhub.bookmanagement.model.User;

import java.util.List;

public interface NotificationService {
    void sendNotification(User user, String message, Transaction transaction);
    List<NotificationDto> getMyNotifications(String email);
    void markAllAsRead(String email);
}