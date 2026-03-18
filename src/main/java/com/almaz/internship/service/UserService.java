package com.almaz.internship.service;

import com.almaz.internship.dto.UserLoginDto;
import com.almaz.internship.dto.UserRegistrationDto;
import com.almaz.internship.model.User;

import java.util.Optional;

public interface UserService {

    User register(UserRegistrationDto dto);

    Optional<User> login(UserLoginDto dto);
}