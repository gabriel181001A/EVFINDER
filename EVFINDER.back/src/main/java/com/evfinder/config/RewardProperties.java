package com.evfinder.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

// Regras do check-in, lidas das propriedades rewards.* do application.properties
@ConfigurationProperties(prefix = "rewards")
public record RewardProperties(
    double tokensPerCheckIn,         // Tokens concedidos a cada check-in
    Duration checkInCooldown,        // Intervalo mínimo entre check-ins da mesma carteira na mesma estação
    double maxCheckInDistanceMeters  // Distância máxima entre o usuário e a estação
) {}
