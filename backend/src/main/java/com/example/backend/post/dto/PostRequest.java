package com.example.backend.post.dto;

import com.example.backend.post.PostCategory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PostRequest {
    private String title;
    private String content;
    private PostCategory category;
    private List<String> tags;
}
