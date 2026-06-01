package com.example.backend.like.dao;

import com.example.backend.like.dto.PostLikeVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PostLikeDao {

    int countByPostIdAndUsername(@Param("postId") Long postId,
                                 @Param("username") String username);

    int insertLike(PostLikeVO vo);

    int deleteByPostIdAndUsername(@Param("postId") Long postId,
                                  @Param("username") String username);
}
