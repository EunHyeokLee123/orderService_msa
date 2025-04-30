package com.playdata.productservice.product.dto;


import lombok.*;

@Getter @Setter
@ToString @NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSearchDTO {

    private String category;
    private String searchName;

}
