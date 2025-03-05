package com.sparta.myselectshop.service;

import com.sparta.myselectshop.dto.ProductMypriceRequestDto;
import com.sparta.myselectshop.dto.ProductRequestDto;
import com.sparta.myselectshop.dto.ProductResponseDto;
import com.sparta.myselectshop.entity.Product;
import com.sparta.myselectshop.entity.User;
import com.sparta.myselectshop.entity.UserRoleEnum;
import com.sparta.myselectshop.naver.dto.ItemDto;
import com.sparta.myselectshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    // 관심상품 희망 최저가 설정 API
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
        
        // 해당 상품의 희망하는 최저가 금액 설정(myprice 업데이트)
        product.update(requestDto);

        return new ProductResponseDto(product);
    }

    // 관심 상품의 현재 최저가 업데이트 API(lprice 업데이트)
    @Transactional
    public void updateBySearch(Long id, ItemDto itemDto) {
        Product product = productRepository.findById(id).orElseThrow(() ->
                new NullPointerException("해당 관심 상품이 존재하지 않습니다.")
        );

        product.updateByItemDto(itemDto);
    }
    // 관심상품 조회 API
    public Page<ProductResponseDto> getProducts(User user, int page, int size, String sortBy, boolean isAsc) {
        // 상품 데이터 페이징 및 정렬
        Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy); // 정렬 방향, 정렬 기준
        Pageable pageable = PageRequest.of(page, size, sort); // 요청 페이지, 항목 수, 정렬 방법(방향, 기준)

        // 권한에 따른 데이터 전달
        UserRoleEnum userRoleEnum = user.getRole();

        Page<Product> productList;

        if (userRoleEnum == UserRoleEnum.USER) {
            // User 권한일 경우 해당 User의 관심 상품만 페이징 및 정렬해서 반환
            productList = productRepository.findAllByUser(user, pageable);
        } else {
            // Admin 권한일 경우 전체 User의 관심 상품 모두를 페이징 및 정렬해서 반환
            productList = productRepository.findAll(pageable);
        }

        // Page<Product> 타입의 리스트를 map 을 사용하여 Page<ProductResponseDto> 타입으로 변환 후 반환
        return productList.map(ProductResponseDto::new);
    }
}
