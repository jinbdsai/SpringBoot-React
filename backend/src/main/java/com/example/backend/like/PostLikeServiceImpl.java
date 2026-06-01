package com.example.backend.like;

import com.example.backend.like.dao.PostLikeDao;
import com.example.backend.like.dto.PostLikeVO;
import com.example.backend.notification.NotificationService;
import com.example.backend.post.Post;
import com.example.backend.post.PostRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class PostLikeServiceImpl implements PostLikeService {

    private final PostLikeDao postLikeDao;
    private final PostRepository postRepository;
    private final NotificationService notificationService;

    public PostLikeServiceImpl(PostLikeDao postLikeDao,
                               PostRepository postRepository,
                               NotificationService notificationService) {
        this.postLikeDao = postLikeDao;
        this.postRepository = postRepository;
        this.notificationService = notificationService;
    }

    @Override
    public boolean isLikedBy(Long postId, String username) {
        if (username == null) return false;
        return postLikeDao.countByPostIdAndUsername(postId, username) > 0;
    }

    @Override
    @Transactional
    public boolean like(Long postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found: " + postId));
        if (postLikeDao.countByPostIdAndUsername(postId, username) > 0) {
            return false;
        }
        PostLikeVO vo = new PostLikeVO();
        vo.setPostId(postId);
        vo.setUsername(username);
        postLikeDao.insertLike(vo);
        post.incrementLikeCount();
        log.info("좋아요 추가: postId={}, username={}", postId, username);
        notificationService.notifyLike(post.getAuthor(), username, postId, post.getTitle());
        return true;
    }

    @Override
    @Transactional
    public boolean unlike(Long postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found: " + postId));
        if (postLikeDao.countByPostIdAndUsername(postId, username) == 0) {
            return false;
        }
        postLikeDao.deleteByPostIdAndUsername(postId, username);
        post.decrementLikeCount();
        log.info("좋아요 취소: postId={}, username={}", postId, username);
        return true;
    }
}
