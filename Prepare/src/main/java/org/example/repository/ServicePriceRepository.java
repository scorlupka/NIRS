package org.example.repository;

import org.example.model.ServicePrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServicePriceRepository extends JpaRepository<ServicePrice, Long> {
    Optional<ServicePrice> findByServiceId(Long serviceId);
}

