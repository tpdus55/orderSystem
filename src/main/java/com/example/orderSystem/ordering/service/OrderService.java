package com.example.orderSystem.ordering.service;

import com.example.orderSystem.member.domain.Member;
import com.example.orderSystem.member.repository.MemberRepository;
import com.example.orderSystem.ordering.domain.Order;
import com.example.orderSystem.ordering.domain.OrderDetail;
import com.example.orderSystem.ordering.domain.Role;
import com.example.orderSystem.ordering.dtos.OrderRequestDto;
import com.example.orderSystem.ordering.repository.OrderRepository;
import com.example.orderSystem.product.domain.Product;
import com.example.orderSystem.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.List;

@Service
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    @Autowired
    public OrderService(OrderRepository orderRepository, MemberRepository memberRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.memberRepository = memberRepository;
        this.productRepository = productRepository;
    }

    public Order save(List<OrderRequestDto> dtoList){
        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(()-> new EntityNotFoundException("없는 이메일 입니다."));

        Order order = Order.builder()
                .member(member)
                .order_status(Role.Ordered)
                .build();

        for(OrderRequestDto dto : dtoList){
            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(()-> new EntityNotFoundException("상품이 존재하지 않습니다."));
            OrderDetail detail = OrderDetail.builder()
                    .order(order)
                    .product(product)
                    .quantity(dto.getProductCount())
                    .build();
            order.getDetailList().add(detail);
        }

        return orderRepository.save(order);
    }

}



