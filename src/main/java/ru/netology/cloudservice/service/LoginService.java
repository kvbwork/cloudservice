package ru.netology.cloudservice.service;

import ru.netology.cloudservice.model.request.LoginRequest;
import ru.netology.cloudservice.model.response.LoginResponse;

public interface LoginService {
    LoginResponse login(LoginRequest loginRequest);

    void logout(String login, String token);

}
