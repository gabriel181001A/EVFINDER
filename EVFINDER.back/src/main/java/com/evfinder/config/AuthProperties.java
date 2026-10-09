package com.evfinder.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

// Regras do login com a carteira, lidas das propriedades auth.* do application.properties
@ConfigurationProperties(prefix = "auth")
public record AuthProperties(
    Duration challengeDuration, // Tempo para assinar a mensagem depois de pedir o desafio
    Duration sessionDuration    // Validade do token de sessão
) {}
