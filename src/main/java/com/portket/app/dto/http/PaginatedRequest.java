package com.portket.app.dto.http;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PaginatedRequest {
    private int page = 1;
    private int size = 10;
}
