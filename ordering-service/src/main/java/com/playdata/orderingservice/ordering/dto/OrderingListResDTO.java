package com.playdata.orderingservice.ordering.dto;

import com.playdata.orderingservice.ordering.entity.OrderStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;
import lombok.*;

import java.util.List;

@Getter @Setter
@ToString @NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderingListResDTO {

    // 하나의 주문에 대한 내용
    private Long id;
    private String userEmail;
    private OrderStatus orderStatus;
    private List<OrderDetailDTO> orderDetails;


    // 주문 상세 내용
    @Getter @Setter
    @ToString @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderDetailDTO {
        private Long id;
        private String productName;
        private int count;
    }


}
