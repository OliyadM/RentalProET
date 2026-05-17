package com.rentalpro.service;

import com.rentalpro.model.dto.request.RegisterRequest;
import com.rentalpro.model.dto.response.AuthResponse;

public interface UserService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(String email, String password);
}