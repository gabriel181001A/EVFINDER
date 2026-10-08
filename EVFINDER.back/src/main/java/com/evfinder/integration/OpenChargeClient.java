package com.evfinder.integration;

import com.evfinder.dto.OcmStationDto;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;

@Service
public class OpenChargeClient {

    private final RestClient restClient;
    // Em produção, coloque essa chave no application.properties!
    private final String API_KEY = "113d56bf-8c94-454a-955c-d22cc81b4ac0"; 

    public OpenChargeClient() {
        // Inicializa o equivalente ao HttpClient do .NET
        this.restClient = RestClient.builder()
                .baseUrl("https://api.openchargemap.io/v3")
                .build();
    }

    public List<OcmStationDto> fetchStationsNearLocation(Double latitude, Double longitude, Double distanceKm) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/poi")
                        .queryParam("key", API_KEY)
                        .queryParam("latitude", latitude)
                        .queryParam("longitude", longitude)
                        .queryParam("distance", distanceKm)
                        .queryParam("maxresults", 10)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<OcmStationDto>>() {});
    }
}