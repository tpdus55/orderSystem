package com.example.ordersystem.common.service;

import com.example.ordersystem.common.dtos.RabbitMqStockDto;
import com.example.ordersystem.product.domain.Product;
import com.example.ordersystem.product.repository.ProductRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RabbitMqStockSerivce {
    private final RabbitTemplate rabbitTemplate;
    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;
    @Autowired
    public RabbitMqStockSerivce(RabbitTemplate rabbitTemplate, ProductRepository productRepository, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.productRepository = productRepository;
        this.objectMapper = objectMapper;
    }

//    orderservice에서 주문하면 여기 publish로 넘어옴
    public void publish(Long productId, int productCount){
        RabbitMqStockDto dto = RabbitMqStockDto.builder()
                .productId(productId)
                .productCount(productCount)
                .build();
        rabbitTemplate.convertAndSend("stockQueue",dto); //stockQueue에 dto를 발행하겠다는 뜻
    }

//   RabbitListener rabbitmq에 특정 큐에 대해 subscribe하는 어노테이션
//    RabbitListener는 단일스레드로 메시지를 처리하므로, 동시성 이슈 발생 X
//    다만 멀티서버환경에서는 문제 발생 할 수있음
    @RabbitListener(queues = "stockQueue")
    @Transactional
    public void subscribe(Message message) throws JsonProcessingException {
        String messageBody = new String(message.getBody());
        RabbitMqStockDto dto = objectMapper.readValue(messageBody, RabbitMqStockDto.class);
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(()-> new EntityNotFoundException("entity is not found"));
        product.updateStockQuantity(dto.getProductCount());

    }
}
