package com.example.backend.post.dto;

import com.example.backend.post.Post;
import com.example.backend.post.PostCategory;
import com.example.backend.tag.Tag;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class PostResponse {
    private Long id;
    private String title;
    private String author;
    private String content;
    private PostCategory category;
    private List<String> tags;
    private Long viewCount;
    private Long likeCount;
    private Long commentCount;
    private boolean likedByMe;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PostResponse from(Post post, boolean likedByMe) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getAuthor(),
                post.getContent(),
                post.getCategory(),
                post.getTags().stream().map(Tag::getName).sorted().toList(),
                post.getViewCount(),
                post.getLikeCount(),
                post.getCommentCount(),
                likedByMe,
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
