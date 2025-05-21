package com.donggle.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @Pattern(
            regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*])[a-zA-Z0-9!@#$%^&*]{8,}$",
            message = "비밀번호는 영문, 숫자, 특수문자를 최소 1개씩 포함해야 합니다.")
    private String password;

    @Size(min = 2, max = 50, message = "이름은 2자 이상 50자 이하여야 합니다.")
    private String name;
}
