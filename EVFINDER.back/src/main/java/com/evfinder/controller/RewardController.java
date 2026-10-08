package com.evfinder.controller;

import com.evfinder.blockchain.TokenRewardService;
import com.evfinder.dto.CheckInRequest;
import com.evfinder.dto.RewardResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/rewards")
public class RewardController {

    private final TokenRewardService rewardService;

    // Injeção de dependência via construtor
    public RewardController(TokenRewardService rewardService) {
        this.rewardService = rewardService;
    }

    @PostMapping("/check-in")
    public ResponseEntity<RewardResponse> checkIn(@Valid @RequestBody CheckInRequest request) {
        
        // Executa a lógica no serviço isolado
        String txHash = rewardService.processReward(request.userWalletAddress());
        
        // Monta a resposta usando o Record
        RewardResponse response = new RewardResponse(
                "SUCCESS",
                "Check-in confirmado. Tokens processados com sucesso!",
                5.0, // Valor fixo de exemplo
                txHash
        );
        
        return ResponseEntity.ok(response);
    }
}