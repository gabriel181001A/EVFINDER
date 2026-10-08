package com.evfinder.dto;

public record StationResponse(
    Long id,
    String name,
    Double latitude,
    Double longitude,
    String connectorType,
    Integer powerKw,
    Boolean isOperational
) {}