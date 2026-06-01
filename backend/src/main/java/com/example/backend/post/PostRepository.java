package com.example.backend.post;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByCategory(PostCategory category, Sort sort);

    List<Post> findByAuthorOrderByIdDesc(String author);

    @Query("""
        SELECT p FROM Post p
        WHERE (:category IS NULL OR p.category = :category)
          AND (p.title   LIKE CONCAT('%', :keyword, '%')
            OR p.content LIKE CONCAT('%', :keyword, '%'))
    """)
    List<Post> search(@Param("category") PostCategory category,
                      @Param("keyword") String keyword,
                      Sort sort);
}
