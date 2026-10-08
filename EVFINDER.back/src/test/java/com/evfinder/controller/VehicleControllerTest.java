package com.evfinder.controller;

import com.evfinder.dto.VehicleResponse;
import com.evfinder.exception.GlobalExceptionHandler;
import com.evfinder.service.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Valida o corpo do POST /api/v1/vehicles sem subir banco de dados
class VehicleControllerTest {

    private VehicleService vehicleService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        vehicleService = mock(VehicleService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new VehicleController(vehicleService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void criaVeiculoComDadosValidos() throws Exception {
        when(vehicleService.createVehicle(any()))
                .thenReturn(new VehicleResponse(1L, "BYD", "Dolphin", 44, "Type 2", "A1B2C3D4E5"));

        mockMvc.perform(post("/api/v1/vehicles").contentType(MediaType.APPLICATION_JSON).content("""
                        {"make": "BYD", "model": "Dolphin", "batteryCapacityKwh": 44,
                         "connectorType": "Type 2", "ownerWalletAddress": "A1B2C3D4E5"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void retorna400QuandoVeiculoNaoTemConector() throws Exception {
        mockMvc.perform(post("/api/v1/vehicles").contentType(MediaType.APPLICATION_JSON).content("""
                        {"make": "BYD", "model": "Dolphin", "connectorType": "  "}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Campos inválidos: connectorType é obrigatório"));

        verifyNoInteractions(vehicleService);
    }
}
