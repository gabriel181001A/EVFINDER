package com.evfinder.dto;

public record StationRequest(
    String name,
    Double latitude,
    Double longitude,
    String connectorType,
    Integer powerKw
) {}