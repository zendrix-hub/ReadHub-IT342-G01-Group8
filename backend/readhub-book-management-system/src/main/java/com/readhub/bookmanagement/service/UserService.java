package com.readhub.bookmanagement.service;

import com.readhub.bookmanagement.dto.UserProfileDto;
import com.readhub.bookmanagement.dto.UserUpdateDto;
import com.readhub.bookmanagement.model.Role;
import com.readhub.bookmanagement.model.User;
import com.readhub.bookmanagement.repository.TransactionRepository;
import com.readhub.bookmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;
    
    private final Path fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();

    // --- MAIN FIX: Get All Students ---
    public List<UserProfileDto> getAllStudents() {
        // Query only student users from the database layer directly
        List<User> studentsList = userRepository.findByRole(Role.STUDENT);

        return studentsList.stream()
                .map(user -> UserProfileDto.builder()
                        .userId(user.getUserId())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .email(user.getEmail())
                        .role(user.getRole().name())
                        .avatarUrl(user.getAvatarUrl())
                        .build())
                .collect(Collectors.toList());
    }

    // --- Other Methods (Kept exactly as they were) ---

    public UserProfileDto getCurrentUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return UserProfileDto.builder()
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    @Transactional
    public void updateUserProfile(String email, UserUpdateDto request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

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

    @Transactional
    public String uploadAvatar(String email, MultipartFile file) throws IOException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!Files.exists(fileStorageLocation)) {
            Files.createDirectories(fileStorageLocation);
        }

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path targetLocation = fileStorageLocation.resolve(fileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        
        String fileUrl = "http://localhost:8080/uploads/" + fileName;
        user.setAvatarUrl(fileUrl);
        userRepository.save(user);
        
        return fileUrl; 
    }

    @Transactional
    public void deleteUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (transactionRepository.existsByUser(user)) {
            throw new RuntimeException("Cannot delete: User has transaction history. Please archive instead.");
        }
        userRepository.delete(user);
    }

    @Transactional
    public void deleteAccount(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(user);
    }
}