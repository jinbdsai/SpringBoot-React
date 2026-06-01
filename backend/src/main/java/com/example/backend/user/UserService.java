package com.example.backend.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(String username, String rawPassword) {
        validateUsername(username);
        validatePassword(rawPassword);
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        User user = new User(username, passwordEncoder.encode(rawPassword));
        User saved = userRepository.save(user);
        log.info("회원가입 성공: username={}, id={}", saved.getUsername(), saved.getId());
        return saved;
    }

    public User authenticate(String username, String rawPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("로그인 실패 (없는 아이디): username={}", username);
                    return new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
                });
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            log.warn("로그인 실패 (비밀번호 불일치): username={}", username);
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        log.info("로그인 성공: username={}", username);
        return user;
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    private void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("아이디를 입력해주세요.");
        }
        if (username.length() < 3 || username.length() > 30) {
            throw new IllegalArgumentException("아이디는 3~30자여야 합니다.");
        }
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("아이디는 영문/숫자/언더스코어만 사용 가능합니다.");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 4) {
            throw new IllegalArgumentException("비밀번호는 4자 이상이어야 합니다.");
        }
    }
}
