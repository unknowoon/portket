package com.portket.app.dto;

import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class PortfolioInput {

    // 포트폴리오 이름
    private String name;

    // 구성
    List<PortfolioComponentRequest> components;
}
