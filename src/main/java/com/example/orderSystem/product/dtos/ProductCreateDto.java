package com.example.orderSystem.product.dtos;

import com.example.orderSystem.member.domain.Member;
import com.example.orderSystem.product.domain.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ProductCreateDto {
    private String name;
    private Long price;
    private String category;
    private Long stockQuantity;
    private String imagePath;

    public Product toEntity(Member member, String imagePath){
        return Product.builder()
                .name(this.name)
                .price(this.price)
                .category(this.category)
                .stockQuantity(this.stockQuantity)
                .imagePath(imagePath)
                .member(member)
                .build();
    }
}
