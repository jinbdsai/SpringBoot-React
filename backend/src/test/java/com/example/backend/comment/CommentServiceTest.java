package com.example.backend.comment;

import com.example.backend.comment.dto.CommentRequest;
import com.example.backend.comment.dto.CommentResponse;
import com.example.backend.notification.NotificationService;
import com.example.backend.post.Post;
import com.example.backend.post.PostCategory;
import com.example.backend.post.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock CommentRepository commentRepository;
    @Mock PostRepository postRepository;
    @Mock NotificationService notificationService;
    @InjectMocks CommentService commentService;

    private Post post;

    @BeforeEach
    void setUp() {
        post = new Post("제목", "jk", "내용", PostCategory.FREE);
    }

    @Test
    @DisplayName("일반 댓글 생성: parentId 없이 저장")
    void create_plain_comment() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> inv.getArgument(0));

        CommentRequest req = new CommentRequest();
        req.setContent("좋은 글이네요");

        CommentResponse res = commentService.create(1L, "alice", req);

        assertThat(res.getContent()).isEqualTo("좋은 글이네요");
        assertThat(res.getAuthor()).isEqualTo("alice");
        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(captor.capture());
        assertThat(captor.getValue().getParentId()).isNull();
    }

    @Test
    @DisplayName("대댓글 생성: 부모의 parentId 가 null 이면 그 부모 ID 그대로 사용")
    void create_reply_to_top_comment() {
        Comment parent = new Comment(1L, "bob", "원댓글", null);
        ReflectionTestUtils.setField(parent, "id", 10L);

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(commentRepository.findById(10L)).thenReturn(Optional.of(parent));
        when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> inv.getArgument(0));

        CommentRequest req = new CommentRequest();
        req.setContent("대댓글");
        req.setParentId(10L);

        commentService.create(1L, "alice", req);

        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(captor.capture());
        assertThat(captor.getValue().getParentId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("대댓글의 대댓글: 깊이 1단계로 평탄화 (조부모 댓글 ID 로 저장)")
    void create_reply_flattened_to_one_level() {
        Comment grandParent = new Comment(1L, "bob", "원댓글", null);
        ReflectionTestUtils.setField(grandParent, "id", 10L);

        Comment parent = new Comment(1L, "carol", "대댓글", 10L);  // parentId=10
        ReflectionTestUtils.setField(parent, "id", 11L);

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(commentRepository.findById(11L)).thenReturn(Optional.of(parent));
        when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> inv.getArgument(0));

        CommentRequest req = new CommentRequest();
        req.setContent("대댓글의 대댓글");
        req.setParentId(11L);

        commentService.create(1L, "alice", req);

        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(captor.capture());
        assertThat(captor.getValue().getParentId()).isEqualTo(10L);  // 평탄화됨
    }

    @Test
    @DisplayName("다른 게시글에 속한 부모 댓글이면 거부")
    void create_reply_to_wrong_post_rejected() {
        Comment parent = new Comment(99L, "bob", "다른 글의 댓글", null);  // 다른 postId
        ReflectionTestUtils.setField(parent, "id", 10L);

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(commentRepository.findById(10L)).thenReturn(Optional.of(parent));

        CommentRequest req = new CommentRequest();
        req.setContent("대댓글");
        req.setParentId(10L);

        assertThatThrownBy(() -> commentService.create(1L, "alice", req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("잘못된 부모");
    }

    @Test
    @DisplayName("작성자만 수정 가능 — 타인은 SecurityException")
    void update_by_other_rejected() {
        Comment c = new Comment(1L, "bob", "원본", null);
        when(commentRepository.findById(5L)).thenReturn(Optional.of(c));

        CommentRequest req = new CommentRequest();
        req.setContent("악의적 수정");

        assertThatThrownBy(() -> commentService.update(5L, "attacker", req))
                .isInstanceOf(SecurityException.class);
    }

    @Test
    @DisplayName("삭제는 soft delete (deleted=true + content 비움)")
    void delete_is_soft() {
        Comment c = new Comment(1L, "bob", "삭제될 댓글", null);
        when(commentRepository.findById(5L)).thenReturn(Optional.of(c));
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        commentService.delete(5L, "bob");

        assertThat(c.getDeleted()).isTrue();
        assertThat(c.getContent()).isEmpty();
    }
}
