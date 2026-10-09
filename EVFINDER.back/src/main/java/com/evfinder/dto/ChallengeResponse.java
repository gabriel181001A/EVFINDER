package com.evfinder.dto;

import java.time.LocalDateTime;

public record ChallengeResponse(
    String walletAddress,
    String nonce,
    String message,          // Texto que a carteira deve assinar, sem alterar nada
    LocalDateTime expiresAt
) {}
