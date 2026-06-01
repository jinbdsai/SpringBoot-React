package com.example.backend.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks UserService userService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = new User("jkdev", "hashed");
    }

    @Test
    @DisplayName("회원가입 성공")
    void register_success() {
        when(userRepository.existsByUsername("jkdev")).thenReturn(false);
        when(passwordEncoder.encode("pw1234")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = userService.register("jkdev", "pw1234");

        assertThat(saved.getUsername()).isEqualTo("jkdev");
        assertThat(saved.getPasswordHash()).isEqualTo("hashed");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("중복 아이디는 거부")
    void register_duplicate_rejected() {
        when(userRepository.existsByUsername("jkdev")).thenReturn(true);

        assertThatThrownBy(() -> userService.register("jkdev", "pw1234"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 사용 중");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("짧은 아이디는 거부")
    void register_short_username_rejected() {
        assertThatThrownBy(() -> userService.register("ab", "pw1234"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("3~30자");
    }

    @Test
    @DisplayName("짧은 비밀번호는 거부")
    void register_short_password_rejected() {
        assertThatThrownBy(() -> userService.register("jkdev", "12"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("4자 이상");
    }

    @Test
    @DisplayName("로그인 성공")
    void authenticate_success() {
        when(userRepository.findByUsername("jkdev")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("pw1234", "hashed")).thenReturn(true);

        User authenticated = userService.authenticate("jkdev", "pw1234");

        assertThat(authenticated).isSameAs(existingUser);
    }

    @Test
    @DisplayName("없는 아이디로 로그인 시 예외")
    void authenticate_unknown_username() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.authenticate("ghost", "pw1234"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("아이디 또는 비밀번호");
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인 시 예외")
    void authenticate_wrong_password() {
        when(userRepository.findByUsername("jkdev")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> userService.authenticate("jkdev", "wrong"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("아이디 또는 비밀번호");
    }
}
