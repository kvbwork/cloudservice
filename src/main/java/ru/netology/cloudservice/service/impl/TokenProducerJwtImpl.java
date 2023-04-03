package ru.netology.cloudservice.service.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import ru.netology.cloudservice.service.TokenProducer;

import java.time.Instant;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.HOURS;

@Service
@Getter
@Setter
@RequiredArgsConstructor
public class TokenProducerJwtImpl implements TokenProducer {

    @Value("${application.security.jwt.algorithm:HS256}")
    private String algorithm;

    @Value("${application.security.jwt.valid-hours:24}")
    private long validHours;

    private final JwtEncoder jwtEncoder;

    @Override
    public OAuth2Token generateToken(UserDetails userDetails) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(validHours, HOURS);
        String scope = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(userDetails.getUsername())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim("scope", scope)
                .build();

        JwsHeader jwsHeader = JwsHeader.with(() -> algorithm).build();
        String tokenValue = jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();

        return new AbstractOAuth2Token(tokenValue, issuedAt, expiresAt) {
        };
    }

}
