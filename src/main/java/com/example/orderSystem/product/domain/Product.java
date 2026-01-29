package com.example.orderSystem.product.domain;

import com.example.orderSystem.common.time.BaseTime;
import com.example.orderSystem.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Builder
@Entity
public class Product extends BaseTime {
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

    public void updateImagePath(String imagePath){
        this.imagePath = imagePath;
    }
}
