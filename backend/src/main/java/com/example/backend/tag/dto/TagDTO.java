package com.example.backend.tag.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TagDTO {
    private Long id;
    private String name;

    public static TagDTO from(TagVO vo) {
        return new TagDTO(vo.getId(), vo.getName());
    }
}
