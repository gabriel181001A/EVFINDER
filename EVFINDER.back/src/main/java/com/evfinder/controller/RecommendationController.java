package com.evfinder.controller;

import com.evfinder.dto.OcmStationDto;
import com.evfinder.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    // O Frontend vai chamar esta rota com o ID do carro e a localização GPS do telemóvel
    @GetMapping
    public ResponseEntity<List<OcmStationDto>> getRecommendations(
            @RequestParam Long vehicleId,
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(defaultValue = "15") Double radiusKm) {

        List<OcmStationDto> recommendations = recommendationService.recommendStations(vehicleId, lat, lng, radiusKm);
        return ResponseEntity.ok(recommendations);
    }
}