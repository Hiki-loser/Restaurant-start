package com.sky.mapper;

import com.sky.entity.AiTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AiTaskMapper {

    int insert(AiTask task);
    AiTask selectById(@Param("id") Long id);
    int update(AiTask task);
}

