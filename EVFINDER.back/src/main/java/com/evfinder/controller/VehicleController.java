package com.evfinder.controller;

import com.evfinder.dto.VehicleRequest;
import com.evfinder.dto.VehicleResponse;
import com.evfinder.security.AuthenticatedWallet;
import com.evfinder.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Todas as rotas exigem login: cada usuário só cadastra e vê os veículos da própria carteira
@RestController
@RequestMapping("/api/v1/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PostMapping
    public ResponseEntity<VehicleResponse> create(@AuthenticatedWallet String walletAddress,
                                                  @Valid @RequestBody VehicleRequest request) {
        VehicleResponse response = vehicleService.createVehicle(walletAddress, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Garagem do usuário conectado com a carteira
    @GetMapping
    public ResponseEntity<List<VehicleResponse>> getMine(@AuthenticatedWallet String walletAddress) {
        return ResponseEntity.ok(vehicleService.getVehiclesByOwner(walletAddress));
    }
}
