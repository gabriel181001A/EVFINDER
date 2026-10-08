package com.evfinder.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "check_ins", indexes = {
        // Acelera a busca do último check-in da carteira na estação
        @Index(name = "idx_check_ins_station_wallet", columnList = "station_id, user_wallet_address, created_at")
})
public class CheckIn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @Column(name = "user_wallet_address", nullable = false)
    private String userWalletAddress;

    // Posição do usuário no momento do check-in
    private Double latitude;
    private Double longitude;

    @Column(name = "tokens_awarded", nullable = false)
    private Double tokensAwarded;

    @Column(name = "transaction_hash")
    private String transactionHash; // Hash da transação na Solana (simulado por enquanto)

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Station getStation() { return station; }
    public void setStation(Station station) { this.station = station; }

    public String getUserWalletAddress() { return userWalletAddress; }
    public void setUserWalletAddress(String userWalletAddress) { this.userWalletAddress = userWalletAddress; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Double getTokensAwarded() { return tokensAwarded; }
    public void setTokensAwarded(Double tokensAwarded) { this.tokensAwarded = tokensAwarded; }

    public String getTransactionHash() { return transactionHash; }
    public void setTransactionHash(String transactionHash) { this.transactionHash = transactionHash; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
