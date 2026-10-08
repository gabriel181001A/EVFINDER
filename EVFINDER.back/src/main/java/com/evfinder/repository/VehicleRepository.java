package com.evfinder.repository;

import com.evfinder.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    
    List<Vehicle> findByOwnerWalletAddress(String ownerWalletAddress);
    
}