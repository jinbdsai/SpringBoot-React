package com.example.backend.tag;

import com.example.backend.tag.dto.PopularTagResponseDTO;
import com.example.backend.tag.dto.TagDTO;

import java.util.List;
import java.util.Set;

public interface TagService {

    List<TagDTO> findAll();

    List<PopularTagResponseDTO> findPopular();

    Set<Tag> resolveOrCreate(List<String> names);
}
