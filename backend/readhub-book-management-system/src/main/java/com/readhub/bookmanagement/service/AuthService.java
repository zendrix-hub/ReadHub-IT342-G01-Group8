package com.readhub.bookmanagement.service;

import com.readhub.bookmanagement.dto.AuthResponse;
import com.readhub.bookmanagement.dto.LoginRequest;
import com.readhub.bookmanagement.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse authenticate(LoginRequest request);
}