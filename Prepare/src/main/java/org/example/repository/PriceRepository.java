package org.example.repository;

import org.example.model.Price;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PriceRepository extends JpaRepository<Price, Long> {
    Optional<Price> findFirstByObjectTypeAndObjectNumber(String objectType, Integer objectNumber);
}

