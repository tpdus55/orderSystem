package com.example.orderSystem.ordering.repository;

import com.example.orderSystem.ordering.domain.Ordering;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderingRepository extends JpaRepository<Ordering,Long> {
}
