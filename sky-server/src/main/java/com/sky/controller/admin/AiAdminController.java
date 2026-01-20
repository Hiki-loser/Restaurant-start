package com.sky.constroller.admin;

import com.sky.entity.AiSuggestion;
import com.sky.result.Result;
import com.sky.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI 后台管理接口（前端只需调用查看建议并点击采纳）
 */
@RestController
@RequestMapping("/admin/ai")
public class AiAdminController {

    @Autowired
    private AiService aiService;

    @PostMapping("/tasks/orders/{orderId}")
    public Result<Long> createOrderTask(@PathVariable Long orderId, @RequestParam(required = false) Long operatorId) {
        Long id = aiService.createTaskForOrder(orderId, operatorId);
        return Result.success(id);
    }

    @PostMapping("/tasks/{taskId}/run")
    public Result<String> runTaskNow(@PathVariable Long taskId) {
        aiService.runTaskNow(taskId);
        return Result.success("started");
    }

    @GetMapping("/suggestions")
    public Result<List<AiSuggestion>> listSuggestions(@RequestParam String bizType, @RequestParam Long bizId) {
        return Result.success(aiService.listSuggestions(bizType, bizId));
    }

    @GetMapping("/suggestions/{id}")
    public Result<AiSuggestion> getSuggestion(@PathVariable Long id) {
        return Result.success(aiService.getSuggestion(id));
    }

    @PostMapping("/suggestions/{id}/accept")
    public Result<String> acceptSuggestion(@PathVariable Long id, @RequestParam Long operatorId,
                                           @RequestParam(defaultValue = "true") boolean autoCreateSetmeal,
                                           @RequestParam(defaultValue = "false") boolean autoDeleteUnused) {
        try {
            aiService.acceptSuggestion(id, operatorId, autoCreateSetmeal, autoDeleteUnused);
            return Result.success("applied");
        } catch (Exception ex) {
            return Result.error(ex.getMessage());
        }
    }
}
