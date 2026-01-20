package com.portket.app.dto.http;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ListResponse<T> {

    private int totalElements;
    private List<T> data;
}
