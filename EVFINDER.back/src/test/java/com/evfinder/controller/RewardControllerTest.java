package com.evfinder.controller;

import com.evfinder.dto.RewardResponse;
import com.evfinder.exception.GlobalExceptionHandler;
import com.evfinder.service.CheckInService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Valida o corpo do POST /api/v1/rewards/check-in e os status de erro das regras de negócio
class RewardControllerTest {

    private static final String VALID_BODY = """
            {"stationId": 1, "userWalletAddress": "A1B2C3D4E5", "latitude": -23.5613, "longitude": -46.6565}
            """;

    private CheckInService checkInService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        checkInService = mock(CheckInService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new RewardController(checkInService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void fazCheckInComDadosValidos() throws Exception {
        when(checkInService.checkIn(any()))
                .thenReturn(new RewardResponse("SUCCESS", "Check-in confirmado.", 5.0, "sol_tx_teste"));

        mockMvc.perform(post("/api/v1/rewards/check-in").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokensAwarded").value(5.0))
                .andExpect(jsonPath("$.solanaTransactionHash").value("sol_tx_teste"));
    }

    @Test
    void retorna400QuandoFaltamCamposObrigatorios() throws Exception {
        mockMvc.perform(post("/api/v1/rewards/check-in").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Campos inválidos: latitude é obrigatório; "
                        + "longitude é obrigatório; stationId é obrigatório; userWalletAddress é obrigatório"));

        verifyNoInteractions(checkInService);
    }

    @Test
    void retorna400QuandoCoordenadasEstaoForaDoIntervalo() throws Exception {
        mockMvc.perform(post("/api/v1/rewards/check-in").contentType(MediaType.APPLICATION_JSON).content("""
                        {"stationId": 1, "userWalletAddress": "A1B2C3D4E5", "latitude": 100, "longitude": -200}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("latitude deve estar entre -90 e 90")))
                .andExpect(jsonPath("$.message").value(containsString("longitude deve estar entre -180 e 180")));

        verifyNoInteractions(checkInService);
    }

    @Test
    void retorna409QuandoCheckInEhRepetido() throws Exception {
        when(checkInService.checkIn(any())).thenThrow(new ResponseStatusException(HttpStatus.CONFLICT,
                "Você já fez check-in nesta estação. Tente novamente em 23h00min."));

        mockMvc.perform(post("/api/v1/rewards/check-in").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Você já fez check-in nesta estação. Tente novamente em 23h00min."));
    }

    @Test
    void retorna422QuandoUsuarioEstaLongeDaEstacao() throws Exception {
        when(checkInService.checkIn(any())).thenThrow(new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT,
                "Você está a 300 m da estação. O check-in só é permitido a até 200 m."));

        mockMvc.perform(post("/api/v1/rewards/check-in").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Você está a 300 m da estação. O check-in só é permitido a até 200 m."));
    }
}
