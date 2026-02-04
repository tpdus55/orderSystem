package com.example.ordersystem.ordering.service;

import com.example.ordersystem.common.service.RabbitMqStockSerivce;
import com.example.ordersystem.common.service.SseAlarmService;
import com.example.ordersystem.member.domain.Member;
import com.example.ordersystem.member.repository.MemberRepository;
import com.example.ordersystem.ordering.domain.Ordering;
import com.example.ordersystem.ordering.domain.OrderDetail;
import com.example.ordersystem.ordering.dtos.OrderListDto;
import com.example.ordersystem.ordering.dtos.OrderCreateDto;
import com.example.ordersystem.ordering.repository.OrderRepository;
import com.example.ordersystem.product.domain.Product;
import com.example.ordersystem.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final SseAlarmService sseAlarmService;
    private final RedisTemplate<String,String> redisTemplate;
    private final RabbitMqStockSerivce rabbitMqStockSerivce;
    @Autowired
    public OrderService(OrderRepository orderRepository, MemberRepository memberRepository, ProductRepository productRepository, SseAlarmService sseAlarmService, @Qualifier("stockInventory") RedisTemplate<String, String> redisTemplate, RabbitMqStockSerivce rabbitMqStockSerivce) {
        this.orderRepository = orderRepository;
        this.memberRepository = memberRepository;
        this.productRepository = productRepository;
        this.sseAlarmService = sseAlarmService;
        this.redisTemplate = redisTemplate;
        this.rabbitMqStockSerivce = rabbitMqStockSerivce;
    }
//동시성 제어방법1. 특정 메서드에 한해 격리수준 올리기.
//    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Long save(List<OrderCreateDto> dtoList){
        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(()-> new EntityNotFoundException("없는 이메일 입니다."));

        Ordering order = Ordering.builder()
                .member(member)
                .build();

        for(OrderCreateDto dto : dtoList){
////            동시성 제어방법2. select for update를 통한 락 설정 이후 조회
//            Product product = productRepository.findByIdForUpdate(dto.getProductId())
//                    .orElseThrow(()-> new EntityNotFoundException("상품이 존재하지 않습니다."));
            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(()-> new EntityNotFoundException("상품이 존재하지 않습니다."));

////            동시성 제어방법3. redis에서 재고수량 확인 및 재고수량 감소처리
////            단점: 조회와 감소요청이 분리되다 보니, 동시성문제 발생 -> 해결책: 루아(lua)스크립트를 통해 여러작업을 단일요청으로 묶어 해결
            String remain = redisTemplate.opsForValue().get(String.valueOf(dto.getProductId()));
            int remainQuantity = Integer.parseInt(remain);
            if(remainQuantity<dto.getProductCount()) {
                throw new IllegalArgumentException("재고가 부족합니다.");
            }else{
                redisTemplate.opsForValue().decrement(String.valueOf(dto.getProductId()),dto.getProductCount());
            }
//            if(product.getStockQuantity()< dto.getProductCount()){
//                throw new IllegalArgumentException("재고가 부족합니다.");
//            }
//            product.updateStockQuantity(dto.getProductCount());
            OrderDetail detail = OrderDetail.builder()
                    .order(order)
                    .product(product)
                    .quantity(dto.getProductCount())
                    .build();
            order.getDetailList().add(detail);

//            rdb 동기화를 위한 작업1 : 스케쥴러 활용
//            rdb 동기화를 위한 작업2 : rabbitmq에 rdb 재고감소 메시지 발행
            rabbitMqStockSerivce.publish(dto.getProductId(), dto.getProductCount());
        }
        orderRepository.save(order);

//        주문성공 시 admin 유저에게 알림메시지 전송
        String message = order.getId() + "번 주문이 들어왔습니다.";
        sseAlarmService.sendMessage("admin@naver.com", email, message);
        return order.getId();
    }

    @Transactional(readOnly = true)
    public List<OrderListDto> findAll(){
        List<Ordering> orderList = orderRepository.findAll();
        List<OrderListDto> orderListDtos = new ArrayList<>();
        for(Ordering o : orderList){
            OrderListDto dto = OrderListDto.fromEntity(o);
            orderListDtos.add(dto);
        }
        return orderListDtos;
    }

    @Transactional(readOnly = true)
    public List<OrderListDto> myorders(){
        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(()-> new EntityNotFoundException("없는 이메일 입니다."));

        List<Ordering> orderList = orderRepository.findAllByMember(member);
        List<OrderListDto> orderListDtos = new ArrayList<>();
        for(Ordering o : orderList){
            OrderListDto dto = OrderListDto.fromEntity(o);
            orderListDtos.add(dto);
        }
        return orderListDtos;
    }

}



