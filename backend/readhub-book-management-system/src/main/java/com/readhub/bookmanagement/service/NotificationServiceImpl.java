package com.readhub.bookmanagement.service;

import com.readhub.bookmanagement.dto.NotificationDto;
import com.readhub.bookmanagement.exception.ResourceNotFoundException;
import com.readhub.bookmanagement.model.Notification;
import com.readhub.bookmanagement.model.Transaction;
import com.readhub.bookmanagement.model.User;
import com.readhub.bookmanagement.repository.NotificationRepository;
import com.readhub.bookmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    private NotificationDto mapToDto(Notification n) {
        NotificationDto dto = NotificationDto.builder()
                .notificationId(n.getNotificationId())
                .message(n.getMessage())
                .isRead(n.isRead())
                .sentDate(n.getSentDate())
                .build();

        if (n.getTransaction() != null) {
            Transaction txn = n.getTransaction();
            dto.setTransactionId(txn.getTransactionId());
            if (txn.getBook() != null) {
                dto.setBookTitle(txn.getBook().getTitle());
            }
            if (txn.getStatus() != null) {
                dto.setTransactionStatus(txn.getStatus().name());
            }
        }
        return dto;
    }

    @Override
    @Transactional
    public void sendNotification(User user, String message, Transaction transaction) {
        // Save Internal Notification (Bell Icon)
        Notification notification = Notification.builder()
                .user(user)
                .message(message)
                .transaction(transaction)
                .isRead(false)
                .build();
        
        notificationRepository.save(notification);

        // Send External Email (Gmail)
        if (user.getEmail() != null && user.getEmail().contains("@")) {
            String subject = "ReadHub Notification";
            
            if (transaction != null) {
                subject = "ReadHub Update: " + transaction.getBook().getTitle();
            } else if (message.contains("URGENT")) {
                subject = "URGENT: ReadHub Overdue Alert";
            }

            emailService.sendEmail(user.getEmail(), subject, message);
        }
    }

    @Override
    public List<NotificationDto> getMyNotifications(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return notificationRepository.findByUserOrderBySentDateDesc(user).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markAllAsRead(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        List<Notification> unread = notificationRepository.findByUserAndIsReadFalse(user);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }
}

