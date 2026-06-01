package com.example.backend.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CommentRequest {

    @NotBlank(message = "댓글 내용은 필수입니다.")
    @Size(max = 1000, message = "댓글은 최대 1000자까지 가능합니다.")
    private String content;

    private Long parentId;   // null 이면 일반 댓글, 값 있으면 대댓글
}
