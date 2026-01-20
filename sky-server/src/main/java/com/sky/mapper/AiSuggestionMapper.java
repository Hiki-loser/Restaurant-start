package com.sky.mapper;

import com.sky.entity.AiSuggestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AiSuggestionMapper {
    int insert(AiSuggestion suggestion);
    AiSuggestion selectById(@Param("id") Long id);
    List<AiSuggestion> selectByBiz(@Param("bizType") String bizType, @Param("bizId") Long bizId);
    int update(AiSuggestion suggestion);
}
