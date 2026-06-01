package com.example.backend.post;

import com.example.backend.like.PostLikeService;
import com.example.backend.post.dto.PostRequest;
import com.example.backend.post.dto.PostResponse;
import com.example.backend.tag.TagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock PostRepository postRepository;
    @Mock TagService tagService;
    @Mock PostLikeService likeService;
    @InjectMocks PostService postService;

    private Post existingPost;

    @BeforeEach
    void setUp() {
        existingPost = new Post("제목", "jk", "내용", PostCategory.FREE);
        ReflectionTestUtils.setField(existingPost, "id", 1L);
    }

    @Test
    @DisplayName("게시글 생성: save 호출되고 응답이 반환된다")
    void create_success() {
        PostRequest req = new PostRequest();
        req.setTitle("새 글");
        req.setContent("새 내용");
        req.setCategory(PostCategory.QUESTION);
        req.setTags(List.of("java"));

        when(tagService.resolveOrCreate(any())).thenReturn(new HashSet<>());
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));

        PostResponse res = postService.create("jk", req);

        assertThat(res.getTitle()).isEqualTo("새 글");
        assertThat(res.getAuthor()).isEqualTo("jk");
        verify(postRepository).save(any(Post.class));
        verify(tagService).resolveOrCreate(List.of("java"));
    }

    @Test
    @DisplayName("게시글 수정: 작성자는 수정 가능")
    void update_by_author() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(existingPost));
        when(tagService.resolveOrCreate(any())).thenReturn(new HashSet<>());
        when(likeService.isLikedBy(eq(1L), anyString())).thenReturn(false);

        PostRequest req = new PostRequest();
        req.setTitle("수정 제목");
        req.setContent("수정 내용");
        req.setCategory(PostCategory.INFO);

        PostResponse res = postService.update(1L, "jk", req);

        assertThat(res.getTitle()).isEqualTo("수정 제목");
        assertThat(res.getContent()).isEqualTo("수정 내용");
    }

    @Test
    @DisplayName("게시글 수정: 타인은 ForbiddenException")
    void update_by_other_forbidden() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(existingPost));

        PostRequest req = new PostRequest();
        req.setTitle("악의적 수정");
        req.setContent("내용");

        assertThatThrownBy(() -> postService.update(1L, "attacker", req))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("작성자만");
    }

    @Test
    @DisplayName("게시글 수정: 없는 게시글이면 IllegalArgumentException")
    void update_not_found() {
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        PostRequest req = new PostRequest();
        req.setTitle("t");
        req.setContent("c");

        assertThatThrownBy(() -> postService.update(999L, "jk", req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Post not found");
    }

    @Test
    @DisplayName("게시글 삭제: 작성자는 삭제 가능")
    void delete_by_author() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(existingPost));

        postService.delete(1L, "jk");

        verify(postRepository).delete(existingPost);
    }

    @Test
    @DisplayName("게시글 삭제: 타인은 ForbiddenException")
    void delete_by_other_forbidden() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(existingPost));

        assertThatThrownBy(() -> postService.delete(1L, "attacker"))
                .isInstanceOf(ForbiddenException.class);
        verify(postRepository, never()).delete(any(Post.class));
    }

    @Test
    @DisplayName("목록 조회: 카테고리 필터 적용")
    void list_with_category() {
        when(postRepository.findByCategory(eq(PostCategory.QUESTION), any(Sort.class)))
                .thenReturn(List.of(existingPost));
        when(likeService.isLikedBy(anyLong(), any())).thenReturn(false);

        List<PostResponse> result = postService.findAll(PostCategory.QUESTION, "latest", null, null);

        assertThat(result).hasSize(1);
        verify(postRepository).findByCategory(eq(PostCategory.QUESTION), any(Sort.class));
        verify(postRepository, never()).findAll(any(Sort.class));
    }
}
