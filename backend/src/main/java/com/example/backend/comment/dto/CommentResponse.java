package com.example.backend.comment.dto;

import com.example.backend.comment.Comment;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CommentResponse {
    private Long id;
    private Long postId;
    private String author;
    private String content;
    private Long parentId;
    private Boolean deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CommentResponse from(Comment c) {
        return new CommentResponse(
                c.getId(), c.getPostId(), c.getAuthor(), c.getContent(),
                c.getParentId(), c.getDeleted(), c.getCreatedAt(), c.getUpdatedAt()
        );
    }
}
