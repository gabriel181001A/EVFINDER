package com.evfinder.security;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class BearerToken {

    private static final String PREFIX = "Bearer ";

    private BearerToken() {}

    // Extrai o token do cabeçalho "Authorization: Bearer <token>", ou responde 401
    public static String from(String authorizationHeader) {
        if (authorizationHeader == null
                || !authorizationHeader.regionMatches(true, 0, PREFIX, 0, PREFIX.length())
                || authorizationHeader.substring(PREFIX.length()).isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Entre com a sua carteira e envie o token no cabeçalho Authorization: Bearer <token>.");
        }
        return authorizationHeader.substring(PREFIX.length()).trim();
    }
}
