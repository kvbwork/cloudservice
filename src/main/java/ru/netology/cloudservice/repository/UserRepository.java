package ru.netology.cloudservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.netology.cloudservice.entity.UserEntity;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    @Query("select u from UserEntity u join fetch u.authorities where u.username=:username")
    Optional<UserEntity> findByUsernameFetchAuthorities(String username);

    @Query("select u.id from UserEntity u where username=:username")
    Optional<Long> findIdByUsername(String username);

}
