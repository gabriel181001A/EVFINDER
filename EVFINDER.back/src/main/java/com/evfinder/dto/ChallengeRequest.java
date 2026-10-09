package com.evfinder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChallengeRequest(
    @NotBlank(message = "é obrigatório")
    @Size(max = 64, message = "deve ter no máximo 64 caracteres")
    String walletAddress
) {}
