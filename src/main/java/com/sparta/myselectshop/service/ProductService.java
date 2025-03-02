package com.sparta.myselectshop.service;

import com.sparta.myselectshop.dto.ProductRequestDto;
import com.sparta.myselectshop.dto.ProductResponseDto;
import com.sparta.myselectshop.entity.Product;
import com.sparta.myselectshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    // 관심상품 등록 API
    public ProductResponseDto createProduct(ProductRequestDto requestDto) {
        // RequestDTO -> Entity -> save
        Product savedProduct = productRepository.save(new Product(requestDto));

        // savedEntity -> ResponseDTO
        return new ProductResponseDto(savedProduct);
    }
}
