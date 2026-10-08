package com.evfinder.controller;

import com.evfinder.dto.VehicleRequest;
import com.evfinder.dto.VehicleResponse;
import com.evfinder.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PostMapping
    public ResponseEntity<VehicleResponse> create(@Valid @RequestBody VehicleRequest request) {
        VehicleResponse response = vehicleService.createVehicle(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<VehicleResponse>> getAll() {
        return ResponseEntity.ok(vehicleService.getAllVehicles());
    }

    // Rota essencial para o Frontend carregar a garagem do utilizador conectado com a Phantom Wallet
    @GetMapping("/wallet/{walletAddress}")
    public ResponseEntity<List<VehicleResponse>> getByWallet(@PathVariable String walletAddress) {
        return ResponseEntity.ok(vehicleService.getVehiclesByWallet(walletAddress));
    }
}