package com.evfinder.service;

import com.evfinder.dto.StationRequest;
import com.evfinder.dto.StationResponse;
import com.evfinder.entity.Station;
import com.evfinder.exception.ResourceNotFoundException;
import com.evfinder.repository.StationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StationService {

    private final StationRepository repository;

    public StationService(StationRepository repository) {
        this.repository = repository;
    }

    public StationResponse createStation(StationRequest request) {
        Station station = new Station();
        station.setName(request.name());
        station.setLatitude(request.latitude());
        station.setLongitude(request.longitude());
        station.setConnectorType(request.connectorType());
        station.setPowerKw(request.powerKw());
        station.setOperational(true);

        Station savedStation = repository.save(station);
        return mapToResponse(savedStation);
    }

    public List<StationResponse> getAllStations() {
        return repository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public StationResponse getStationById(Long id) {
        Station station = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estação com ID " + id + " não encontrada."));
        return mapToResponse(station);
    }

    private StationResponse mapToResponse(Station station) {
        return new StationResponse(
                station.getId(),
                station.getName(),
                station.getLatitude(),
                station.getLongitude(),
                station.getConnectorType(),
                station.getPowerKw(),
                station.getOperational()
        );
    }
}