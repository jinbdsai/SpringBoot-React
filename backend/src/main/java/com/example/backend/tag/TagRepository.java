package com.example.backend.tag;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name);

    List<Tag> findAllByOrderByNameAsc();

    @Query("""
        SELECT t.name, COUNT(p.id)
        FROM Post p JOIN p.tags t
        GROUP BY t.name
        ORDER BY COUNT(p.id) DESC
    """)
    List<Object[]> findPopular();
}
