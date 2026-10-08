package com.evfinder.dto;

public record VehicleRequest(
    String make,
    String model,
    Integer batteryCapacityKwh,
    String connectorType,
    String ownerWalletAddress
) {}