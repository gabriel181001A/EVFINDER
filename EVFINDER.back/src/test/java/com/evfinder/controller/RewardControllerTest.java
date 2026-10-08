package com.evfinder.controller;

import com.evfinder.blockchain.TokenRewardService;
import com.evfinder.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Valida o corpo do POST /api/v1/rewards/check-in
class RewardControllerTest {

    private TokenRewardService rewardService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        rewardService = mock(TokenRewardService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new RewardController(rewardService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void fazCheckInComDadosValidos() throws Exception {
        when(rewardService.processReward("A1B2C3D4E5")).thenReturn("sol_tx_teste");

        mockMvc.perform(post("/api/v1/rewards/check-in").contentType(MediaType.APPLICATION_JSON).content("""
                        {"stationId": 123, "userWalletAddress": "A1B2C3D4E5"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.solanaTransactionHash").value("sol_tx_teste"));
    }

    @Test
    void retorna400QuandoFaltamCamposObrigatorios() throws Exception {
        mockMvc.perform(post("/api/v1/rewards/check-in").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Campos inválidos: stationId é obrigatório; userWalletAddress é obrigatório"));

        verifyNoInteractions(rewardService);
    }
}
