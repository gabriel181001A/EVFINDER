package com.evfinder.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OcmStationDto(
    @JsonProperty("AddressInfo") AddressInfo addressInfo,
    @JsonProperty("Connections") List<Connection> connections
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AddressInfo(
        @JsonProperty("Title") String title,
        @JsonProperty("Latitude") Double latitude,
        @JsonProperty("Longitude") Double longitude,
        @JsonProperty("AddressLine1") String addressLine1
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Connection(
        @JsonProperty("ConnectionType") ConnectionType connectionType
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ConnectionType(
        @JsonProperty("Title") String title,
        @JsonProperty("ID") Integer id
    ) {}
}