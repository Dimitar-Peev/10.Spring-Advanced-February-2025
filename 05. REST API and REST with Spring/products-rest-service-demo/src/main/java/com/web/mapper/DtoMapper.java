package com.web.mapper;

import com.model.Product;
import com.web.dto.ProductResponse;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DtoMapper {

    public static ProductResponse toProductResponse(Product product) {
        return ProductResponse.builder()
                .name(product.getName())
                .quantity(product.getQuantity())
                .build();
    }
}
