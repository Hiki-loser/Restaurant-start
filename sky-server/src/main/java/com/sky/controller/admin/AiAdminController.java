package com.sky.controller.admin;

import com.sky.entity.AiSuggestion;
import com.sky.entity.AiTask;
import com.sky.service.AiService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI 后台管理接口（批量发送订单生成建议 → 列表 → 采纳）
 */
@RestController
@RequestMapping("/admin/ai")
@Api(tags = "AI 后台管理接口")
public class AiAdminController {

    @Autowired
    private AiService aiService;

    /**
     * 批量生成套餐建议（一次性为多个订单生成建议）
     * 请求 body: [orderId1, orderId2, ...]
     */
    @PostMapping("/generate")
    @ApiOperation("批量生成套餐建议")
    public AiTask generateForOrders() throws Exception {

        return aiService.generateSuggestions();

    }

    /**
     * 列出某任务下的建议
     */
    @GetMapping("/tasks/{taskId}/suggestions")
    @ApiOperation("列出某任务下的建议")
    public List<AiSuggestion> listSuggestions(@PathVariable Long taskId) {
        return aiService.listSuggestions(taskId);
    }

    /**
     * 获取单条建议详情
     */
    @GetMapping("/suggestions/{id}")
    @ApiOperation("获取单条建议详情")
    public AiSuggestion getSuggestion(@PathVariable Long id) {
        return aiService.getSuggestion(id);
    }

    /**
     * 采纳一条建议（operatorId 为当前操作者）
     */
    @PostMapping("/suggestions/{id}/accept")
    @ApiOperation("采纳一条建议")
    public String accept(@PathVariable Long id) {
        try {
            aiService.acceptSuggestion(id);
            return "applied";
        } catch (Exception ex) {
            return "error: " + ex.getMessage();
        }
    }
}
