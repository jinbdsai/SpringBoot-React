package com.example.backend.post;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByCategory(PostCategory category, Sort sort);

    List<Post> findByAuthorOrderByIdDesc(String author);
}
