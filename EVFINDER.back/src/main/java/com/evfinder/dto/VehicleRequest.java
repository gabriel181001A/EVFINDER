package com.evfinder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

// A carteira dona do veículo não vem no corpo: ela sai do token de sessão
public record VehicleRequest(
    @NotBlank(message = "é obrigatório")
    String make,

    @NotBlank(message = "é obrigatório")
    String model,

    @Positive(message = "deve ser maior que zero")
    Integer batteryCapacityKwh,

    // Obrigatório porque é com ele que a recomendação filtra as estações compatíveis
    @NotBlank(message = "é obrigatório")
    String connectorType
) {}
