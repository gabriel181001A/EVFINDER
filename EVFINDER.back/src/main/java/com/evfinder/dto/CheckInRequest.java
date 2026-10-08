package com.evfinder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CheckInRequest(
    @NotNull(message = "é obrigatório")
    Long stationId,

    @NotBlank(message = "é obrigatório")
    String userWalletAddress
) {}
