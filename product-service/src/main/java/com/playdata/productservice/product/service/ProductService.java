package com.playdata.productservice.product.service;


import com.playdata.productservice.common.configs.AwsS3Config;
import com.playdata.productservice.product.dto.ProductResDTO;
import com.playdata.productservice.product.dto.ProductSaveReqDTO;
import com.playdata.productservice.product.dto.ProductSearchDTO;
import com.playdata.productservice.product.entity.Product;
import com.playdata.productservice.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    private final AwsS3Config s3Config;

    public Product productCreate(ProductSaveReqDTO dto) throws IOException {
        // 원본 이미지를 어딘가(기존에는 로컬)에 저장하고, 그 저장된 위치를 Entity에 세팅해야함.

    /*    MultipartFile productImage = dto.getProductImage();

        // 상품을 등록하는 과정에서 이미지 이름의 중복으로 인한 충돌을 방지하기 위해
        // 랜덤한 문자열을 섞어서 충돌을 막아주자.
        String uniqueFileName
                = UUID.randomUUID() + "_" + productImage.getOriginalFilename();

        // 특정 로컬 경로에 이미지를 전송하고, 그 경로를 Entity에 세팅하자.
        File file =
                new File("C:/Users/user/Desktop/Upload/" + uniqueFileName);

        // 화면단에서 전달받은 이미지를 로컬 저장소로 보내기
        try {
            productImage.transferTo(file);
        } catch (IOException e) {
            throw new RuntimeException("이미지 저장 실패ㅠㅠ");
        }*/

        // 이제는 aws의 s3에 저장되게 하자!

        MultipartFile productImage = dto.getProductImage();

        String uniqueFileName
                = UUID.randomUUID() + "_" + productImage.getOriginalFilename();

        // 더 이상 로컬 경로에 이미지를 저장하지 말고, s3 버킷에 저장하자!
        String imageUrl
                = s3Config.uploadToS3Bucket(productImage.getBytes(), uniqueFileName);


        Product product = dto.toEntity();
        // 파일명이 아닌 S3 url이 저장될 것임.
        product.setImagePath(imageUrl);

        return productRepository.save(product);
    }

    public List<ProductResDTO> productList(ProductSearchDTO dto, Pageable pageable) {

        Page<Product> all;
        if(dto.getCategory() == null){
            all = productRepository.findAll(pageable);
        }
        else if(dto.getCategory().equals("category")){
            all = productRepository.findByCategoryValue(dto.getSearchName(),pageable);
        } else {
            all = productRepository.findByNameValue(dto.getSearchName(),pageable);
        }


        return all.getContent()
                .stream().map(Product::fromEntity)
                .toList();
    }

    public void productDelete(Long id) throws Exception {
        Product product = productRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(id +"는 없는 제품"));

        productRepository.deleteById(id);
        s3Config.deleteFromS3Bucket(product.getImagePath());
    }

    public ProductResDTO prodFindById(Long prodId) {

        Product product = productRepository.findById(prodId)
                .orElseThrow(() -> new EntityNotFoundException(prodId + ""));

        return product.fromEntity();

    }

    public void updateStockQuantity(Long prodId, Integer stockQuantity) {

        Product product = productRepository.findById(prodId)
                .orElseThrow(() -> new EntityNotFoundException(prodId + ""));

        product.setStockQuantity(stockQuantity);

        productRepository.save(product);

    }

    public List<ProductResDTO> getProductsName(List<Long> productIds) {

        List<Product> products = productRepository.findByIdIn(productIds);

        return products.stream().map(Product::fromEntity).collect(Collectors.toList());
    }

    public void cancelProduct(Map<Long, Integer> map) {
        for (Long key : map.keySet()) {
            Product foundProd = productRepository.findById(key).orElseThrow(
                    () -> new EntityNotFoundException("Product with id: " + key + " not found")
            );
            int quantity = foundProd.getStockQuantity();
            foundProd.setStockQuantity(quantity + map.get(key));
            productRepository.save(foundProd);
        }
    }
}
