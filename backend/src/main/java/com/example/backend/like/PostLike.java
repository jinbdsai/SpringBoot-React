package com.example.backend.like;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "post_likes")
@IdClass(PostLike.PK.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostLike {

    @Id
    @Column(name = "post_id")
    private Long postId;

    @Id
    @Column(name = "username", length = 50)
    private String username;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public PostLike(Long postId, String username) {
        this.postId = postId;
        this.username = username;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public static class PK implements Serializable {
        private Long postId;
        private String username;

        public PK() {}

        public PK(Long postId, String username) {
            this.postId = postId;
            this.username = username;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PK pk)) return false;
            return Objects.equals(postId, pk.postId) && Objects.equals(username, pk.username);
        }

        @Override
        public int hashCode() {
            return Objects.hash(postId, username);
        }
    }
}
