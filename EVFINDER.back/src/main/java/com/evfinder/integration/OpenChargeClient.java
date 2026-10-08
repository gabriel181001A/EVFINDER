package com.evfinder.integration;

import com.evfinder.dto.OcmStationDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;

@Service
public class OpenChargeClient {

    private final RestClient restClient;
    // Lida do application.properties (variável de ambiente OCM_API_KEY ou arquivo .env)
    private final String apiKey;

    public OpenChargeClient(@Value("${openchargemap.api-key}") String apiKey) {
        this.apiKey = apiKey;
        // Inicializa o equivalente ao HttpClient do .NET
        this.restClient = RestClient.builder()
                .baseUrl("https://api.openchargemap.io/v3")
                .build();
    }

    public List<OcmStationDto> fetchStationsNearLocation(Double latitude, Double longitude, Double distanceKm) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/poi")
                        .queryParam("key", apiKey)
                        .queryParam("latitude", latitude)
                        .queryParam("longitude", longitude)
                        .queryParam("distance", distanceKm)
                        .queryParam("maxresults", 10)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<OcmStationDto>>() {});
    }
}