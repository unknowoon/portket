package com.portket.app.dto;

import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class PortfolioOutput {

    private Long id;
    private String name;
    private List<Component> components;
}
