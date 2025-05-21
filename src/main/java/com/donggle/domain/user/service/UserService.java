package com.donggle.domain.user.service;

import com.donggle.domain.user.domain.User;
import com.donggle.domain.user.dto.UserProfileResponse;
import com.donggle.domain.user.dto.UserSignupRequest;
import com.donggle.domain.user.dto.UserUpdateRequest;
import com.donggle.domain.user.repository.UserRepository;
import com.donggle.global.error.exception.EntityNotFoundException;
import com.donggle.global.util.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signup(UserSignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        if (userRepository.existsByStudentId(request.getStudentId())) {
            throw new IllegalArgumentException("이미 존재하는 학번입니다.");
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user =
                new User(
                        request.getEmail(),
                        encodedPassword,
                        request.getStudentId(),
                        request.getName());
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User login(String email, String password) {
        User user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        // 비밀번호 검증
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        return user;
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. ID: " + id));
    }

    @Transactional
    public void updateUserRole(Long userId, User.UserRole role) {
        User user = findById(userId);
        user.updateRole(role);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long userId) {
        User user = findById(userId);
        return UserProfileResponse.from(user);
    }

    @Transactional
    public UserProfileResponse updateUserProfile(Long userId, UserUpdateRequest request) {
        User user = findById(userId);

        // 이메일 변경 요청이 있고, 현재 이메일과 다른 경우
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            // 중복 이메일 체크
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
            }
            user.updateEmail(request.getEmail());
        }

        // 이름 변경 요청이 있는 경우
        if (request.getName() != null && !request.getName().isBlank()) {
            user.updateName(request.getName());
        }

        // 비밀번호 변경 요청이 있는 경우
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            String encodedPassword = passwordEncoder.encode(request.getPassword());
            user.updatePassword(encodedPassword);
        }

        User updatedUser = userRepository.save(user);
        return UserProfileResponse.from(updatedUser);
    }

    @Transactional
    public void updateUserRoleByAdmin(Long targetUserId, User.UserRole role, Long adminId) {
        // 관리자 권한 확인
        User admin = findById(adminId);
        if (admin.getRole() != User.UserRole.ADMIN) {
            throw new IllegalArgumentException("권한이 없습니다. 관리자만 사용자 권한을 변경할 수 있습니다.");
        }

        // 대상 사용자 권한 변경
        updateUserRole(targetUserId, role);
    }
}
