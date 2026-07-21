package com.readhub.bookmanagement.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Who receives the notification

    @ManyToOne
    @JoinColumn(name = "transaction_id")
    private Transaction transaction; // Context (optional)

    private String message;
    
    private boolean isRead;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime sentDate;

    @PrePersist
    protected void onCreate() {
        this.sentDate = LocalDateTime.now();
        this.isRead = false;
    }
}