package com.example.backend.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "아이디는 필수입니다.")
    @Size(min = 3, max = 30, message = "아이디는 3~30자여야 합니다.")
    private String username;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 4, max = 100, message = "비밀번호는 4~100자여야 합니다.")
    private String password;
}
