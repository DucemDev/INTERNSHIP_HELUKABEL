package com.helu.internship.service;

import com.helu.internship.dto.request.UserRequest;
import com.helu.internship.dto.response.UserResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {
    List<UserResponse> getAllUsers();
    UserResponse getUserById(UUID id);
    UserResponse createUser(UserRequest request);
    UserResponse updateUser(UUID id, UserRequest request);
    void deleteUser(UUID id);
}
