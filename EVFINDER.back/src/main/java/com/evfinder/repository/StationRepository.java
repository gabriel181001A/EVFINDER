package com.evfinder.repository;

import com.evfinder.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StationRepository extends JpaRepository<Station, Long> {
    
    // Você pode criar buscas customizadas apenas escrevendo o nome do método!
    List<Station> findByIsOperationalTrue();
    List<Station> findByConnectorType(String connectorType);
    
}