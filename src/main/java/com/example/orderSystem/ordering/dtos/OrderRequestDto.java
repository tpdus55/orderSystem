package com.example.orderSystem.ordering.dtos;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter @ToString
@Builder
public class OrderRequestDto {
    private Long productId;
    private int productCount;


}
