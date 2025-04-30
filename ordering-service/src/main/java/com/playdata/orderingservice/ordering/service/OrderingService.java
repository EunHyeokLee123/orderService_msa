package com.playdata.orderingservice.ordering.service;

import com.playdata.orderingservice.client.ProductServiceClient;
import com.playdata.orderingservice.client.UserServiceClient;
import com.playdata.orderingservice.common.auth.TokenUserInfo;
import com.playdata.orderingservice.common.dto.CommonResDTO;
import com.playdata.orderingservice.ordering.dto.OrderingSaveReqDTO;
import com.playdata.orderingservice.ordering.dto.ProductResDTO;
import com.playdata.orderingservice.ordering.dto.UserResDTO;
import com.playdata.orderingservice.ordering.entity.OrderDetail;
import com.playdata.orderingservice.ordering.entity.Ordering;
import com.playdata.orderingservice.ordering.repository.OrderingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class OrderingService {

    private final OrderingRepository orderingRepository;
    private final RestTemplate restTemplate;

    // Feign client 구현체를 주입받기
    private final UserServiceClient userServiceClient;
    private final ProductServiceClient productServiceClient;

    public Ordering createOrder(List<OrderingSaveReqDTO> dtoList,
                                TokenUserInfo userInfo) {

        // Ordering 객체를 생성하기 위해 회원 정보를 얻어오자.
        // 우리가 가진 유일한 정보는 토큰 안에 들어있던 이메일 뿐입니다...
        // 이메일을 가지고 요청을 보내자 -> user-service

        CommonResDTO<UserResDTO> byEmail 
                = userServiceClient.findByEmail(userInfo.getEmail());

        
        
        UserResDTO userDTO = byEmail.getResult();
        log.info("user-service로부터 전달받은 결과: {}", userDTO);

        // Ordering(주문) 객체 생성
        Ordering ordering = Ordering.builder()
                .userId(userDTO.getId())
                .orderDetails(new ArrayList<>()) // 아직 주문 상세 들어가기 전.
                .build();

        // 주문 상세 내역에 대한 처리를 반복해서 지정.
        for (OrderingSaveReqDTO dto : dtoList) {

            // dto 안에 있는 상품 id를 이용해서 상품 정보 얻어오자.
            // product 객체를 조회하자 -> product-service에게 요청해야 함!

            CommonResDTO<ProductResDTO> byId
                    = productServiceClient.findById(dto.getProductId());

            ProductResDTO productResDTO = byId.getResult();

            log.info("product-service로부터 받아온 결과: {}", productResDTO);
            int stockQuantity = productResDTO.getStockQuantity();

            // 재고 넉넉하게 있는지 확인
            int quantity = dto.getProductQuantity();
            if (stockQuantity < quantity) {
                throw new IllegalArgumentException("재고 부족!");
            }

            // 재고가 부족하지 않다면 재고 수량을 주문 수량만큼 빼주자
            // product-service에게 재고 수량이 변경되었다고 알려주자.
            // 상품 id와 변경되어야 할 수량을 함께 보내주자.

            // LinkedMultiValueMap은 키 하나에 여러개의 값을 리스트 형태로 저장 가능합니다.
            // 데이터 삽입 순서를 보장할 수 있습니다.
            // 웹 관련 작업에 적합합니다.

            productResDTO.setStockQuantity(stockQuantity - quantity);

            productServiceClient.updateQuantity(productResDTO);

            // 주문 상세 내역 엔터티 생성
            OrderDetail orderDetail = OrderDetail.builder()
                    .productId(dto.getProductId())
                    .ordering(ordering)
                    .quantity(quantity)
                    .build();

            // 주문 내역 리스트에 상세 내역을 add하기.
            // (cascadeType.PERSIST로 세팅했기 때문에 함께 INSERT가 진행될 것!)
            ordering.getOrderDetails().add(orderDetail);
        }

        return orderingRepository.save(ordering);
    }
}