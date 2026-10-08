package com.evfinder.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "stations")
public class Station {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private Double latitude;
    private Double longitude;

    @Column(name = "connector_type")
    private String connectorType; // ex: Type 2, CCS, CHAdeMO

    @Column(name = "power_kw")
    private Integer powerKw;

    private Boolean isOperational = true;

    // Getters e Setters
    // Dica: Se quiser usar o Lombok depois, basta colocar um @Data na classe e apagar isso
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getConnectorType() { return connectorType; }
    public void setConnectorType(String connectorType) { this.connectorType = connectorType; }

    public Integer getPowerKw() { return powerKw; }
    public void setPowerKw(Integer powerKw) { this.powerKw = powerKw; }

    public Boolean getOperational() { return isOperational; }
    public void setOperational(Boolean operational) { isOperational = operational; }
}