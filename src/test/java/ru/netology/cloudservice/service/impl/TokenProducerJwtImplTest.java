package ru.netology.cloudservice.service.impl;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.UUID;

import static java.time.Instant.now;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class TokenProducerJwtImplTest {
    private static final String jwtSecret = UUID.randomUUID().toString();
    private static final String jwtAlgorithm = "HS256";
    private static final long jwtValidHours = 24L;

    private static final UserDetails TEST_USER = User.builder()
            .username("test_user")
            .password("{noop}test_password")
            .roles("USER")
            .build();

    private static JwtEncoder jwtEncoder;
    private static JwtDecoder jwtDecoder;
    private TokenProducerJwtImpl sut;

    @BeforeAll
    static void beforeAll() {
        SecretKey key = new SecretKeySpec(jwtSecret.getBytes(), jwtAlgorithm);
        JWKSource<SecurityContext> immutableSecret = new ImmutableSecret<>(key);
        jwtEncoder = new NimbusJwtEncoder(immutableSecret);
        jwtDecoder = NimbusJwtDecoder.withSecretKey(key).build();
    }

    @BeforeEach
    void setUp() {
        sut = new TokenProducerJwtImpl(jwtEncoder);
        sut.setAlgorithm(jwtAlgorithm);
        sut.setValidHours(jwtValidHours);
    }

    @Test
    @DisplayName("generateToken() успешно возвращает OAuth2Token")
    void generateToken_success() {
        var oauth2Token = sut.generateToken(TEST_USER);
        assertThat(oauth2Token.getTokenValue(), not(blankOrNullString()));
        assertThat(now().isAfter(oauth2Token.getIssuedAt()), is(true));
        assertThat(now().isBefore(oauth2Token.getExpiresAt()), is(true));
    }

    @Test
    @DisplayName("generateToken() возвращенный JWT декодируется успешно")
    void generateToken_jwt_can_be_decoded() {
        var oauth2Token = sut.generateToken(TEST_USER);
        var jwt = jwtDecoder.decode(oauth2Token.getTokenValue());
        assertThat(jwt.getSubject(), equalTo(TEST_USER.getUsername()));
        assertThat(jwt.getClaimAsStringList("scope"), hasItem("ROLE_USER"));
    }
}