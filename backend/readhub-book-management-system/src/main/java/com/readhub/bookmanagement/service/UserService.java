package com.readhub.bookmanagement.service;

import com.readhub.bookmanagement.dto.UserProfileDto;
import com.readhub.bookmanagement.dto.UserUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {
    List<UserProfileDto> getAllStudents();
    List<UserProfileDto> getAllStudents(String keyword);
    List<UserProfileDto> getAllStudents(Sort sort);
    List<UserProfileDto> getAllStudents(String keyword, Sort sort);
    Page<UserProfileDto> getAllStudents(Pageable pageable);
    Page<UserProfileDto> getAllStudents(String keyword, Pageable pageable);
    UserProfileDto getCurrentUserProfile(String email);
    void updateUserProfile(String email, UserUpdateDto request);
    String uploadAvatar(String email, MultipartFile file);
    void deleteUserById(Long id);
    void deleteAccount(String email);
}