package com.evfinder.controller;

import com.evfinder.dto.OcmStationDto;
import com.evfinder.entity.Vehicle;
import com.evfinder.exception.GlobalExceptionHandler;
import com.evfinder.integration.OpenChargeClient;
import com.evfinder.repository.VehicleRepository;
import com.evfinder.security.AuthenticatedWalletResolver;
import com.evfinder.service.AuthService;
import com.evfinder.service.RecommendationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Testa a rota de recomendações junto com o login e o GlobalExceptionHandler, sem subir banco nem chamar a API externa
class RecommendationControllerTest {

    private VehicleRepository vehicleRepository;
    private OpenChargeClient openChargeClient;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        vehicleRepository = mock(VehicleRepository.class);
        openChargeClient = mock(OpenChargeClient.class);
        RecommendationService service = new RecommendationService(vehicleRepository, openChargeClient);

        AuthService authService = mock(AuthService.class);
        when(authService.authenticate("token-valido")).thenReturn("WALLET1");

        mockMvc = MockMvcBuilders.standaloneSetup(new RecommendationController(service))
                .setCustomArgumentResolvers(new AuthenticatedWalletResolver(authService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void retornaApenasEstacoesCompativeisComOConectorDoVeiculo() throws Exception {
        when(vehicleRepository.findByIdAndOwnerWalletAddress(1L, "WALLET1")).thenReturn(Optional.of(vehicle("Type 2")));
        when(openChargeClient.fetchStationsNearLocation(anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(List.of(station("Posto Type 2", "Type 2 (Socket Only)"), station("Posto CHAdeMO", "CHAdeMO")));

        mockMvc.perform(recommendations().param("vehicleId", "1").param("lat", "-22.9").param("lng", "-47.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].AddressInfo.Title").value("Posto Type 2"));
    }

    @Test
    void retorna401SemToken() throws Exception {
        mockMvc.perform(get("/api/v1/recommendations").param("vehicleId", "1").param("lat", "-22.9").param("lng", "-47.0"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(vehicleRepository, openChargeClient);
    }

    @Test
    void retorna404QuandoVeiculoNaoExiste() throws Exception {
        when(vehicleRepository.findByIdAndOwnerWalletAddress(99L, "WALLET1")).thenReturn(Optional.empty());

        mockMvc.perform(recommendations().param("vehicleId", "99").param("lat", "-22.9").param("lng", "-47.0"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void retorna404QuandoVeiculoEhDeOutraCarteira() throws Exception {
        // O veículo 2 existe, mas é de outra carteira: a busca filtrada pelo dono não o encontra
        when(vehicleRepository.findByIdAndOwnerWalletAddress(2L, "WALLET1")).thenReturn(Optional.empty());

        mockMvc.perform(recommendations().param("vehicleId", "2").param("lat", "-22.9").param("lng", "-47.0"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Veículo não encontrado. ID: 2"));

        verifyNoInteractions(openChargeClient);
    }

    @Test
    void retorna400QuandoVeiculoNaoTemConector() throws Exception {
        when(vehicleRepository.findByIdAndOwnerWalletAddress(1L, "WALLET1")).thenReturn(Optional.of(vehicle(null)));

        mockMvc.perform(recommendations().param("vehicleId", "1").param("lat", "-22.9").param("lng", "-47.0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("não tem tipo de conector")));
    }

    @Test
    void retorna400QuandoFaltaParametroObrigatorio() throws Exception {
        mockMvc.perform(recommendations().param("vehicleId", "1").param("lng", "-47.0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("lat")));
    }

    @Test
    void retorna400QuandoParametroTemTipoInvalido() throws Exception {
        mockMvc.perform(recommendations().param("vehicleId", "1").param("lat", "abc").param("lng", "-47.0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    private static MockHttpServletRequestBuilder recommendations() {
        return get("/api/v1/recommendations").header("Authorization", "Bearer token-valido");
    }

    private Vehicle vehicle(String connectorType) {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(1L);
        vehicle.setMake("BYD");
        vehicle.setModel("Dolphin");
        vehicle.setConnectorType(connectorType);
        vehicle.setOwnerWalletAddress("WALLET1");
        return vehicle;
    }

    private OcmStationDto station(String title, String connectorTitle) {
        return new OcmStationDto(
                new OcmStationDto.AddressInfo(title, -22.9, -47.0, "Rua Teste, 123"),
                List.of(new OcmStationDto.Connection(new OcmStationDto.ConnectionType(connectorTitle, 1)))
        );
    }
}
