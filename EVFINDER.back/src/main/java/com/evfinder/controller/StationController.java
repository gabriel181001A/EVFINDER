package com.evfinder.controller;
import com.evfinder.dto.OcmStationDto;
import com.evfinder.integration.OpenChargeClient;
import com.evfinder.dto.StationRequest;
import com.evfinder.dto.StationResponse;
import com.evfinder.service.StationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stations")
public class StationController {

    private final StationService stationService;
    private final OpenChargeClient openChargeClient;

    public StationController(StationService stationService, OpenChargeClient openChargeClient) {
    this.stationService = stationService;
    this.openChargeClient = openChargeClient;
}

    @GetMapping("/search-live")
    public ResponseEntity<List<OcmStationDto>> searchLiveStations(
            @RequestParam Double lat, 
            @RequestParam Double lng, 
            @RequestParam(defaultValue = "10") Double distance) {
        
        List<OcmStationDto> liveStations = openChargeClient.fetchStationsNearLocation(lat, lng, distance);
        return ResponseEntity.ok(liveStations);
    }

    @PostMapping
    public ResponseEntity<StationResponse> create(@Valid @RequestBody StationRequest request) {
        StationResponse response = stationService.createStation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<StationResponse>> getAll() {
        return ResponseEntity.ok(stationService.getAllStations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(stationService.getStationById(id));
    }
}