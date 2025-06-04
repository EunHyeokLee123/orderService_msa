package com.playdata.orderingservice.client;

import com.playdata.orderingservice.common.dto.CommonResDTO;
import com.playdata.orderingservice.ordering.dto.ProductResDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

import java.util.List;

@FeignClient(name = "product-service", url = "http://product-service.default.svc.cluster.local:8082")
public interface ProductServiceClient {

    @GetMapping("/product/{prodId}")
    CommonResDTO<ProductResDTO> findById(@PathVariable long prodId); // 상품 정보 조회

    @PostMapping("/product/updateQuantity")
    ResponseEntity<?> updateQuantity(@RequestBody ProductResDTO productResDTO); // 상품 재고 최신화 메소드

    @PostMapping("/product/products")
    CommonResDTO<List<ProductResDTO>> getProducts(@RequestBody List<Long> productIds);

    @PutMapping("/product/cancel")
    ResponseEntity<?> cancelProduct(@RequestBody Map<Long, Integer> map);

}
