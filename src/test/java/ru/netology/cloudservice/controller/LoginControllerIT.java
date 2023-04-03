package ru.netology.cloudservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ru.netology.cloudservice.model.request.LoginRequest;
import ru.netology.cloudservice.service.TokenProducer;
import ru.netology.cloudservice.service.TokenRegistrar;

import javax.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
class LoginControllerIT {
    private static final String TEST_LOGIN = "TESTLOGIN";
    private static final String TEST_PASSWORD = "TESTPASSWORD";
    private static final String TEST_ROLE = "USER";
    private static final String TOKEN_HEADER = "auth-token";
    private static final String TEST_TOKEN = "testtokenvalue";
    private static final UserDetails TEST_USER_DETAILS = User.builder()
            .username(TEST_LOGIN)
            .password("{noop}" + TEST_PASSWORD)
            .roles(TEST_ROLE)
            .build();

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private TokenRegistrar tokenRegistrar;

    @MockBean
    private BearerTokenResolver bearerTokenResolver;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TokenProducer tokenProducer;

    @Test
    @DisplayName("POST /login возвращает auth-token")
    public void post_login_success() throws Exception {
        String path = "/login";
        String body = objectMapper.writeValueAsString(new LoginRequest(TEST_LOGIN, TEST_PASSWORD));

        when(userDetailsService.loadUserByUsername(TEST_LOGIN)).thenReturn(TEST_USER_DETAILS);
        when(tokenRegistrar.register(any(UserDetails.class))).thenReturn(TEST_TOKEN);

        mockMvc.perform(post(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.auth-token").value(TEST_TOKEN))
                .andDo(print());
    }

    @Test
    @DisplayName("POST /login ошибка пользователь не найден 400")
    public void post_login_not_found_failure() throws Exception {
        String path = "/login";
        String body = objectMapper.writeValueAsString(new LoginRequest(TEST_LOGIN, TEST_PASSWORD));

        when(userDetailsService.loadUserByUsername(TEST_LOGIN))
                .thenThrow(UsernameNotFoundException.class);

        mockMvc.perform(post(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("POST /login ошибка тела запроса 400")
    public void post_login_bad_request_error() throws Exception {
        String path = "/login";

        mockMvc.perform(post(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("POST /login ошибка login пустой 400")
    public void post_login_empty_login_bad_request_error() throws Exception {
        String path = "/login";
        String body = objectMapper.writeValueAsString(new LoginRequest("", TEST_PASSWORD));

        mockMvc.perform(post(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("POST /login ошибка password пустой 400")
    public void post_login_empty_password_bad_request_error() throws Exception {
        String path = "/login";
        String body = objectMapper.writeValueAsString(new LoginRequest(TEST_LOGIN, ""));

        mockMvc.perform(post(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("POST /logout ошибка авторизации 401")
    public void post_logout_anon_failure() throws Exception {
        String path = "/logout";
        mockMvc.perform(post(path))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /logout успешная очистка контекста")
    public void logout_success() throws Exception {
        String path = "/logout";
        Authentication authBeforeLogout = SecurityContextHolder.getContext().getAuthentication();

        mockMvc.perform(post(path))
                .andExpect(status().isOk())
                .andDo(print());

        Authentication authAfterLogout = SecurityContextHolder.getContext().getAuthentication();

        assertTrue(authBeforeLogout.isAuthenticated());
        assertNull(authAfterLogout);
    }

    @Test
    @WithMockUser(username = TEST_LOGIN)
    @DisplayName("POST /logout успешно отзывает токен")
    public void logout_revoke_token_success() throws Exception {
        String path = "/logout";
        String testToken = tokenProducer.generateToken(TEST_USER_DETAILS).getTokenValue();

        when(bearerTokenResolver.resolve(any(HttpServletRequest.class))).thenReturn(testToken);
        when(tokenRegistrar.isRegistered(testToken)).thenReturn(true);

        mockMvc.perform(post(path)
                        .header(TOKEN_HEADER, testToken))
                .andExpect(status().isOk())
                .andDo(print());

        verify(tokenRegistrar, times(1)).revoke(TEST_LOGIN, testToken);
    }
}