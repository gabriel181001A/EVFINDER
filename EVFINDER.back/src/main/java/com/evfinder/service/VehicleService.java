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

    public VehicleResponse createVehicle(VehicleRequest request) {
        Vehicle vehicle = new Vehicle();
        vehicle.setMake(request.make());
        vehicle.setModel(request.model());
        vehicle.setBatteryCapacityKwh(request.batteryCapacityKwh());
        vehicle.setConnectorType(request.connectorType());
        vehicle.setOwnerWalletAddress(request.ownerWalletAddress());

        Vehicle savedVehicle = repository.save(vehicle);
        return mapToResponse(savedVehicle);
    }

    public List<VehicleResponse> getAllVehicles() {
        return repository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Busca específica para a integração Web3
    public List<VehicleResponse> getVehiclesByWallet(String walletAddress) {
        return repository.findByOwnerWalletAddress(walletAddress)
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