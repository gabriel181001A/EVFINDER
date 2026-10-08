package com.evfinder.dto;

public record RewardResponse(
    String status,
    String message,
    Double tokensAwarded,
    String solanaTransactionHash
) {}