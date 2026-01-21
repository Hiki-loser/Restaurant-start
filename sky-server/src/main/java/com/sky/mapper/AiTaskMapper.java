package com.sky.mapper;

import com.sky.entity.AiTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AiTaskMapper {

    /**
     * 插入任务并回填 id（useGeneratedKeys = true）
     */
    int insert(AiTask task);

    /**
     * 更新任务（按 id 更新可变字段）
     */
    int update(AiTask task);

    /**
     * 根据 id 查询
     */
    AiTask selectById(@Param("id") Long id);

    /**
     * 根据 userId 查询任务列表（可按时间倒序）
     */
    List<AiTask> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据状态查询任务（例如 PENDING / SUCCESS / FAILED）
     */
    List<AiTask> selectByStatus(@Param("status") String status);

    /**
     * 简单的删除（物理删除）——视需要启用或注释
     */
    int deleteById(@Param("id") Long id);
}
