package com.readhub.bookmanagement.controller;

import com.readhub.bookmanagement.dto.ApiResponse;
import com.readhub.bookmanagement.dto.NotificationDto;
import com.readhub.bookmanagement.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Endpoints for retrieving and managing user notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Get notifications for the authenticated user", description = "Returns all notification records for the currently logged-in user, ordered by most recent first.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Notifications retrieved successfully")
    public ResponseEntity<ApiResponse<List<NotificationDto>>> getMyNotifications(Authentication authentication) {
        String email = authentication.getName();
        List<NotificationDto> notifications = notificationService.getMyNotifications(email);
        return ResponseEntity.ok(ApiResponse.success(notifications, "Notifications retrieved successfully"));
    }

    @PutMapping("/read")
    @Operation(summary = "Mark all notifications as read", description = "Sets all unread notification records for the authenticated user to read status.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Notifications marked as read successfully")
    public ResponseEntity<ApiResponse<Void>> markAsRead(Authentication authentication) {
        notificationService.markAllAsRead(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(null, "Notifications marked as read successfully"));
    }
}