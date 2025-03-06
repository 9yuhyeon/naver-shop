package com.sparta.myselectshop.service;

import com.sparta.myselectshop.dto.ProductMypriceRequestDto;
import com.sparta.myselectshop.dto.ProductRequestDto;
import com.sparta.myselectshop.dto.ProductResponseDto;
import com.sparta.myselectshop.entity.*;
import com.sparta.myselectshop.naver.dto.ItemDto;
import com.sparta.myselectshop.repository.FolderRepository;
import com.sparta.myselectshop.repository.ProductFolderRepository;
import com.sparta.myselectshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.SortDirection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final FolderRepository folderRepository;
    private final ProductFolderRepository productFolderRepository;

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
    // 관심 상품(Product)를 가져올 때 연관된 폴더(Folder) 리스트가 포함되어 있는데 폴더를 조회하는 시점을 필요에 따라 조회로 가정
    // 1:N 관계 default FetchType은 Lazy(지연 로딩)이므로 트랜잭션 환경이 필요함(수정/삭제)가 아니기에 readOnly 옵션 적용
    @Transactional(readOnly = true)
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

    // 관심상품 폴더 등록 API
    public void addFolder(Long productId, Long folderId, User user) {
        Product product = productRepository.findById(productId).orElseThrow(
                () -> new NullPointerException("해당 상품이 존재하지 않습니다.")
        );

        Folder folder = folderRepository.findById(folderId).orElseThrow(
                () -> new NullPointerException("해당 폴더가 존재하지 않습니다.")
        );

        // Product와 Folder 사용자 검증(User)
        if (!product.getUser().getId().equals(user.getId()) || !folder.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("회원님의 관심 상품이 아니거나, 회원님의 폴더가 아닙니다.");
        }

        // 해당 폴더에 해당 상품을 이미 추가했는지 검증
        Optional<ProductFolder> overLapFolder = productFolderRepository.findByProductAndFolder(product, folder);

        if (overLapFolder.isPresent()) {
            throw new IllegalArgumentException("중복된 폴더입니다.");
        }

        // 상품, 폴더 중간 테이블에 저장
        productFolderRepository.save(new ProductFolder(product, folder));
    }

    // 폴더별 관심상품 목록 조회 API
    public Page<ProductResponseDto> getProductsInFolder(Long folderId, int page, int size, String sortBy, boolean isAsc, User user) {
        Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        // 해당 User의 특정 폴더에 담긴 관심 상품 조회 -> folder_id를 사용하여 중간 테이블에 있는 해당 folder_id와 연결된 product_id를 페이징 처리하여 가져옴
        Page<Product> productList = productRepository.findAllByUserAndProductFolderList_FolderId(user, folderId, pageable);

        Page<ProductResponseDto> responseDtoList = productList.map(ProductResponseDto::new);

        return responseDtoList;
    }
}
