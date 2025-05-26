package com.playdata.productservice.product.controller;

import com.playdata.productservice.common.dto.CommonResDTO;
import com.playdata.productservice.product.dto.ProductResDTO;
import com.playdata.productservice.product.dto.ProductSaveReqDTO;
import com.playdata.productservice.product.dto.ProductSearchDTO;
import com.playdata.productservice.product.entity.Product;
import com.playdata.productservice.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    // 상품 등록 요청
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<?> createProduct(ProductSaveReqDTO dto) throws IOException {

        /*
        상품 등록 요청은 여러 데이터와 함께 이미지가 전달될 것임.
        1. JS의 fromData 객체를 통해 모든 데이터를 전달   --> 우리가 사용할 방식
        (JSON 형태가 아니라, multipart/form-data 형식으로 옴)
        2. JSON 형태로 전달 (이미지를 Base64 인코딩을 통해 문자열로 변환해서 전달)

        from-data로 넘어오는 이미지 파일은 MultipartFile 형태로 받으면 됨.
        MultipartFile은 이미지의 정보(크기, 원본이름...),
        지정된 경로로 파일을 전송하는 기능까지 제공함.
        */

        log.info("dto: {}", dto);
        Product product = productService.productCreate(dto);


        CommonResDTO resDTO
                = new CommonResDTO(HttpStatus.CREATED, "상품 등록 성공", product.getId());

        return new ResponseEntity<>(resDTO, HttpStatus.CREATED);
    }

    // 요청방식: GET, 요청 URL: /product/list
    // 페이징이 필요합니다. 리턴은 ProductResDto 형태로 리턴됩니다.
    // -> 클라이언트 쪽에서 페이지 번호와 한 화면에 보여질 상품 개수, 정렬 방식이 넘어옴.
    // ProductResDto(id, name, category, price, stockQuantity, imagePath)
    @GetMapping("/list")
    public ResponseEntity<?> listProduct(ProductSearchDTO dto, Pageable pageable) {
        // 페이지 번호를 number로 주면 안되고, page로 전달해야 함.
        // 사용자가 선택할 페이지 번호 -1을 클라이언트 단에서 전달해야 함.
        log.info("pageable: {}", pageable);
        log.info("dto: {}", dto);
        List<ProductResDTO> dtoList = productService.productList(dto ,pageable);

        CommonResDTO resDto = new CommonResDTO(HttpStatus.OK,
                "상품 조회 성공", dtoList);

        return ResponseEntity.ok().body(resDto);

    }

    @PreAuthorize("hasRole('ADMIN')")
    // 삭제 요청
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteProduct(@RequestParam("id") Long id)
            throws Exception {

        productService.productDelete(id);

        CommonResDTO resDTO = new CommonResDTO(HttpStatus.OK, "삭제 완료", id);

        return ResponseEntity.ok().body(resDTO);

    }

    // 단일 상품 조회
    @GetMapping("/{prodId}")
    public ResponseEntity<?> getProductById(@PathVariable Long prodId){
        log.info("prodId: {}", prodId);
        ProductResDTO productResDTO = productService.getProductInfo(prodId);

        CommonResDTO resDTO = new CommonResDTO(HttpStatus.OK,
                "제품 찾음!", productResDTO);

        return ResponseEntity.ok().body(resDTO);
    }

    @PostMapping("/updateQuantity")
    public ResponseEntity<?> updateStockQuantity(@RequestBody ProductResDTO productResDTO) {
        Long prodId = productResDTO.getId();
        int stockQuantity = productResDTO.getStockQuantity();

        log.info("/product/updateQuantity: PATCH, prodId: {}, stockQuantity: {}"
                , prodId, stockQuantity);
        productService.updateStockQuantity(prodId, stockQuantity);
        CommonResDTO resDto
                = new CommonResDTO(HttpStatus.OK, "변경 완료", prodId);
        return ResponseEntity.ok().body(resDto);
    }

    // 한 사용자의 모든 주문 내역 안에 있는 상품 정보를 리턴하는 메소드
    @PostMapping("/products")
    public ResponseEntity<?>  getProducts(@RequestBody List<Long> productIds) {
        List<ProductResDTO> productsName = productService.getProductsName(productIds);

        CommonResDTO resDTO = new CommonResDTO(HttpStatus.OK, "해당하는 모든 제품 찾음",
                productsName);

        return ResponseEntity.ok().body(resDTO);
    }

    // 주문 취소 시에 각 상품의 재고 수량을 원복하는 요청
    @PutMapping("/cancel")
    public ResponseEntity<?> cancelProduct(@RequestBody Map<Long, Integer> map) {
        log.info("/product/cancel: PUT, map: {}", map);
        productService.cancelProduct(map);
        CommonResDTO resDto
                = new CommonResDTO(HttpStatus.OK, "update completed", map);
        return ResponseEntity.ok().body(resDto);
    }

}
