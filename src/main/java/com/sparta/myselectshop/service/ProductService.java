package com.sparta.myselectshop.service;

import com.sparta.myselectshop.dto.ProductMypriceRequestDto;
import com.sparta.myselectshop.dto.ProductRequestDto;
import com.sparta.myselectshop.dto.ProductResponseDto;
import com.sparta.myselectshop.entity.Product;
import com.sparta.myselectshop.entity.User;
import com.sparta.myselectshop.naver.dto.ItemDto;
import com.sparta.myselectshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    // 최저가 설정 최소 금액
    public static final int MIN_MY_PRICE = 100;

    // 관심상품 등록 API
    public ProductResponseDto createProduct(ProductRequestDto requestDto, User user) {
        // RequestDTO -> Entity -> save
        Product savedProduct = productRepository.save(new Product(requestDto, user));

        // savedEntity -> ResponseDTO
        return new ProductResponseDto(savedProduct);
    }

    // 관심상품 희망 최저가 업데이트 API
    @Transactional
    public ProductResponseDto updateProduct(Long id, ProductMypriceRequestDto requestDto) {
        // 희망하는 최저가
        int myprice = requestDto.getMyprice();

        // 최저가 설정이 가능한 금액인지(100원 이상) 검증
        if (myprice < MIN_MY_PRICE) {
            throw new IllegalArgumentException("유효하지 않은 관심 가격입니다. 최소 " + MIN_MY_PRICE + "이상으로 설정해 주세요.");
        }

        // 최저가 업데이트를 원하는 해당 상품 조회
        Product product = productRepository.findById(id).orElseThrow(() ->
            new IllegalArgumentException("해당 상품이 존재하지 않습니다.")
        );
        
        // 해당 상품의 희망하는 최저가 금액 설정
        product.update(requestDto);

        return new ProductResponseDto(product);
    }

    // 관심 상품의 최신 정보의 최저가로 업데이트 API(최저가 수정)
    @Transactional
    public void updateBySearch(Long id, ItemDto itemDto) {
        Product product = productRepository.findById(id).orElseThrow(() ->
                new NullPointerException("해당 관심 상품이 존재하지 않습니다.")
        );

        product.updateByItemDto(itemDto);
    }
    // 관심상품 조회 API
    public List<ProductResponseDto> getProducts(User user) {
        List<Product> productList = productRepository.findAllByUser(user);

        List<ProductResponseDto> responseDtoList = new ArrayList<>();
        for (Product product : productList) {
            responseDtoList.add(new ProductResponseDto(product));
        }

        return responseDtoList;
    }
}
