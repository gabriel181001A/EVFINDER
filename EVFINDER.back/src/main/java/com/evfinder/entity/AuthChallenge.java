package com.evfinder.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// Mensagem que a carteira precisa assinar para entrar. Cada desafio vale uma única vez
@Entity
@Table(name = "auth_challenges")
public class AuthChallenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "wallet_address", nullable = false)
    private String walletAddress;

    @Column(nullable = false, unique = true)
    private String nonce;

    @Column(nullable = false, length = 500)
    private String message; // Texto exato que a carteira assina

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getWalletAddress() { return walletAddress; }
    public void setWalletAddress(String walletAddress) { this.walletAddress = walletAddress; }

    public String getNonce() { return nonce; }
    public void setNonce(String nonce) { this.nonce = nonce; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}
