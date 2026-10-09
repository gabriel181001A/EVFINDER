package com.evfinder.controller;

import com.evfinder.dto.VehicleRequest;
import com.evfinder.dto.VehicleResponse;
import com.evfinder.exception.GlobalExceptionHandler;
import com.evfinder.security.AuthenticatedWalletResolver;
import com.evfinder.service.AuthService;
import com.evfinder.service.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Valida o login exigido e o corpo das rotas de /api/v1/vehicles sem subir banco de dados
class VehicleControllerTest {

    private static final String AUTH = "Bearer token-valido";

    private VehicleService vehicleService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        vehicleService = mock(VehicleService.class);
        AuthService authService = mock(AuthService.class);
        when(authService.authenticate("token-valido")).thenReturn("WALLET1");

        mockMvc = MockMvcBuilders.standaloneSetup(new VehicleController(vehicleService))
                .setCustomArgumentResolvers(new AuthenticatedWalletResolver(authService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void criaVeiculoNaCarteiraDoToken() throws Exception {
        when(vehicleService.createVehicle(eq("WALLET1"), any()))
                .thenReturn(new VehicleResponse(1L, "BYD", "Dolphin", 44, "Type 2", "WALLET1"));

        mockMvc.perform(post("/api/v1/vehicles").header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                        {"make": "BYD", "model": "Dolphin", "batteryCapacityKwh": 44, "connectorType": "Type 2"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.ownerWalletAddress").value("WALLET1"));

        verify(vehicleService).createVehicle("WALLET1", new VehicleRequest("BYD", "Dolphin", 44, "Type 2"));
    }

    @Test
    void retorna401AoCriarVeiculoSemToken() throws Exception {
        mockMvc.perform(post("/api/v1/vehicles").contentType(MediaType.APPLICATION_JSON).content("""
                        {"make": "BYD", "model": "Dolphin", "connectorType": "Type 2"}
                        """))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(vehicleService);
    }

    @Test
    void retorna400QuandoVeiculoNaoTemConector() throws Exception {
        mockMvc.perform(post("/api/v1/vehicles").header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                        {"make": "BYD", "model": "Dolphin", "connectorType": "  "}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Campos inválidos: connectorType é obrigatório"));

        verifyNoInteractions(vehicleService);
    }

    @Test
    void listaSoOsVeiculosDaCarteiraDoToken() throws Exception {
        when(vehicleService.getVehiclesByOwner("WALLET1"))
                .thenReturn(List.of(new VehicleResponse(1L, "BYD", "Dolphin", 44, "Type 2", "WALLET1")));

        mockMvc.perform(get("/api/v1/vehicles").header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].ownerWalletAddress").value("WALLET1"));
    }

    @Test
    void retorna401AoListarVeiculosSemToken() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(vehicleService);
    }
}
