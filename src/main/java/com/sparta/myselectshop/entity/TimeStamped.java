package com.sparta.myselectshop.entity;


import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass // 해당 추상 클래스를 상속받는 Entity는 아래 멤버변수를 컬럼으로 자동 인식됨
@EntityListeners(AuditingEntityListener.class) // Auditing(감사)는 데이터 생성,수정,삭제 등 변경 사항을 추적하는 기능
public abstract class TimeStamped {
    // Auditing(감사)을 하는 TimeStamped는 따로 인스턴스를 생성할 필요가 없기에 추상 클래스로 선언하여 생성을 막음.
    @CreatedDate // Entity 객체가 생성되어 저장될 때의 시간을 자동 저장
    @Column(updatable = false)
    @Temporal(TemporalType.TIMESTAMP) // 날짜 타입 매핑할 때 사용하는 애너테이션
    private LocalDateTime createdAt;

    @LastModifiedDate // Entity 객체가 수정될 때 수정된 시간을 자동 저장
    @Column
    @Temporal(TemporalType.TIMESTAMP) // 날짜 타입 매핑할 때 사용하는 애너테이션
    private LocalDateTime modifiedAt;
}
