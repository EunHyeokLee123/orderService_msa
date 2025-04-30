package com.playdata.orderingservice.ordering.entity;

import jakarta.persistence.*;
import lombok.*;

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
}
