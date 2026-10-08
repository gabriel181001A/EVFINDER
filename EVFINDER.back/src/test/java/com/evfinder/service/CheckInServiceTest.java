package com.evfinder.service;

import com.evfinder.blockchain.TokenRewardService;
import com.evfinder.config.RewardProperties;
import com.evfinder.dto.CheckInRequest;
import com.evfinder.dto.RewardResponse;
import com.evfinder.entity.CheckIn;
import com.evfinder.entity.Station;
import com.evfinder.exception.ResourceNotFoundException;
import com.evfinder.repository.CheckInRepository;
import com.evfinder.repository.StationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

// Regras do check-in: estação existente, distância máxima, intervalo entre check-ins e tokens concedidos
class CheckInServiceTest {

    // Estação na Av. Paulista. Cada 0,001 grau de latitude equivale a cerca de 111 m
    private static final double STATION_LAT = -23.5613;
    private static final double STATION_LNG = -46.6565;

    private StationRepository stationRepository;
    private CheckInRepository checkInRepository;
    private TokenRewardService tokenRewardService;
    private CheckInService service;

    @BeforeEach
    void setUp() {
        stationRepository = mock(StationRepository.class);
        checkInRepository = mock(CheckInRepository.class);
        tokenRewardService = mock(TokenRewardService.class);
        service = new CheckInService(stationRepository, checkInRepository, tokenRewardService,
                new RewardProperties(5, Duration.ofHours(24), 200));

        when(stationRepository.findLockedById(1L)).thenReturn(Optional.of(station(STATION_LAT, STATION_LNG)));
        when(tokenRewardService.processReward(anyString(), anyDouble())).thenReturn("sol_tx_teste");
    }

    @Test
    void registraCheckInEConcedeTokens() {
        // ~145 m da estação, e com espaços em volta da carteira
        RewardResponse response = service.checkIn(request(" WALLET1 ", STATION_LAT + 0.0013));

        assertThat(response.tokensAwarded()).isEqualTo(5.0);
        assertThat(response.solanaTransactionHash()).isEqualTo("sol_tx_teste");
        verify(tokenRewardService).processReward("WALLET1", 5.0);

        ArgumentCaptor<CheckIn> saved = ArgumentCaptor.forClass(CheckIn.class);
        verify(checkInRepository).save(saved.capture());
        assertThat(saved.getValue().getStation().getId()).isEqualTo(1L);
        assertThat(saved.getValue().getUserWalletAddress()).isEqualTo("WALLET1");
        assertThat(saved.getValue().getTokensAwarded()).isEqualTo(5.0);
        assertThat(saved.getValue().getTransactionHash()).isEqualTo("sol_tx_teste");
        assertThat(saved.getValue().getCreatedAt()).isNotNull();
    }

    @Test
    void lancaNotFoundQuandoEstacaoNaoExiste() {
        when(stationRepository.findLockedById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.checkIn(new CheckInRequest(99L, "WALLET1", STATION_LAT, STATION_LNG)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Estação com ID 99 não encontrada.");

        verifyNoInteractions(tokenRewardService);
    }

    @Test
    void recusaCheckInLongeDaEstacao() {
        // ~300 m da estação, acima do limite de 200 m
        assertThatThrownBy(() -> service.checkIn(request("WALLET1", STATION_LAT + 0.0027)))
                .isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
                    assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
                    assertThat(ex.getReason())
                            .isEqualTo("Você está a 300 m da estação. O check-in só é permitido a até 200 m.");
                });

        verifyNoInteractions(tokenRewardService);
        verify(checkInRepository, never()).save(any());
    }

    @Test
    void recusaCheckInEmEstacaoSemLocalizacao() {
        when(stationRepository.findLockedById(1L)).thenReturn(Optional.of(station(null, null)));

        assertThatThrownBy(() -> service.checkIn(request("WALLET1", STATION_LAT)))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));

        verifyNoInteractions(tokenRewardService);
    }

    @Test
    void recusaCheckInRepetidoAntesDoIntervalo() {
        lastCheckInOf("WALLET1", LocalDateTime.now().minusHours(1));

        assertThatThrownBy(() -> service.checkIn(request("WALLET1", STATION_LAT)))
                .isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
                    assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(ex.getReason())
                            .isEqualTo("Você já fez check-in nesta estação. Tente novamente em 23h00min.");
                });

        verifyNoInteractions(tokenRewardService);
        verify(checkInRepository, never()).save(any());
    }

    @Test
    void permiteNovoCheckInDepoisDoIntervalo() {
        lastCheckInOf("WALLET1", LocalDateTime.now().minusHours(25));

        RewardResponse response = service.checkIn(request("WALLET1", STATION_LAT));

        assertThat(response.status()).isEqualTo("SUCCESS");
        verify(checkInRepository).save(any());
    }

    @Test
    void calculaDistanciaEmMetros() {
        // 1 grau no equador ou ao longo de um meridiano equivale a ~111,2 km
        assertThat(CheckInService.distanceInMeters(0, 0, 0, 1)).isCloseTo(111_195, within(1.0));
        assertThat(CheckInService.distanceInMeters(0, 0, 1, 0)).isCloseTo(111_195, within(1.0));
        assertThat(CheckInService.distanceInMeters(STATION_LAT, STATION_LNG, STATION_LAT, STATION_LNG)).isZero();
    }

    private void lastCheckInOf(String walletAddress, LocalDateTime createdAt) {
        CheckIn lastCheckIn = new CheckIn();
        lastCheckIn.setCreatedAt(createdAt);
        when(checkInRepository.findFirstByStationIdAndUserWalletAddressOrderByCreatedAtDesc(1L, walletAddress))
                .thenReturn(Optional.of(lastCheckIn));
    }

    private static CheckInRequest request(String walletAddress, double userLat) {
        return new CheckInRequest(1L, walletAddress, userLat, STATION_LNG);
    }

    private static Station station(Double latitude, Double longitude) {
        Station station = new Station();
        station.setId(1L);
        station.setName("Posto Paulista");
        station.setLatitude(latitude);
        station.setLongitude(longitude);
        return station;
    }
}
