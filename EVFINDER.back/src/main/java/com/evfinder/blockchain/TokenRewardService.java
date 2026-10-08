package com.evfinder.blockchain;

import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class TokenRewardService {

    // Envia os tokens para a carteira. Por enquanto a transação na Solana é simulada,
    // então o valor ainda não é usado
    public String processReward(String walletAddress, double tokens) {
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
