package com.portket.app.controller;

import com.portket.app.dto.InstrumentOutput;
import com.portket.app.dto.TagInput;
import com.portket.app.dto.TagOutput;
import com.portket.app.dto.http.ListResponse;
import com.portket.app.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {
    private final TagService tagService;

    @GetMapping
    public ResponseEntity<ListResponse<TagOutput>> list() {
        return ResponseEntity.ok(tagService.list());
    }

    @GetMapping("/{name}")
    public ResponseEntity<ListResponse<InstrumentOutput>> get(@PathVariable String name) {
        return ResponseEntity.ok(tagService.get(name));
    }

    @PostMapping
    public ResponseEntity<Void> createTag(@RequestBody TagInput req) {
        tagService.createTag(req);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteTagAll(@RequestBody TagInput req) {
        tagService.deleteTagAll(req);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/{ticker}/from/{name}")
    public ResponseEntity<Void> deleteTag(@PathVariable String ticker, @PathVariable String name) {
        tagService.deleteTag(ticker, name);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/{ticker}/from/{name}")
    public ResponseEntity<Void> addTag(@PathVariable String ticker, @PathVariable String name) {
        tagService.addTag(ticker, name);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
