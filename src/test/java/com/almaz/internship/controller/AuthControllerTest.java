package com.almaz.internship.controller;

import com.almaz.internship.dto.UserLoginDto;
import com.almaz.internship.dto.UserRegistrationDto;
import com.almaz.internship.model.User;
import com.almaz.internship.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("POST /register - registration successful, 201")
    void register_success() throws Exception {
        // given
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setName("Almaz");
        dto.setEmail("almaz@test.com");
        dto.setPassword("secret123");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setName("Almaz");
        savedUser.setEmail("almaz@test.com");

        when(userService.register(any(UserRegistrationDto.class))).thenReturn(savedUser);

        // when & then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Almaz"))
                .andExpect(jsonPath("$.email").value("almaz@test.com"));
    }

    @Test
    @DisplayName("POST /register - duplicate email, 409")
    void register_duplicateEmail() throws Exception {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setName("Almaz");
        dto.setEmail("almaz@test.com");
        dto.setPassword("secret123");

        when(userService.register(any(UserRegistrationDto.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Email already exists"));
    }

    @Test
    @DisplayName("POST /login - successful login, 200")
    void login_success() throws Exception {
        UserLoginDto dto = new UserLoginDto();
        dto.setEmail("almaz@test.com");
        dto.setPassword("secret123");

        User user = new User();
        user.setId(1L);
        user.setName("Almaz");
        user.setEmail("almaz@test.com");

        when(userService.login(any(UserLoginDto.class))).thenReturn(Optional.of(user));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Almaz"))
                .andExpect(jsonPath("$.email").value("almaz@test.com"));
    }

    @Test
    @DisplayName("POST /login - incorrect data, 401")
    void login_invalidCredentials() throws Exception {
        UserLoginDto dto = new UserLoginDto();
        dto.setEmail("almaz@test.com");
        dto.setPassword("wrongpassword");

        when(userService.login(any(UserLoginDto.class))).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid email or password"));
    }
}