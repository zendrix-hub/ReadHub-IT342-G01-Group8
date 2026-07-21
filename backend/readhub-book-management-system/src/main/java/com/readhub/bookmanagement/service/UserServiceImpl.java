package com.readhub.bookmanagement.service;

import com.readhub.bookmanagement.dto.UserProfileDto;
import com.readhub.bookmanagement.dto.UserUpdateDto;
import com.readhub.bookmanagement.exception.DeleteViolationException;
import com.readhub.bookmanagement.exception.FileStorageException;
import com.readhub.bookmanagement.exception.ResourceNotFoundException;
import com.readhub.bookmanagement.model.Role;
import com.readhub.bookmanagement.model.User;
import com.readhub.bookmanagement.repository.TransactionRepository;
import com.readhub.bookmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    private UserProfileDto mapUserDto(User user) {
        return UserProfileDto.builder()
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    private List<UserProfileDto> mapUserList(List<User> users) {
        return users.stream()
                .map(this::mapUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserProfileDto> getAllStudents() {
        return getAllStudents((String) null);
    }

    @Override
    public List<UserProfileDto> getAllStudents(String keyword) {
        List<User> studentsList = userRepository.searchStudents(keyword, Sort.unsorted());
        return mapUserList(studentsList);
    }

    @Override
    public List<UserProfileDto> getAllStudents(Sort sort) {
        return getAllStudents(null, sort);
    }

    @Override
    public List<UserProfileDto> getAllStudents(String keyword, Sort sort) {
        List<User> studentsList = userRepository.searchStudents(keyword, sort);
        return mapUserList(studentsList);
    }

    @Override
    public Page<UserProfileDto> getAllStudents(Pageable pageable) {
        return getAllStudents(null, pageable);
    }

    @Override
    public Page<UserProfileDto> getAllStudents(String keyword, Pageable pageable) {
        Page<User> studentsPage = userRepository.searchStudents(keyword, pageable);
        return studentsPage.map(this::mapUserDto);
    }

    @Override
    public UserProfileDto getCurrentUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return UserProfileDto.builder()
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    @Override
    @Transactional
    public void updateUserProfile(String email, UserUpdateDto request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getFirstName() != null && !request.getFirstName().isEmpty()) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null && !request.getLastName().isEmpty()) {
            user.setLastName(request.getLastName());
        }
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        userRepository.save(user);
    }

    @Override
    @Transactional
    public String uploadAvatar(String email, MultipartFile file) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("Cannot upload empty file");
        }

        // Limit size to 5MB
        long maxSize = 5 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new FileStorageException("File size exceeds maximum limit of 5MB");
        }

        // Validate content type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new FileStorageException("Only image files are allowed");
        }

        String subType = contentType.substring(6).toLowerCase();
        if (!subType.equals("jpeg") && !subType.equals("jpg") && !subType.equals("png") && !subType.equals("gif") && !subType.equals("webp")) {
            throw new FileStorageException("Unsupported image format. Allowed: JPG, PNG, GIF, WEBP");
        }

        // Prevent path traversal
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && originalFilename.contains("..")) {
            throw new FileStorageException("Invalid filename path traversal attempt detected");
        }

        String fileUrl = fileStorageService.storeFile(file);
        user.setAvatarUrl(fileUrl);
        userRepository.save(user);
        
        return fileUrl;
    }

    @Override
    @Transactional
    public void deleteUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (transactionRepository.existsByUser(user)) {
            throw new DeleteViolationException("Cannot delete: User has transaction history. Please archive instead.");
        }
        userRepository.delete(user);
    }

    @Override
    @Transactional
    public void deleteAccount(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        userRepository.delete(user);
    }
}
