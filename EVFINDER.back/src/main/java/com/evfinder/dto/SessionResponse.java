package com.evfinder.dto;

import java.time.LocalDateTime;

public record SessionResponse(
    String token,            // Enviar em "Authorization: Bearer <token>"
    String walletAddress,
    LocalDateTime expiresAt
) {}
