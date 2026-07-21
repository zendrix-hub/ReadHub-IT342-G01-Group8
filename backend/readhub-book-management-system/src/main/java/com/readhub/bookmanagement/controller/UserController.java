package com.readhub.bookmanagement.controller;

import com.readhub.bookmanagement.dto.ApiResponse;
import com.readhub.bookmanagement.dto.UserProfileDto;
import com.readhub.bookmanagement.dto.UserUpdateDto;
import com.readhub.bookmanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // --- CURRENT USER ENDPOINTS ---

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileDto>> getMyProfile(Authentication authentication) {
        String email = authentication.getName(); 
        UserProfileDto profile = userService.getCurrentUserProfile(email);
        return ResponseEntity.ok(ApiResponse.success(profile, "Profile retrieved successfully"));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<String>> updateMyProfile(@Valid @RequestBody UserUpdateDto request, Authentication authentication) {
        String email = authentication.getName();
        userService.updateUserProfile(email, request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", "Profile updated successfully"));
    }

    @PostMapping("/me/avatar")
    public ResponseEntity<ApiResponse<String>> uploadAvatar(@RequestParam("avatar") MultipartFile file, Authentication authentication) {
        String email = authentication.getName();
        String fileUrl = userService.uploadAvatar(email, file);
        return ResponseEntity.ok(ApiResponse.success(fileUrl, "Avatar uploaded successfully"));
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<String>> deleteMyAccount(Authentication authentication) {
        String email = authentication.getName();
        userService.deleteAccount(email);
        return ResponseEntity.ok(ApiResponse.success("Account deleted successfully", "Account deleted successfully"));
    }

    // --- ADMIN ENDPOINTS (FR-10) ---

    // 1. List All Students
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> getAllStudents(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "userId") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        if (page != null && size != null) {
            Page<UserProfileDto> students = userService.getAllStudents(keyword, PageRequest.of(page, size, sort));
            return ResponseEntity.ok(ApiResponse.success(students, "All students retrieved successfully"));
        }
        List<UserProfileDto> students = userService.getAllStudents(keyword, sort);
        return ResponseEntity.ok(ApiResponse.success(students, "All students retrieved successfully"));
    }

    // 2. Delete Specific User
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long id) {
        userService.deleteUserById(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", "User deleted successfully"));
    }
}