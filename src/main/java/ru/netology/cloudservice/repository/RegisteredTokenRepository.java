package ru.netology.cloudservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.netology.cloudservice.entity.RegisteredToken;

import java.util.Optional;

@Repository
public interface RegisteredTokenRepository extends JpaRepository<RegisteredToken, String> {
    Optional<RegisteredToken> findFirstByLoginAndToken(String login, String token);
}
