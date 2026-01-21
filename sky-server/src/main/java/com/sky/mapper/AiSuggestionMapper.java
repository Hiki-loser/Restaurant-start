package com.sky.mapper;

import com.sky.entity.AiSuggestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AiSuggestionMapper {

    /**
     * 插入建议并回填 id
     */
    int insert(AiSuggestion suggestion);

    /**
     * 更新建议（按 id 更新可变字段）
     */
    int update(AiSuggestion suggestion);

    /**
     * 根据 id 查询
     */
    AiSuggestion selectById(@Param("id") Long id);

    /**
     * 根据 taskId 查询该任务下的所有建议（按时间倒序）
     */
    List<AiSuggestion> selectByTaskId(@Param("taskId") Long taskId);

    /**
     * 查询未被采纳的建议（status = NEW / REVIEW_NEEDED）
     */
    List<AiSuggestion> selectByStatus(@Param("status") String status);

    /**
     * 标记为 accepted（方便原子更新）
     */
    int markAccepted(@Param("id") Long id, @Param("acceptedBy") Long acceptedBy, @Param("acceptedAt") java.util.Date acceptedAt);
}
