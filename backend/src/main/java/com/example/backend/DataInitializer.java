package com.example.backend;

import com.example.backend.user.UserRepository;
import com.example.backend.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public ApplicationRunner seedAdminUser(UserRepository userRepository, UserService userService) {
        return args -> {
            if (!userRepository.existsByUsername("admin")) {
                userService.register("admin", "admin");
                log.info("기본 관리자 계정 생성: admin / admin");
            }
        };
    }
}
