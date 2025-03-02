package com.sparta.myselectshop.entity;

import com.sparta.myselectshop.dto.ProductMypriceRequestDto;
import com.sparta.myselectshop.dto.ProductRequestDto;
import jakarta.persistence.*;
import lombok.*;


@Entity // JPA가 관리하는 Entity 클래스로 지정
@Getter // Lombok 라이브러리를 사용하여 쉽게 필드를 가져올 수 있는 Get 메서드 제공
@Setter // Lombok 라이브러리를 사용하여 쉽게 필드값을 수정할 수 있는 Set 메서드 제공
@NoArgsConstructor // 기본 생성자를 직접 작성하지 않아도 자동으로 적용해줌
@Table(name = "product") // DB에서 매핑할 테이블의 이름을 지정
public class Product extends TimeStamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String image;

    @Column(nullable = false)
    private String link;

    @Column(nullable = false)
    private int lprice;

    @Column(nullable = false)
    private int myprice;

    public Product(ProductRequestDto requestDto) {
        this.title = requestDto.getTitle();
        this.image = requestDto.getImage();
        this.link = requestDto.getLink();
        this.lprice = requestDto.getLprice();
    }

    // 관심상품의 희망 최저가 설정 메서드
    public void update(ProductMypriceRequestDto requestDto) {
        this.myprice = requestDto.getMyprice();
    }
}
