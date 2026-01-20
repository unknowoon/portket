package com.portket.app.controller;

import com.portket.app.dto.RebalancingInput;
import com.portket.app.dto.RebalancingOutput;
import com.portket.app.dto.http.DataResponse;
import com.portket.app.service.RebalancingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rebalancing")
@RequiredArgsConstructor
public class RebalancingController {
    
    private final RebalancingService rebalancingService;
    
    @PostMapping("/simulate")
    public ResponseEntity<DataResponse<RebalancingOutput>> simulateRebalancing(@Valid @RequestBody RebalancingInput input) {
        input.setSimulate(true);
        RebalancingOutput result = rebalancingService.calculateRebalancing(input);
        return ResponseEntity.ok(DataResponse.<RebalancingOutput>builder()
                .data(result)
                .inqYn(true)
                .build());
    }
    
    @PostMapping("/execute")
    public ResponseEntity<DataResponse<RebalancingOutput>> executeRebalancing(@Valid @RequestBody RebalancingInput input) {
        input.setSimulate(false);
        RebalancingOutput result = rebalancingService.executeRebalancing(input);
        return ResponseEntity.ok(DataResponse.<RebalancingOutput>builder()
                .data(result)
                .inqYn(true)
                .build());
    }
}