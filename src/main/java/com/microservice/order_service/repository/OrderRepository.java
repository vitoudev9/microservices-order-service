package com.microservice.order_service.repository;

import com.microservice.order_service.model.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Orders, Long> {

    // Custom queries if needed
}