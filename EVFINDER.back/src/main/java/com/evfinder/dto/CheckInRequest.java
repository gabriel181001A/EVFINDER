package com.evfinder.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

// A carteira não vem no corpo: ela sai do token de sessão (veja AuthController)
public record CheckInRequest(
    @NotNull(message = "é obrigatório")
    Long stationId,

    // Posição atual do usuário, usada para conferir se ele está perto da estação
    @NotNull(message = "é obrigatório")
    @DecimalMin(value = "-90.0", message = "deve estar entre -90 e 90")
    @DecimalMax(value = "90.0", message = "deve estar entre -90 e 90")
    Double latitude,

    @NotNull(message = "é obrigatório")
    @DecimalMin(value = "-180.0", message = "deve estar entre -180 e 180")
    @DecimalMax(value = "180.0", message = "deve estar entre -180 e 180")
    Double longitude
) {}
