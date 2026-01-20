package com.sky.service;

import com.sky.entity.AiSuggestion;

import java.util.List;

public interface AiService {
    /**
     * 创建 AI 任务：只是入库（PENDING），返回任务ID。实际处理由 Scheduler 处理（或可立即 run）。
     */
    Long createTaskForOrder(Long orderId, Long operatorId);

    /**
     * 查询建议列表（bizType 可为 "ORDER"/"SETMEAL_OPT" 等）
     */
    List<AiSuggestion> listSuggestions(String bizType, Long bizId);

    /**
     * 根据ID获取建议详情
     */
    AiSuggestion getSuggestion(Long suggestionId);

    /**
     * 接受建议并执行落地（创建/删除/更新套餐），事务化
     */
    void acceptSuggestion(Long suggestionId, Long operatorId, boolean autoCreateSetmeal, boolean autoDeleteUnused) throws Exception;

    /**
     * 立即执行某 task（用于手动触发）
     */
    void runTaskNow(Long taskId);
}
