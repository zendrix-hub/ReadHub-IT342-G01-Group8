package com.readhub.bookmanagement.repository;

import com.readhub.bookmanagement.model.Notification;
import com.readhub.bookmanagement.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // Get all notifications for a specific user, ordered by newest first
    @EntityGraph(attributePaths = {"transaction", "transaction.book"})
    List<Notification> findByUserOrderBySentDateDesc(User user);
    
    // Get only unread notifications
    List<Notification> findByUserAndIsReadFalse(User user);
}