package com.example.backend.post;

import com.example.backend.tag.Tag;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 100)
    private String author;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostCategory category = PostCategory.FREE;

    @Column(nullable = false)
    private Long viewCount = 0L;

    @Column(nullable = false)
    private Long likeCount = 0L;

    @Column(nullable = false)
    private Long commentCount = 0L;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "post_tags",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Post(String title, String author, String content, PostCategory category) {
        this.title = title;
        this.author = author;
        this.content = content;
        this.category = category == null ? PostCategory.FREE : category;
        this.viewCount = 0L;
        this.likeCount = 0L;
        this.commentCount = 0L;
    }

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.viewCount == null) this.viewCount = 0L;
        if (this.likeCount == null) this.likeCount = 0L;
        if (this.commentCount == null) this.commentCount = 0L;
        if (this.category == null) this.category = PostCategory.FREE;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void update(String title, String content, PostCategory category) {
        this.title = title;
        this.content = content;
        if (category != null) this.category = category;
    }

    public void incrementViewCount() {
        this.viewCount = (this.viewCount == null ? 0L : this.viewCount) + 1L;
    }

    public void incrementLikeCount() {
        this.likeCount = (this.likeCount == null ? 0L : this.likeCount) + 1L;
    }

    public void decrementLikeCount() {
        long current = this.likeCount == null ? 0L : this.likeCount;
        this.likeCount = Math.max(0L, current - 1L);
    }

    public void incrementCommentCount() {
        this.commentCount = (this.commentCount == null ? 0L : this.commentCount) + 1L;
    }

    public void decrementCommentCount() {
        long current = this.commentCount == null ? 0L : this.commentCount;
        this.commentCount = Math.max(0L, current - 1L);
    }

    public void replaceTags(Set<Tag> newTags) {
        this.tags.clear();
        if (newTags != null) this.tags.addAll(newTags);
    }
}
