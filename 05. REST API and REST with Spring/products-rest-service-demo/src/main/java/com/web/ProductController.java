package com.web;

import com.model.Product;
import com.service.ProductService;
import com.web.dto.NewProductRequest;
import com.web.dto.ProductResponse;
import com.web.mapper.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(Paths.API_V1_BASE_PATH + "products")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {

        List<ProductResponse> list = productService.getAllProducts()
                .stream()
                .map(DtoMapper::toProductResponse)
                .toList();

        return ResponseEntity
                .status(HttpStatus.OK)
                 .body(list);
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createNewProduct(@RequestBody NewProductRequest newProductRequest) {

        Product product = productService.createNewProduct(newProductRequest);

        ProductResponse productResponse = DtoMapper.toProductResponse(product);

        // ResponseEntity == HTTP response

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header("Application-name", "Spring Boot Project")
                .body(productResponse);
    }
}
