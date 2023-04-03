package ru.netology.cloudservice.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.netology.cloudservice.repository.UserRepository;
import ru.netology.cloudservice.service.impl.JpaUserDetailsServiceImpl;

@Configuration
public class UserDetailsConfig {

    @Bean
    @ConditionalOnMissingBean
    public UserDetailsService jpaUserDetailsService(UserRepository userRepository) {
        return new JpaUserDetailsServiceImpl(userRepository);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

}
