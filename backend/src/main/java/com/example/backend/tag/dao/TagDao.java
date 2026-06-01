package com.example.backend.tag.dao;

import com.example.backend.tag.dto.PopularTagVO;
import com.example.backend.tag.dto.TagVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TagDao {
    List<TagVO> selectAllOrderByName();

    List<PopularTagVO> selectPopular();
}
