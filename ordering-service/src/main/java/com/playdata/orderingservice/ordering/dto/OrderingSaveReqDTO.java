package com.playdata.orderingservice.ordering.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder @ToString
public class OrderingSaveReqDTO {

    // 누가 주문했는지에 대한 정보는 JWT에 있음 (프론트가 토큰을 같이 보낼 것임.)

    private Long productId;
    private int productQuantity;

}
