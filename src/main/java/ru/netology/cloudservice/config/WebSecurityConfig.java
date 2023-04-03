package ru.netology.cloudservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionException;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.HandlerExceptionResolver;
import ru.netology.cloudservice.service.TokenRegistrar;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            HandlerExceptionResolver handlerExceptionResolver
    ) throws Exception {
        http.cors();
        http.csrf().disable();
        http.logout().disable();

        http.sessionManagement()
                .sessionCreationPolicy(STATELESS);

        http.oauth2ResourceServer()
                .opaqueToken();

        http.authorizeRequests()
                .antMatchers(HttpMethod.POST, "/login").permitAll()
                .anyRequest().authenticated();

        http.exceptionHandling()
                .authenticationEntryPoint((request, response, authException) ->
                        handlerExceptionResolver.resolveException(request, response, null, authException));

        return http.build();
    }

    @Bean
    public OpaqueTokenIntrospector opaqueTokenIntrospector(JwtDecoder jwtDecoder, TokenRegistrar tokenRegistrar) {
        return token -> {
            if (!tokenRegistrar.isRegistered(token)) {
                throw new OAuth2IntrospectionException("Token is not registered.");
            }
            Jwt jwt = jwtDecoder.decode(token);
            List<GrantedAuthority> authoritiesList = jwt.getClaimAsStringList("scope")
                    .stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
            return new DefaultOAuth2User(authoritiesList, jwt.getClaims(), "sub");
        };
    }

    @Bean
    @ConditionalOnProperty("application.security.token.header")
    public BearerTokenResolver customBearerTokenResolver(
            @Value("${application.security.token.header}") String tokenBearerHeader
    ) {
        DefaultBearerTokenResolver defaultBearerTokenResolver = new DefaultBearerTokenResolver();
        defaultBearerTokenResolver.setBearerTokenHeaderName(tokenBearerHeader);
        return defaultBearerTokenResolver;
    }

}
