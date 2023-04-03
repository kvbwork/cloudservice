package ru.netology.cloudservice.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.netology.cloudservice.entity.UserEntity;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@DataJpaTest
class UserRepositoryTest {
    private static final String TEST_USER_NAME = "test_user";
    private static final String TEST_USER_PASSWORD = "{noop}123";
    private static final boolean TEST_USER_ENABLED = true;
    private static final Set<String> TEST_USER_AUTHORITIES = Set.of("ROLE_USER", "TEST_READ", "TEST_WRITE");

    @Autowired
    private UserRepository sut;

    UserEntity userEntity;

    @BeforeEach
    void setUp() {
        userEntity = sut.save(
                new UserEntity(0, TEST_USER_NAME, TEST_USER_PASSWORD, TEST_USER_ENABLED, TEST_USER_AUTHORITIES)
        );
    }

    @AfterEach
    void tearDown() {
        sut.deleteById(userEntity.getId());
    }

    @Test
    void findByUsernameFetchAuthorities() {
        var resultEntity = sut.findByUsernameFetchAuthorities(TEST_USER_NAME).orElseThrow();
        assertThat(resultEntity.getId(), is(userEntity.getId()));
        assertThat(resultEntity.getUsername(), is(TEST_USER_NAME));
        assertThat(resultEntity.getAuthorities().equals(TEST_USER_AUTHORITIES), is(true));
    }

    @Test
    void findIdByUsername_success() {
        var resultEntity = sut.findIdByUsername(TEST_USER_NAME).orElseThrow();
        assertThat(resultEntity, is(userEntity.getId()));
    }
}