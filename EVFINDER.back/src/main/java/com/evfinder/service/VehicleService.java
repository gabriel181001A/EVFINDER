package com.evfinder.service;

import com.evfinder.dto.VehicleRequest;
import com.evfinder.dto.VehicleResponse;
import com.evfinder.entity.Vehicle;
import com.evfinder.repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VehicleService {

    private final VehicleRepository repository;

    public VehicleService(VehicleRepository repository) {
        this.repository = repository;
    }

    // ownerWalletAddress é a carteira autenticada pelo token de sessão
    public VehicleResponse createVehicle(String ownerWalletAddress, VehicleRequest request) {
        Vehicle vehicle = new Vehicle();
        vehicle.setMake(request.make());
        vehicle.setModel(request.model());
        vehicle.setBatteryCapacityKwh(request.batteryCapacityKwh());
        vehicle.setConnectorType(request.connectorType());
        vehicle.setOwnerWalletAddress(ownerWalletAddress);

        Vehicle savedVehicle = repository.save(vehicle);
        return mapToResponse(savedVehicle);
    }

    // Garagem do usuário logado: só os veículos da própria carteira
    public List<VehicleResponse> getVehiclesByOwner(String ownerWalletAddress) {
        return repository.findByOwnerWalletAddress(ownerWalletAddress)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private VehicleResponse mapToResponse(Vehicle vehicle) {
        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getMake(),
                vehicle.getModel(),
                vehicle.getBatteryCapacityKwh(),
                vehicle.getConnectorType(),
                vehicle.getOwnerWalletAddress()
        );
    }
}
