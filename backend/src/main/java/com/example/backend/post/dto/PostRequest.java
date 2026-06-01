package com.example.backend.post.dto;

import com.example.backend.post.PostCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PostRequest {

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 200, message = "제목은 최대 200자까지 가능합니다.")
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    private String content;

    private PostCategory category;

    private List<String> tags;
}
