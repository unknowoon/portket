package com.portket.app.controller;

import com.portket.app.dto.*;
import com.portket.app.dto.http.ListResponse;
import com.portket.app.dto.http.PaginatedResponse;
import com.portket.app.service.InstrumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/instruments")
@RequiredArgsConstructor
public class InstrumentController {

    private final InstrumentService instrumentService;

    @GetMapping
    public ResponseEntity<PaginatedResponse<InstrumentListInqOutput>> list(@Valid @ModelAttribute InstrumentListInqInput req) {
        return ResponseEntity.ok(instrumentService.list(req));
    }

    @GetMapping("/{ticker}")
    public ResponseEntity<InstrumentOutput> get(@PathVariable String ticker) {
        return ResponseEntity.ok(instrumentService.get(ticker));
    }

    @GetMapping("/search/{name}")
    public ResponseEntity<ListResponse<InstrumentOutput>> search(@PathVariable String name) {
        return ResponseEntity.ok(instrumentService.getLike(name));
    }
}
