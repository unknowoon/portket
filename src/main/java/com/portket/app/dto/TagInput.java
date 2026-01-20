package com.portket.app.dto;

import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class TagInput {
    private String name;
    private List<String> instruments;
}
