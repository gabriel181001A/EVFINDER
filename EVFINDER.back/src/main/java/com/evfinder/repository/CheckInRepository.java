package com.evfinder.repository;

import com.evfinder.entity.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CheckInRepository extends JpaRepository<CheckIn, Long> {

    // Último check-in da carteira na estação, usado para aplicar o intervalo entre check-ins
    Optional<CheckIn> findFirstByStationIdAndUserWalletAddressOrderByCreatedAtDesc(Long stationId, String userWalletAddress);

}
