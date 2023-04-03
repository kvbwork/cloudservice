package ru.netology.cloudservice.service.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.netology.cloudservice.model.request.LoginRequest;
import ru.netology.cloudservice.model.response.LoginResponse;
import ru.netology.cloudservice.service.TokenRegistrar;

import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginServiceImplTest {
    private static final String TEST_USER_NAME = "TEST_USER_NAME";
    private static final String TEST_PASSWORD = "TEST_PASSWORD";
    private static final String TEST_ROLE = "USER";
    private static final String TEST_TOKEN = "TEST-TOKEN-VALUE";

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenRegistrar tokenRegistrar;

    @InjectMocks
    private LoginServiceImpl sut;

    @Test
    @DisplayName("login() успешно возвращает токен")
    public void authenticate_success() {
        UserDetails userDetails = User.builder()
                .username(TEST_USER_NAME)
                .password(TEST_PASSWORD)
                .roles(TEST_ROLE)
                .build();

        when(userDetailsService.loadUserByUsername(TEST_USER_NAME)).thenReturn(userDetails);
        when(passwordEncoder.matches(TEST_PASSWORD, TEST_PASSWORD)).thenReturn(true);
        when(tokenRegistrar.register(userDetails)).thenReturn(TEST_TOKEN);

        LoginRequest loginRequest = new LoginRequest(TEST_USER_NAME, TEST_PASSWORD);
        LoginResponse loginResponse = sut.login(loginRequest);

        Assertions.assertEquals(TEST_TOKEN, loginResponse.getAuthToken());
    }

    @Test
    @DisplayName("login() ошибка неправильный пароль")
    public void authenticate_password_error() {
        UserDetails userDetails = User.builder()
                .username(TEST_USER_NAME)
                .password(TEST_PASSWORD)
                .roles(TEST_ROLE)
                .build();
        when(userDetailsService.loadUserByUsername(TEST_USER_NAME)).thenReturn(userDetails);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        LoginRequest loginRequest = new LoginRequest(TEST_USER_NAME, TEST_PASSWORD);

        assertThrowsExactly(BadCredentialsException.class,
                () -> sut.login(loginRequest));
    }

    @Test
    @DisplayName("login() ошибка пользователь не найден")
    public void authenticate_user_not_found() {
        when(userDetailsService.loadUserByUsername(anyString()))
                .thenThrow(new UsernameNotFoundException("Пользователь не найден."));

        LoginRequest loginRequest = new LoginRequest(TEST_USER_NAME, TEST_PASSWORD);

        assertThrowsExactly(UsernameNotFoundException.class,
                () -> sut.login(loginRequest));
    }


}