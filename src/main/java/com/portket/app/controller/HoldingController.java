package com.portket.app.controller;

import com.portket.app.dto.HoldingListInqInput;
import com.portket.app.dto.HoldingListInqOutput;
import com.portket.app.dto.http.DataResponse;
import com.portket.app.service.HoldingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/holdings")
@RequiredArgsConstructor
public class HoldingController {

    private final HoldingService holdingService;

    @GetMapping("/{name}")
    public ResponseEntity<DataResponse<HoldingListInqOutput>> list(@PathVariable String name) {
        return ResponseEntity.ok(holdingService.list(name).toResponse());
    }
}
