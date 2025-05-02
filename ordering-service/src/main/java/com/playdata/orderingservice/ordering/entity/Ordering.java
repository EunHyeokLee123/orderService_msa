package com.playdata.orderingservice.ordering.entity;

import com.playdata.orderingservice.ordering.dto.OrderingListResDTO;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter @ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Ordering {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

/*   이제는 연관관계 매핑을 할 수 없음 -> 도메인으로 다 나뉘었기때문에
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    private User user;
    */

    // 프로젝트가 나눠지면서 Ordering에서는 User 엔터티에 대한 정보를 얻을 수 없음.
    // 클라이언트 단에서 넘어오는 정보만 저장할 수 있음.

    @JoinColumn
    private Long userId;

    private OrderStatus orderStatus;

    // 주문 상세 정보를 가지는
    @OneToMany(mappedBy = "ordering", cascade = CascadeType.PERSIST)
    private List<OrderDetail> orderDetails;

    public OrderingListResDTO fromEntity(String email,
                                         Map<Long, String> productIdToNameMap) {
        List<OrderDetail> orderDetailList = this.getOrderDetails();

        List<OrderingListResDTO.OrderDetailDTO> orderDetailDTOs
                 = new ArrayList<>();

        // OrderDetail 엔터티를 OrderDetailDTO로 변환하자
        for (OrderDetail orderDetail : orderDetailList) {
            OrderingListResDTO.OrderDetailDTO orderDetailDTO = orderDetail.fromEntity(productIdToNameMap);
            orderDetailDTOs.add(orderDetailDTO);
        }

        return OrderingListResDTO.builder()
                .id(id)
                .userEmail(email)
                .orderDetails(orderDetailDTOs)
                .orderStatus(orderStatus)
                .build();
    }

}
