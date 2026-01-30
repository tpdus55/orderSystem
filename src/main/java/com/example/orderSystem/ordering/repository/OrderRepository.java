package com.example.orderSystem.ordering.repository;

import com.example.orderSystem.ordering.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order,Long> {
}
