package ru.netology.cloudservice.config;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionException;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import ru.netology.cloudservice.service.TokenRegistrar;

import javax.crypto.spec.SecretKeySpec;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class OpaqueTokenIntrospectorTest {
    private static final String TEST_USER_NAME = "test_user";
    private static final String TEST_VALID_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0X3VzZXIiLCJleHAiOjE2ODA1NDQ0NzEsImlhdCI6MTY4MDQ1ODA3MSwic2NvcGUiOiJST0xFX1VTRVIifQ.iU7F40ilWaAflhjAOQ2J7SrTdsc-4I4aWC3ANlkcaPg";
    private static final String TEST_WRONG_SIGN_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0X3VzZXIiLCJleHAiOjE2ODA1NDUxNDEsImlhdCI6MTY4MDQ1ODc0MSwic2NvcGUiOiJST0xFX1VTRVIifQ.TwHxDVZCAioAMNn7sO2cJ1S-Zkaxe15B4FAAFfTScQ4";
    private static final String TEST_EMPTY_SIGN_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0X3VzZXIiLCJleHAiOjE2ODA1NDUxNDEsImlhdCI6MTY4MDQ1ODc0MSwic2NvcGUiOiJST0xFX1VTRVIifQ";
    private static final String JWT_SECRET = "6d735d1f-fdf2-46df-85d1-2e011ff5ed10";
    private static final String JWT_ALGORITHM = "HS256";

    private static JwtDecoder jwtDecoder;

    @Mock
    private TokenRegistrar tokenRegistrar;

    private OpaqueTokenIntrospector sut;

    @BeforeAll
    static void beforeAll() {
        var secretKey = new SecretKeySpec(JWT_SECRET.getBytes(), JWT_ALGORITHM);
        jwtDecoder = NimbusJwtDecoder.withSecretKey(secretKey).build();
    }

    @BeforeEach
    void setUp() {
        sut = new WebSecurityConfig().opaqueTokenIntrospector(jwtDecoder, tokenRegistrar);
    }

    @Test
    @DisplayName("introspect() успешное распознавание зарегистрированного токена")
    void opaqueTokenIntrospector_registered_success() {
        Mockito.when(tokenRegistrar.isRegistered(TEST_VALID_TOKEN)).thenReturn(true);
        var auth2AuthenticatedPrincipal = sut.introspect(TEST_VALID_TOKEN);
        assertThat(auth2AuthenticatedPrincipal.getName(), equalTo(TEST_USER_NAME));
    }

    @Test
    @DisplayName("introspect() ошибка токен не зарегистрирован")
    void opaqueTokenIntrospector_not_registered_failure() {
        Mockito.when(tokenRegistrar.isRegistered(anyString())).thenReturn(false);
        Assertions.assertThrows(OAuth2IntrospectionException.class, () ->
                sut.introspect(TEST_VALID_TOKEN));
    }

    @Test
    @DisplayName("introspect() ошибка декодирования токена")
    void opaqueTokenIntrospector_decoding_failure() {
        Mockito.when(tokenRegistrar.isRegistered(anyString())).thenReturn(true);
        Assertions.assertThrows(BadJwtException.class, () ->
                sut.introspect("already_registered_?_random_text_string"));
    }

    @Test
    @DisplayName("introspect() ошибка проверки подписи токена")
    void opaqueTokenIntrospector_sign_verify_failure() {
        Mockito.when(tokenRegistrar.isRegistered(anyString())).thenReturn(true);
        Assertions.assertThrows(BadJwtException.class, () ->
                sut.introspect(TEST_WRONG_SIGN_TOKEN));
    }

    @Test
    @DisplayName("introspect() ошибка проверки токена без подписи")
    void opaqueTokenIntrospector_empty_sign_verify_failure() {
        Mockito.when(tokenRegistrar.isRegistered(anyString())).thenReturn(true);
        Assertions.assertThrows(BadJwtException.class, () ->
                sut.introspect(TEST_EMPTY_SIGN_TOKEN));
    }
}