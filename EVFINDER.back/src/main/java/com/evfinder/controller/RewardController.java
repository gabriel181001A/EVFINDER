package com.evfinder.controller;

import com.evfinder.dto.CheckInRequest;
import com.evfinder.dto.RewardResponse;
import com.evfinder.security.AuthenticatedWallet;
import com.evfinder.service.CheckInService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/rewards")
public class RewardController {

    private final CheckInService checkInService;

    // Injeção de dependência via construtor
    public RewardController(CheckInService checkInService) {
        this.checkInService = checkInService;
    }

    // Exige login: a carteira que recebe os tokens vem do token de sessão, não do corpo.
    // Regras (distância, intervalo entre check-ins, tokens) ficam no CheckInService
    @PostMapping("/check-in")
    public ResponseEntity<RewardResponse> checkIn(@AuthenticatedWallet String walletAddress,
                                                  @Valid @RequestBody CheckInRequest request) {
        return ResponseEntity.ok(checkInService.checkIn(walletAddress, request));
    }
}
