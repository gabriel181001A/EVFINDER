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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class CheckInService {

    private static final double EARTH_RADIUS_METERS = 6_371_000;

    private final StationRepository stationRepository;
    private final CheckInRepository checkInRepository;
    private final TokenRewardService tokenRewardService;
    private final RewardProperties properties;

    public CheckInService(StationRepository stationRepository, CheckInRepository checkInRepository,
                          TokenRewardService tokenRewardService, RewardProperties properties) {
        this.stationRepository = stationRepository;
        this.checkInRepository = checkInRepository;
        this.tokenRewardService = tokenRewardService;
        this.properties = properties;
    }

    // walletAddress é a carteira autenticada pelo token de sessão.
    // A transação mantém a estação travada desde a checagem de repetição até o check-in ser salvo
    @Transactional
    public RewardResponse checkIn(String walletAddress, CheckInRequest request) {
        Station station = stationRepository.findLockedById(request.stationId())
                .orElseThrow(() -> new ResourceNotFoundException("Estação com ID " + request.stationId() + " não encontrada."));

        validateDistance(station, request.latitude(), request.longitude());

        LocalDateTime now = LocalDateTime.now();
        validateCooldown(station, walletAddress, now);

        double tokens = properties.tokensPerCheckIn();
        String txHash = tokenRewardService.processReward(walletAddress, tokens);

        CheckIn checkIn = new CheckIn();
        checkIn.setStation(station);
        checkIn.setUserWalletAddress(walletAddress);
        checkIn.setLatitude(request.latitude());
        checkIn.setLongitude(request.longitude());
        checkIn.setTokensAwarded(tokens);
        checkIn.setTransactionHash(txHash);
        checkIn.setCreatedAt(now);
        checkInRepository.save(checkIn);

        return new RewardResponse(
                "SUCCESS",
                "Check-in confirmado. Tokens processados com sucesso!",
                tokens,
                txHash
        );
    }

    private void validateDistance(Station station, double userLat, double userLng) {
        if (station.getLatitude() == null || station.getLongitude() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Estação " + station.getId() + " não tem localização cadastrada, então o check-in não pode ser validado.");
        }

        double distance = distanceInMeters(userLat, userLng, station.getLatitude(), station.getLongitude());
        if (distance > properties.maxCheckInDistanceMeters()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT,
                    "Você está a " + Math.round(distance) + " m da estação. O check-in só é permitido a até "
                            + Math.round(properties.maxCheckInDistanceMeters()) + " m.");
        }
    }

    private void validateCooldown(Station station, String walletAddress, LocalDateTime now) {
        checkInRepository.findFirstByStationIdAndUserWalletAddressOrderByCreatedAtDesc(station.getId(), walletAddress)
                .map(lastCheckIn -> lastCheckIn.getCreatedAt().plus(properties.checkInCooldown()))
                .filter(availableAt -> availableAt.isAfter(now))
                .ifPresent(availableAt -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Você já fez check-in nesta estação. Tente novamente em "
                                    + formatRemaining(Duration.between(now, availableAt)) + ".");
                });
    }

    // Ex: 23h05min. Arredonda para cima, para nunca dizer "0min" enquanto ainda falta algum tempo
    private static String formatRemaining(Duration remaining) {
        long totalMinutes = remaining.plusMinutes(1).minusNanos(1).toMinutes();
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        return hours > 0 ? String.format("%dh%02dmin", hours, minutes) : minutes + "min";
    }

    // Fórmula de Haversine: distância em linha reta entre dois pontos na superfície da Terra
    static double distanceInMeters(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return 2 * EARTH_RADIUS_METERS * Math.asin(Math.sqrt(a));
    }
}
