package com.example.backend.tag.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PopularTagResponseDTO {
    private String name;
    private Long count;

    public static PopularTagResponseDTO from(PopularTagVO vo) {
        return new PopularTagResponseDTO(vo.getName(), vo.getCount());
    }
}
