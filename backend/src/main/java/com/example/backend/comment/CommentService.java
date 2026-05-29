package com.example.backend.comment;

import com.example.backend.comment.dto.CommentRequest;
import com.example.backend.comment.dto.CommentResponse;
import com.example.backend.post.Post;
import com.example.backend.post.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    public CommentService(CommentRepository commentRepository, PostRepository postRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
    }

    public List<CommentResponse> findByPost(Long postId) {
        return commentRepository.findByPostIdOrderByIdAsc(postId)
                .stream()
                .map(CommentResponse::from)
                .toList();
    }

    @Transactional
    public CommentResponse create(Long postId, String username, CommentRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found: " + postId));
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("댓글 내용을 입력해주세요.");
        }
        if (request.getParentId() != null) {
            Comment parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("부모 댓글을 찾을 수 없습니다."));
            if (!parent.getPostId().equals(postId)) {
                throw new IllegalArgumentException("잘못된 부모 댓글입니다.");
            }
            // 깊이 1단계까지만 허용: 부모의 parentId 가 있으면 그것의 parentId 로 평탄화
            Long actualParent = parent.getParentId() != null ? parent.getParentId() : parent.getId();
            request.setParentId(actualParent);
        }
        Comment saved = commentRepository.save(
                new Comment(postId, username, request.getContent().trim(), request.getParentId())
        );
        post.incrementCommentCount();
        return CommentResponse.from(saved);
    }

    @Transactional
    public CommentResponse update(Long commentId, String username, CommentRequest request) {
        Comment c = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found: " + commentId));
        if (!c.getAuthor().equals(username)) {
            throw new SecurityException("작성자만 수정할 수 있습니다.");
        }
        if (Boolean.TRUE.equals(c.getDeleted())) {
            throw new IllegalArgumentException("삭제된 댓글입니다.");
        }
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("댓글 내용을 입력해주세요.");
        }
        c.edit(request.getContent().trim());
        return CommentResponse.from(c);
    }

    @Transactional
    public void delete(Long commentId, String username) {
        Comment c = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found: " + commentId));
        if (!c.getAuthor().equals(username)) {
            throw new SecurityException("작성자만 삭제할 수 있습니다.");
        }
        c.softDelete();
        Post post = postRepository.findById(c.getPostId()).orElse(null);
        if (post != null) post.decrementCommentCount();
    }
}
