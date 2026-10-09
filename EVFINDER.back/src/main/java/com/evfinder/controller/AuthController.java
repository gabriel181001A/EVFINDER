package com.evfinder.controller;

import com.evfinder.dto.ChallengeRequest;
import com.evfinder.dto.ChallengeResponse;
import com.evfinder.dto.SessionResponse;
import com.evfinder.dto.VerifyRequest;
import com.evfinder.security.BearerToken;
import com.evfinder.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Login com a carteira Solana: pede o desafio, assina na carteira e troca a assinatura por um token
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/challenge")
    public ResponseEntity<ChallengeResponse> challenge(@Valid @RequestBody ChallengeRequest request) {
        return ResponseEntity.ok(authService.createChallenge(request.walletAddress()));
    }

    @PostMapping("/verify")
    public ResponseEntity<SessionResponse> verify(@Valid @RequestBody VerifyRequest request) {
        return ResponseEntity.ok(authService.verify(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        authService.logout(BearerToken.from(authorization));
        return ResponseEntity.noContent().build();
    }
}
