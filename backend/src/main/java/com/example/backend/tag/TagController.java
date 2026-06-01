package com.example.backend.tag;

import com.example.backend.tag.dto.PopularTagResponseDTO;
import com.example.backend.tag.dto.TagDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    public List<String> list() {
        return tagService.findAll().stream().map(TagDTO::getName).toList();
    }

    @GetMapping("/popular")
    public List<PopularTagResponseDTO> popular() {
        return tagService.findPopular();
    }
}
