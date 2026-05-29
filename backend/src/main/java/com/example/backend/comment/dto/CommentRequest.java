package com.example.backend.comment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CommentRequest {
    private String content;
    private Long parentId;   // null 이면 일반 댓글, 값 있으면 대댓글
}
