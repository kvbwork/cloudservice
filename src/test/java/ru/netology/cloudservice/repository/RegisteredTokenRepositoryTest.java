package ru.netology.cloudservice.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.netology.cloudservice.entity.RegisteredToken;

import java.time.Instant;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@DataJpaTest
class RegisteredTokenRepositoryTest {
    private static final String TEST_TOKEN = "testtokenvalue";
    private static final String TEST_LOGIN = "testlogin";
    private static final Instant TEST_ISSUED_AT = Instant.now();
    private static final Instant TEST_EXPIRES_AT = Instant.now().plusSeconds(60);

    @Autowired
    RegisteredTokenRepository sut;

    RegisteredToken tokenEntity;

    @BeforeEach
    void setUp() {
        tokenEntity = sut.save(
                new RegisteredToken(TEST_TOKEN, TEST_LOGIN, TEST_ISSUED_AT, TEST_EXPIRES_AT)
        );
    }

    @AfterEach
    void tearDown() {
        sut.deleteById(tokenEntity.getToken());
    }

    @Test
    void findFirstByLoginAndToken() {
        var resultEntity = sut.findFirstByLoginAndToken(TEST_LOGIN, TEST_TOKEN).orElseThrow();
        assertThat(resultEntity.getToken(), equalTo(TEST_TOKEN));
        assertThat(resultEntity.getLogin(), equalTo(TEST_LOGIN));
        assertThat(resultEntity.getIssuedAt(), equalTo(TEST_ISSUED_AT));
        assertThat(resultEntity.getExpiresAt(), equalTo(TEST_EXPIRES_AT));
    }
}