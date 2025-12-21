package org.example.repository;

import org.example.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByRoomNumberAndCheckOutDateAfterAndCheckInDateBefore(Integer roomNumber, LocalDate from, LocalDate to);
    List<Order> findByClientPassportNumber(String passportNumber);
    List<Order> findByRoomNumber(Integer roomNumber);
}

