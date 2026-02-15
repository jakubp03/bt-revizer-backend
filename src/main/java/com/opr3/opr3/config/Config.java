package com.opr3.opr3.config;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.opr3.opr3.entity.User;
import com.opr3.opr3.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class Config {

    private static final Logger log = LoggerFactory.getLogger(Config.class);

    @Value("${spring.profiles.active}")
    private String activeSpringProfile;
    @Value("${app.docker-profile}")
    private String activeDockerProfile;
    private final UserRepository userRepository;

    @Bean
    CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
            initializeMinimalTestData();
        };
    }

    @Transactional
    public void initializeMinimalTestData() {
        if (activeDockerProfile.equals("docker_dev") || activeSpringProfile.equals("dev")) {

            Optional<User> testUserOneOptional = userRepository.findUserByEmail("test1@email.com");
            Optional<User> testUserTwoOptional = userRepository.findUserByEmail("test2@email.com");

            if (!testUserOneOptional.isPresent()) {
                User testUser = new User("Test User One", this.passwordEncoder().encode("testuserone"),
                        "test1@email.com");
                userRepository.save(testUser);
                log.info("adding missing test user 1");
            }

            if (!testUserTwoOptional.isPresent()) {
                User testUser2 = new User("Test User Two", this.passwordEncoder().encode("testusertwo"),
                        "test2@email.com");
                userRepository.save(testUser2);
                log.info("adding missing test user 2");
            }
        }
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findUserByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
