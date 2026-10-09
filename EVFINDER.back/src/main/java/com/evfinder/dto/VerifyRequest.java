package com.evfinder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyRequest(
    @NotBlank(message = "é obrigatório")
    @Size(max = 64, message = "deve ter no máximo 64 caracteres")
    String walletAddress,

    @NotBlank(message = "é obrigatório")
    @Size(max = 64, message = "deve ter no máximo 64 caracteres")
    String nonce,

    // Assinatura da mensagem do desafio, em Base58
    @NotBlank(message = "é obrigatório")
    @Size(max = 128, message = "deve ter no máximo 128 caracteres")
    String signature
) {}
