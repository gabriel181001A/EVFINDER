package com.evfinder.dto;

public record CheckInRequest(
    Long stationId,
    String userWalletAddress
) {}