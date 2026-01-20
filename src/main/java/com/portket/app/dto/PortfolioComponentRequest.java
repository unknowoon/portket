package com.portket.app.dto;

import com.portket.app.constant.ComponentType;
import lombok.*;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class PortfolioComponentRequest {

    // 티커/태그 이름
    private String name;

    // 컴포넌트 타입
    private ComponentType componentType;

    // 비중
    private BigDecimal weight;
}
