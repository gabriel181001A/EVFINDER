package com.evfinder.blockchain;

import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class TokenRewardService {

    public String processReward(String walletAddress) {
        // Simulação do tempo de resposta da rede Solana
        try {
            Thread.sleep(400); 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Retorna um hash falso para o frontend consumir por enquanto
        return "sol_tx_" + UUID.randomUUID().toString().replace("-", "");
    }
}