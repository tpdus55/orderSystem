package com.example.orderSystem.ordering.domain;

import com.example.orderSystem.common.time.BaseTime;
import com.example.orderSystem.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter @ToString
@Builder
@Entity
@Table(name = "ordering")
public class Order extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id",foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT), nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role order_status = Role.Ordered;

    @OneToMany(mappedBy = "order",fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @Builder.Default
    private List<OrderDetail> detailList = new ArrayList<>();
}
