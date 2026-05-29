package com.example.backend.tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    public List<String> list() {
        return tagService.findAll().stream().map(Tag::getName).toList();
    }

    @GetMapping("/popular")
    public List<Map<String, Object>> popular() {
        return tagService.findPopular().stream()
                .map(row -> Map.of("name", (Object) row[0], "count", (Object) row[1]))
                .toList();
    }
}
