package com.portket.app.dto.http;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class DataResponse<T> {

    private T data;
    private boolean inqYn;
}
