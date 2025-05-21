package com.donggle.domain.user.controller;

import com.donggle.domain.user.domain.User;
import com.donggle.domain.user.dto.UserProfileResponse;
import com.donggle.domain.user.dto.UserUpdateRequest;
import com.donggle.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;

    @Override
    public ResponseEntity<UserProfileResponse> getCurrentUser(Long userId) {
        UserProfileResponse response = userService.getUserProfile(userId);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<UserProfileResponse> getUserById(Long userId) {
        UserProfileResponse response = userService.getUserProfile(userId);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<UserProfileResponse> updateUserProfile(
            UserUpdateRequest request, Long userId) {
        UserProfileResponse response = userService.updateUserProfile(userId, request);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> updateUserRole(Long userId, User.UserRole role, Long adminId) {
        userService.updateUserRoleByAdmin(userId, role, adminId);
        return ResponseEntity.ok().build();
    }
}
