package org.example.repository;

import org.example.model.OrderServiceId;
import org.example.model.OrderServiceLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderServiceLinkRepository extends JpaRepository<OrderServiceLink, OrderServiceId> {

    @Query("select l.serviceId from OrderServiceLink l where l.orderNumber = :orderNumber")
    List<Long> findServiceIdsByOrderNumber(Integer orderNumber);

    void deleteByOrderNumberAndServiceId(Integer orderNumber, Long serviceId);

    void deleteByOrderNumber(Integer orderNumber);
    
    void deleteByServiceId(Long serviceId);
}




