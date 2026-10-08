package com.evfinder.dto;

public record VehicleResponse(
    Long id,
    String make,
    String model,
    Integer batteryCapacityKwh,
    String connectorType,
    String ownerWalletAddress
) {}