package ru.netology.cloudservice.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class JwtCodecConfig {

    private final SecretKey key;

    public JwtCodecConfig(
            @Value("${application.security.jwt.secret.file}") Resource jwtKeyResource,
            @Value("${application.security.jwt.algorithm:HS256}") String jwtAlgorithm
    ) throws IOException {
        try (InputStream inputStream = jwtKeyResource.getInputStream()) {
            byte[] jwtSecret = inputStream.readAllBytes();
            key = new SecretKeySpec(jwtSecret, jwtAlgorithm);
        }
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        JWKSource<SecurityContext> immutableSecret = new ImmutableSecret<>(key);
        return new NimbusJwtEncoder(immutableSecret);
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(key).build();
    }

}
