package ru.netology.cloudservice.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.OAuth2Token;

public interface TokenProducer {
    OAuth2Token generateToken(UserDetails userDetails);

}
