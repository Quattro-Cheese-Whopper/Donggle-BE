package com.donggle.domain.user.dto;

import com.donggle.domain.user.domain.User;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private Long id;
    private String email;
    private String studentId;
    private String name;
    private User.UserRole role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getStudentId(),
                user.getName(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
}
