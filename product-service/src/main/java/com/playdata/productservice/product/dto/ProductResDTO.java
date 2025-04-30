package com.playdata.productservice.product.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResDTO {

    private Long id;

    private String name;

    private String category;

    private int price;

    private int stockQuantity;

    private String imagePath;

}
