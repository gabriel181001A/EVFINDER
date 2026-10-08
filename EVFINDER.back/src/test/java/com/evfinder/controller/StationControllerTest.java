package com.evfinder.controller;

import com.evfinder.dto.StationResponse;
import com.evfinder.exception.GlobalExceptionHandler;
import com.evfinder.integration.OpenChargeClient;
import com.evfinder.service.StationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Valida o corpo do POST /api/v1/stations sem subir banco de dados
class StationControllerTest {

    private StationService stationService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        stationService = mock(StationService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new StationController(stationService, mock(OpenChargeClient.class)))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void criaEstacaoComDadosValidos() throws Exception {
        when(stationService.createStation(any()))
                .thenReturn(new StationResponse(1L, "Supercharger Central", -22.9, -47.0, "Type 2", 150, true));

        mockMvc.perform(post("/api/v1/stations").contentType(MediaType.APPLICATION_JSON).content("""
                        {"name": "Supercharger Central", "latitude": -22.9, "longitude": -47.0,
                         "connectorType": "Type 2", "powerKw": 150}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void retorna400QuandoFaltamCamposObrigatorios() throws Exception {
        mockMvc.perform(post("/api/v1/stations").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Campos inválidos: latitude é obrigatório; longitude é obrigatório; name é obrigatório"));

        verifyNoInteractions(stationService);
    }

    @Test
    void retorna400QuandoCoordenadasEstaoForaDoIntervalo() throws Exception {
        mockMvc.perform(post("/api/v1/stations").contentType(MediaType.APPLICATION_JSON).content("""
                        {"name": "Posto", "latitude": 100, "longitude": -200, "powerKw": 0}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("latitude deve estar entre -90 e 90")))
                .andExpect(jsonPath("$.message").value(containsString("longitude deve estar entre -180 e 180")))
                .andExpect(jsonPath("$.message").value(containsString("powerKw deve ser maior que zero")));
    }
}
