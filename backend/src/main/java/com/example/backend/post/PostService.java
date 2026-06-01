package com.example.backend.post;

import com.example.backend.like.PostLikeService;
import com.example.backend.post.dto.PostRequest;
import com.example.backend.post.dto.PostResponse;
import com.example.backend.tag.Tag;
import com.example.backend.tag.TagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final TagService tagService;
    private final PostLikeService likeService;

    public PostService(PostRepository postRepository, TagService tagService, PostLikeService likeService) {
        this.postRepository = postRepository;
        this.tagService = tagService;
        this.likeService = likeService;
    }

    public List<PostResponse> findAll(PostCategory category, String sort, String keyword, String currentUsername) {
        Sort s = resolveSort(sort);
        List<Post> posts;
        if (keyword != null && !keyword.trim().isEmpty()) {
            posts = postRepository.search(category, keyword.trim(), s);
        } else if (category != null) {
            posts = postRepository.findByCategory(category, s);
        } else {
            posts = postRepository.findAll(s);
        }
        return posts.stream()
                .map(p -> PostResponse.from(p, likeService.isLikedBy(p.getId(), currentUsername)))
                .toList();
    }

    public List<PostResponse> findByAuthor(String author, String currentUsername) {
        return postRepository.findByAuthorOrderByIdDesc(author).stream()
                .map(p -> PostResponse.from(p, likeService.isLikedBy(p.getId(), currentUsername)))
                .toList();
    }

    @Transactional
    public PostResponse findOne(Long id, String currentUsername) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found: " + id));
        post.incrementViewCount();
        return PostResponse.from(post, likeService.isLikedBy(id, currentUsername));
    }

    @Transactional
    public PostResponse create(String author, PostRequest request) {
        Post post = new Post(request.getTitle(), author, request.getContent(), request.getCategory());
        Set<Tag> tags = tagService.resolveOrCreate(request.getTags());
        post.replaceTags(tags);
        Post saved = postRepository.save(post);
        log.info("게시글 생성: id={}, author={}, category={}", saved.getId(), author, request.getCategory());
        return PostResponse.from(saved, false);
    }

    @Transactional
    public PostResponse update(Long id, String currentUsername, PostRequest request) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found: " + id));
        ensureOwner(post, currentUsername);
        post.update(request.getTitle(), request.getContent(), request.getCategory());
        Set<Tag> tags = tagService.resolveOrCreate(request.getTags());
        post.replaceTags(tags);
        log.info("게시글 수정: id={}, author={}", id, currentUsername);
        return PostResponse.from(post, likeService.isLikedBy(id, currentUsername));
    }

    @Transactional
    public void delete(Long id, String currentUsername) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found: " + id));
        ensureOwner(post, currentUsername);
        postRepository.delete(post);
        log.info("게시글 삭제: id={}, author={}", id, currentUsername);
    }

    private Sort resolveSort(String sort) {
        if (sort == null) sort = "latest";
        return switch (sort) {
            case "popular" -> Sort.by(Sort.Direction.DESC, "likeCount").and(Sort.by(Sort.Direction.DESC, "id"));
            case "views" -> Sort.by(Sort.Direction.DESC, "viewCount").and(Sort.by(Sort.Direction.DESC, "id"));
            case "comments" -> Sort.by(Sort.Direction.DESC, "commentCount").and(Sort.by(Sort.Direction.DESC, "id"));
            default -> Sort.by(Sort.Direction.DESC, "id");
        };
    }

    private void ensureOwner(Post post, String currentUsername) {
        if (!post.getAuthor().equals(currentUsername)) {
            throw new ForbiddenException("작성자만 수정/삭제할 수 있습니다.");
        }
    }
}
