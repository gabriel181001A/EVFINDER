package com.evfinder.service;

import com.evfinder.dto.OcmStationDto;
import com.evfinder.entity.Vehicle;
import com.evfinder.exception.ResourceNotFoundException;
import com.evfinder.integration.OpenChargeClient;
import com.evfinder.repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecommendationService {

    private final VehicleRepository vehicleRepository;
    private final OpenChargeClient openChargeClient;

    public RecommendationService(VehicleRepository vehicleRepository, OpenChargeClient openChargeClient) {
        this.vehicleRepository = vehicleRepository;
        this.openChargeClient = openChargeClient;
    }

    public List<OcmStationDto> recommendStations(Long vehicleId, Double currentLat, Double currentLng, Double radiusKm) {
        
        // 1. Busca o carro no banco de dados local
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado. ID: " + vehicleId));

        // 2. Busca todas as estações reais na localização informada
        List<OcmStationDto> nearbyStations = openChargeClient.fetchStationsNearLocation(currentLat, currentLng, radiusKm);

        // 3. A MÁGICA: Filtra apenas os postos que têm a mesma ficha do carro
        String carConnector = vehicle.getConnectorType(); // ex: "Type 2"

        List<OcmStationDto> compatibleStations = nearbyStations.stream()
            .filter(station -> station.connections() != null && station.connections().stream()
                .anyMatch(conn -> conn.connectionType() != null && 
                                  conn.connectionType().title() != null && 
                                  conn.connectionType().title().toLowerCase().contains(carConnector.toLowerCase())))
            .toList();

        return compatibleStations;
    }
}