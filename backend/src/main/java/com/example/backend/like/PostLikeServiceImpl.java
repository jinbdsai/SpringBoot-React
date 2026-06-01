package com.example.backend.like;

import com.example.backend.like.dao.PostLikeDao;
import com.example.backend.like.dto.PostLikeVO;
import com.example.backend.post.Post;
import com.example.backend.post.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PostLikeServiceImpl implements PostLikeService {

    private final PostLikeDao postLikeDao;
    private final PostRepository postRepository;

    public PostLikeServiceImpl(PostLikeDao postLikeDao, PostRepository postRepository) {
        this.postLikeDao = postLikeDao;
        this.postRepository = postRepository;
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
        return true;
    }
}
