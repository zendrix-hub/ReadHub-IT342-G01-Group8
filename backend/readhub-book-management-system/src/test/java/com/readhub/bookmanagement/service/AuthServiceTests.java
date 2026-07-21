package com.readhub.bookmanagement.service;

import com.readhub.bookmanagement.dto.AuthResponse;
import com.readhub.bookmanagement.dto.LoginRequest;
import com.readhub.bookmanagement.dto.RegisterRequest;
import com.readhub.bookmanagement.exception.ResourceAlreadyExistsException;
import com.readhub.bookmanagement.exception.ResourceNotFoundException;
import com.readhub.bookmanagement.model.Role;
import com.readhub.bookmanagement.model.User;
import com.readhub.bookmanagement.repository.UserRepository;
import com.readhub.bookmanagement.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;

    @BeforeEach
    public void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");
        registerRequest.setEmail("john@readhub.com");
        registerRequest.setPassword("securepass");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("john@readhub.com");
        loginRequest.setPassword("securepass");

        user = User.builder()
                .userId(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@readhub.com")
                .passwordHash("encodedpass")
                .role(Role.STUDENT)
                .build();
    }

    @Test
    public void testRegister_Success() {
        when(userRepository.existsByEmail("john@readhub.com")).thenReturn(false);
        when(passwordEncoder.encode("securepass")).thenReturn("encodedpass");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtUtils.generateToken(any(UserDetails.class))).thenReturn("mockJwtToken");

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("mockJwtToken", response.getToken());
        verify(userRepository, times(1)).save(any(User.class));
        verify(jwtUtils, times(1)).generateToken(any(UserDetails.class));
    }

    @Test
    public void testRegister_EmailAlreadyExists() {
        when(userRepository.existsByEmail("john@readhub.com")).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any(User.class));
        verify(jwtUtils, never()).generateToken(any(UserDetails.class));
    }

    @Test
    public void testAuthenticate_Success() {
        when(userRepository.findByEmail("john@readhub.com")).thenReturn(Optional.of(user));
        when(jwtUtils.generateToken(any(UserDetails.class))).thenReturn("mockJwtToken");

        AuthResponse response = authService.authenticate(loginRequest);

        assertNotNull(response);
        assertEquals("mockJwtToken", response.getToken());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, times(1)).findByEmail("john@readhub.com");
    }

    @Test
    public void testAuthenticate_UserNotFound() {
        when(userRepository.findByEmail("john@readhub.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.authenticate(loginRequest));
        verify(jwtUtils, never()).generateToken(any(UserDetails.class));
    }
}
