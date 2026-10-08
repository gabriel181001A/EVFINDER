package com.evfinder.repository;

import com.evfinder.entity.Station;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StationRepository extends JpaRepository<Station, Long> {

    // Você pode criar buscas customizadas apenas escrevendo o nome do método!
    List<Station> findByIsOperationalTrue();
    List<Station> findByConnectorType(String connectorType);

    // Trava a linha da estação até o fim da transação (SELECT ... FOR UPDATE).
    // Assim, dois check-ins simultâneos na mesma estação não passam juntos pela checagem de repetição.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Station> findLockedById(Long id);

}