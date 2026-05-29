package com.example.backend.post;

import com.example.backend.post.dto.PostRequest;
import com.example.backend.post.dto.PostResponse;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public List<PostResponse> findAll() {
        return postRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .map(PostResponse::from)
                .toList();
    }

    @Transactional
    public PostResponse findOne(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found: " + id));
        post.incrementViewCount();
        return PostResponse.from(post);
    }

    @Transactional
    public PostResponse create(String author, PostRequest request) {
        Post post = new Post(request.getTitle(), author, request.getContent());
        return PostResponse.from(postRepository.save(post));
    }

    @Transactional
    public PostResponse update(Long id, String currentUsername, PostRequest request) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found: " + id));
        ensureOwner(post, currentUsername);
        post.update(request.getTitle(), post.getAuthor(), request.getContent());
        return PostResponse.from(post);
    }

    @Transactional
    public void delete(Long id, String currentUsername) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found: " + id));
        ensureOwner(post, currentUsername);
        postRepository.delete(post);
    }

    private void ensureOwner(Post post, String currentUsername) {
        if (!post.getAuthor().equals(currentUsername)) {
            throw new ForbiddenException("작성자만 수정/삭제할 수 있습니다.");
        }
    }
}
