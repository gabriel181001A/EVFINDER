package com.evfinder.service;

import com.evfinder.dto.OcmStationDto;
import com.evfinder.entity.Vehicle;
import com.evfinder.exception.ResourceNotFoundException;
import com.evfinder.integration.OpenChargeClient;
import com.evfinder.repository.VehicleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class RecommendationService {

    private final VehicleRepository vehicleRepository;
    private final OpenChargeClient openChargeClient;

    public RecommendationService(VehicleRepository vehicleRepository, OpenChargeClient openChargeClient) {
        this.vehicleRepository = vehicleRepository;
        this.openChargeClient = openChargeClient;
    }

    public List<OcmStationDto> recommendStations(String walletAddress, Long vehicleId, Double currentLat, Double currentLng, Double radiusKm) {

        // 1. Busca o carro no banco de dados local. Veículo de outra carteira recebe o mesmo 404
        // de um veículo inexistente, para não revelar quais IDs existem
        Vehicle vehicle = vehicleRepository.findByIdAndOwnerWalletAddress(vehicleId, walletAddress)
                .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado. ID: " + vehicleId));

        // Sem conector não há como filtrar, então nem chama a API externa
        String carConnector = vehicle.getConnectorType(); // ex: "Type 2"
        if (carConnector == null || carConnector.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Veículo " + vehicleId + " não tem tipo de conector cadastrado.");
        }

        // 2. Busca todas as estações reais na localização informada
        List<OcmStationDto> nearbyStations = openChargeClient.fetchStationsNearLocation(currentLat, currentLng, radiusKm);

        // 3. A MÁGICA: Filtra apenas os postos que têm a mesma ficha do carro

        List<OcmStationDto> compatibleStations = nearbyStations.stream()
            .filter(station -> station.connections() != null && station.connections().stream()
                .anyMatch(conn -> conn.connectionType() != null && 
                                  conn.connectionType().title() != null && 
                                  conn.connectionType().title().toLowerCase().contains(carConnector.toLowerCase())))
            .toList();

        return compatibleStations;
    }
}