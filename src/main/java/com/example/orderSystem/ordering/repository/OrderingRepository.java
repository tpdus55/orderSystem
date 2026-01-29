package com.example.orderSystem.ordering.repository;

import com.example.orderSystem.ordering.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderingRepository extends JpaRepository<Order,Long> {
}
