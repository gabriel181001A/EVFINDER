package com.evfinder.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "vehicles")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String make; // Ex: Tesla, Nissan, BYD

    @Column(nullable = false)
    private String model;

    @Column(name = "battery_capacity_kwh")
    private Integer batteryCapacityKwh; // Capacidade total da bateria

    @Column(name = "connector_type")
    private String connectorType; // Para cruzar com o tipo da Station

    @Column(name = "owner_wallet_address")
    private String ownerWalletAddress; // Vincula o carro à carteira da Solana do usuário

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMake() { return make; }
    public void setMake(String make) { this.make = make; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public Integer getBatteryCapacityKwh() { return batteryCapacityKwh; }
    public void setBatteryCapacityKwh(Integer batteryCapacityKwh) { this.batteryCapacityKwh = batteryCapacityKwh; }

    public String getConnectorType() { return connectorType; }
    public void setConnectorType(String connectorType) { this.connectorType = connectorType; }

    public String getOwnerWalletAddress() { return ownerWalletAddress; }
    public void setOwnerWalletAddress(String ownerWalletAddress) { this.ownerWalletAddress = ownerWalletAddress; }
}