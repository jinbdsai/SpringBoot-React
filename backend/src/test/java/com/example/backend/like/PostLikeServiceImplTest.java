package com.example.backend.like;

import com.example.backend.like.dao.PostLikeDao;
import com.example.backend.like.dto.PostLikeVO;
import com.example.backend.notification.NotificationService;
import com.example.backend.post.Post;
import com.example.backend.post.PostCategory;
import com.example.backend.post.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostLikeServiceImplTest {

    @Mock PostLikeDao postLikeDao;
    @Mock PostRepository postRepository;
    @Mock NotificationService notificationService;
    @InjectMocks PostLikeServiceImpl likeService;

    private Post post;

    @BeforeEach
    void setUp() {
        post = new Post("제목", "jk", "내용", PostCategory.FREE);
    }

    @Test
    @DisplayName("isLikedBy: username null 이면 false (DB 조회 안 함)")
    void isLikedBy_null_username() {
        boolean result = likeService.isLikedBy(1L, null);

        assertThat(result).isFalse();
        verify(postLikeDao, never()).countByPostIdAndUsername(any(), any());
    }

    @Test
    @DisplayName("좋아요 추가 성공: insert 호출 + likeCount 증가")
    void like_success() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postLikeDao.countByPostIdAndUsername(1L, "alice")).thenReturn(0);

        boolean result = likeService.like(1L, "alice");

        assertThat(result).isTrue();
        assertThat(post.getLikeCount()).isEqualTo(1L);
        verify(postLikeDao).insertLike(any(PostLikeVO.class));
    }

    @Test
    @DisplayName("중복 좋아요는 false 반환 + insert 호출 안 됨 + 카운트 그대로")
    void like_duplicate_returns_false() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postLikeDao.countByPostIdAndUsername(1L, "alice")).thenReturn(1);

        boolean result = likeService.like(1L, "alice");

        assertThat(result).isFalse();
        assertThat(post.getLikeCount()).isEqualTo(0L);
        verify(postLikeDao, never()).insertLike(any());
    }

    @Test
    @DisplayName("좋아요 취소 성공: delete 호출 + likeCount 감소")
    void unlike_success() {
        post.incrementLikeCount();  // 시작 카운트 1
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postLikeDao.countByPostIdAndUsername(1L, "alice")).thenReturn(1);

        boolean result = likeService.unlike(1L, "alice");

        assertThat(result).isTrue();
        assertThat(post.getLikeCount()).isEqualTo(0L);
        verify(postLikeDao).deleteByPostIdAndUsername(1L, "alice");
    }

    @Test
    @DisplayName("좋아요 안 한 상태에서 unlike 는 false")
    void unlike_when_not_liked_returns_false() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postLikeDao.countByPostIdAndUsername(1L, "alice")).thenReturn(0);

        boolean result = likeService.unlike(1L, "alice");

        assertThat(result).isFalse();
        verify(postLikeDao, never()).deleteByPostIdAndUsername(any(), any());
    }

    @Test
    @DisplayName("없는 게시글이면 IllegalArgumentException")
    void like_post_not_found() {
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> likeService.like(999L, "alice"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Post not found");
    }
}
