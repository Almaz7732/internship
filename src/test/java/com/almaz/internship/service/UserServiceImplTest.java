package com.almaz.internship.service;

import com.almaz.internship.dto.UserLoginDto;
import com.almaz.internship.dto.UserRegistrationDto;
import com.almaz.internship.model.User;
import com.almaz.internship.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("register() - registration successful")
    void register_success() {
        // given (preparation)
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setName("Almaz");
        dto.setEmail("almaz@test.com");
        dto.setPassword("secret123");

        when(passwordEncoder.encode("secret123")).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        // when (action)
        User result = userService.register(dto);

        // then (checks)
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Almaz", result.getName());
        assertEquals("almaz@test.com", result.getEmail());
        assertEquals("hashed_password", result.getPassword());

        verify(passwordEncoder).encode("secret123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("login() - successful login")
    void login_success() {
        UserLoginDto dto = new UserLoginDto();
        dto.setEmail("almaz@test.com");
        dto.setPassword("secret123");

        User user = new User();
        user.setId(1L);
        user.setName("Almaz");
        user.setEmail("almaz@test.com");
        user.setPassword("hashed_password");

        when(userRepository.findByEmail("almaz@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret123", "hashed_password")).thenReturn(true);

        Optional<User> result = userService.login(dto);

        assertTrue(result.isPresent());
        assertEquals("almaz@test.com", result.get().getEmail());
    }

    @Test
    @DisplayName("login() - incorrect password")
    void login_wrongPassword() {
        UserLoginDto dto = new UserLoginDto();
        dto.setEmail("almaz@test.com");
        dto.setPassword("wrongpassword");

        User user = new User();
        user.setPassword("hashed_password");

        when(userRepository.findByEmail("almaz@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "hashed_password")).thenReturn(false);

        Optional<User> result = userService.login(dto);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("login() - user not found")
    void login_userNotFound() {
        UserLoginDto dto = new UserLoginDto();
        dto.setEmail("nobody@test.com");
        dto.setPassword("secret123");

        when(userRepository.findByEmail("nobody@test.com")).thenReturn(Optional.empty());

        Optional<User> result = userService.login(dto);

        assertTrue(result.isEmpty());
        verify(passwordEncoder, never()).matches(any(), any());
    }
}