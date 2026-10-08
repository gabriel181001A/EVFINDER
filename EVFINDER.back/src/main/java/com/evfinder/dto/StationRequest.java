package com.evfinder.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record StationRequest(
    @NotBlank(message = "é obrigatório")
    String name,

    @NotNull(message = "é obrigatório")
    @DecimalMin(value = "-90.0", message = "deve estar entre -90 e 90")
    @DecimalMax(value = "90.0", message = "deve estar entre -90 e 90")
    Double latitude,

    @NotNull(message = "é obrigatório")
    @DecimalMin(value = "-180.0", message = "deve estar entre -180 e 180")
    @DecimalMax(value = "180.0", message = "deve estar entre -180 e 180")
    Double longitude,

    String connectorType,

    @Positive(message = "deve ser maior que zero")
    Integer powerKw
) {}
