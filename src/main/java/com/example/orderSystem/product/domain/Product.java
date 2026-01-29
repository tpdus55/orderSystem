package com.example.orderSystem.product.domain;

import com.example.orderSystem.member.domain.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT),nullable = false)
    private Member member;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private Long price;
    private String category;
    @Column(nullable = false)
    private Long stockQuantity;
    private String imagePath;
    private LocalDate created_time;

    public void ImageUrl(String imagePath){
        this.imagePath = imagePath;
    }
}
