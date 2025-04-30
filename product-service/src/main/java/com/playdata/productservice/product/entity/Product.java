package com.playdata.productservice.product.entity;


import com.playdata.productservice.common.entity.BaseTimeEntity;
import com.playdata.productservice.product.dto.ProductResDTO;
import jakarta.persistence.*;
import lombok.*;

@Getter @ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name="tbl_product")
public class Product extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String category;

    private int price;

    @Setter
    private int stockQuantity;

    @Setter // 해당 필드에만 setter가 적용됨.
    private String imagePath;


    public ProductResDTO fromEntity() {


        return new ProductResDTO().builder()
                .id(this.id)
                .name(this.name)
                .category(this.category)
                .price(this.price)
                .stockQuantity(this.stockQuantity)
                .imagePath(this.imagePath)
                .build();
    }

}
