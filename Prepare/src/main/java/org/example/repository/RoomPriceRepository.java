package org.example.repository;

import org.example.model.RoomPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoomPriceRepository extends JpaRepository<RoomPrice, Integer> {
    Optional<RoomPrice> findByRoomNumber(Integer roomNumber);
}

