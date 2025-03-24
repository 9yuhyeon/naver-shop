package com.sparta.myselectshop.scheduler;

import com.sparta.myselectshop.entity.Product;
import com.sparta.myselectshop.naver.dto.ItemDto;
import com.sparta.myselectshop.naver.service.NaverApiService;
import com.sparta.myselectshop.repository.ProductRepository;
import com.sparta.myselectshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j(topic = "Scheduler")
@Component
@RequiredArgsConstructor
public class Scheduler {

    private final NaverApiService naverApiService;
    private final ProductService productService;
    private final ProductRepository productRepository;

    // 초, 분, 시, 일, 월, 주 순서
    @Scheduled(cron = "0 0 1 * * *") // 매일 새벽 1시
    public void updatePrice() throws InterruptedException {
        log.info("가격 업데이트 실행");

        // 관심 상품 목록 전체 조회
        List<Product> productList = productRepository.findAll();

        for (Product product : productList) {
            TimeUnit.SECONDS.sleep(1); // 1초에 한 번씩 조회(Naver 검색 제한)

            String title = product.getTitle(); // 해당 관심 상품의 제목 추출

            List<ItemDto> itemDtoList = naverApiService.searchItems(title); // 관심 상품의 제목을 naver 검색 api로 검색

            // 검색 결과 목록이 하나라도 있을 경우 최저가 업데이트
            if (itemDtoList.size() > 0) {
                ItemDto itemDto = itemDtoList.get(0); // 검색 결과 중 최상단 상품(검색 정확도가 가장 높다고 가정)
                Long id = product.getId(); // 최저가를 업데이트 할 상품의 id 값 조회

                try {
                    productService.updateBySearch(id, itemDto); // 나의 관심 상품의 최저가를 검색 상품의 최저가로 업데이트
                } catch (Exception e) {
                    log.error(id + " : " + e.getMessage());
                }
            }
        }
    }
}
