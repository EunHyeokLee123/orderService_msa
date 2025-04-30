package com.playdata.orderingservice.ordering.controller;

import com.playdata.orderingservice.common.auth.TokenUserInfo;
import com.playdata.orderingservice.common.dto.CommonResDTO;
import com.playdata.orderingservice.ordering.dto.OrderingSaveReqDTO;
import com.playdata.orderingservice.ordering.entity.Ordering;
import com.playdata.orderingservice.ordering.service.OrderingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
@Slf4j
public class OrderingController {

    private final OrderingService orderingService;


    @PostMapping("/create")
    public ResponseEntity<?> createOrder(
            // 전역 인증 정보를 담아놓는 Security Context Holder에서 메소드 호출 시
            // 사용자 인증 정보를 전달해주는 아노테이션
            @AuthenticationPrincipal TokenUserInfo userInfo,
            @RequestBody List<OrderingSaveReqDTO> dtoList
            ){
            log.info("/order/create: POST, userInfo:{}", userInfo);
            log.info("dtoList: {}", dtoList);

        Ordering order = orderingService.createOrder(dtoList, userInfo);

        CommonResDTO resDTO = new CommonResDTO(HttpStatus.CREATED,
                "정상 주문 완료", order.getId());

        return new ResponseEntity<>(resDTO, HttpStatus.CREATED);
    }
}
