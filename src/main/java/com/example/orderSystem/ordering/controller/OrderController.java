package com.example.orderSystem.ordering.controller;

import com.example.orderSystem.ordering.domain.Order;
import com.example.orderSystem.ordering.dtos.OrderRequestDto;
import com.example.orderSystem.ordering.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ordering")
public class OrderController {
    private final OrderService orderService;
    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

//    주문하기
    @PostMapping("/create")
    public ResponseEntity<?> save(@RequestBody List<OrderRequestDto> dtoList){
        Order order = orderService.save(dtoList);
        return ResponseEntity.status(HttpStatus.CREATED).body(order.getId());
    }

////   주문목록조회
//    @GetMapping("/list")
//    public

//   주문상세조회

}
