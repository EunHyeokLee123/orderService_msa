package com.playdata.orderingservice.ordering.entity;

import com.playdata.orderingservice.ordering.dto.OrderingListResDTO;
import jakarta.persistence.*;
import lombok.*;

import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Entity
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int quantity;

    @JoinColumn
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="ordering_id")
    private Ordering ordering;

    // 엔터티를 DTO로 변환하는 메소드
    public OrderingListResDTO.OrderDetailDTO fromEntity(Map<Long, String> map) {
        return OrderingListResDTO.OrderDetailDTO.builder()
                .id(id)
                .productName(map.get(productId))
                .count(quantity)
                .build();
    }
}
