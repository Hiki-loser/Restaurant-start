package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sky.entity.AiSuggestion;
import com.sky.entity.AiTask;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.AiSuggestionMapper;
import com.sky.mapper.AiTaskMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.service.AiService;
import com.sky.utils.AiHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

/**
 * AiService 实现（核心逻辑）
 *
 * 说明：部分 Mapper/实体请使用你已有的实现。我只写 service 逻辑，调用你现成的 mapper。
 */
@Service
public class AiServiceImpl implements AiService {

    @Resource
    private AiTaskMapper aiTaskMapper;

    @Resource
    private AiSuggestionMapper aiSuggestionMapper;

    @Resource
    private OrderDetailMapper orderDetailMapper; // 读取 order_detail 信息的 mapper（你已有）

    @Resource
    private SetmealMapper setmealMapper;

    @Resource
    private SetmealDishMapper setmealDishMapper;

    @Value("${sky.ai.api-url}")
    private String aiApiUrl;

    @Value("${sky.ai.api-key}")
    private String aiApiKey;

    @Value("${sky.ai.model:gpt-5-thinking-mini}")
    private String aiModel;

    @Override
    public Long createTaskForOrder(Long orderId, Long operatorId) {
        AiTask t = new AiTask();
        t.setBizType("ORDER_SUMMARY");
        t.setBizId(orderId);
        t.setPayload("{}");
        t.setStatus("PENDING");
        t.setRetries(0);
        aiTaskMapper.insert(t);
        return t.getId();
    }

    @Override
    public List<AiSuggestion> listSuggestions(String bizType, Long bizId) {
        return aiSuggestionMapper.selectByBiz(bizType, bizId);
    }

    @Override
    public AiSuggestion getSuggestion(Long suggestionId) {
        return aiSuggestionMapper.selectById(suggestionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acceptSuggestion(Long suggestionId, Long operatorId, boolean autoCreateSetmeal, boolean autoDeleteUnused) throws Exception {
        AiSuggestion s = aiSuggestionMapper.selectById(suggestionId);
        if (s == null) throw new IllegalArgumentException("suggestion not found");
        if (Boolean.TRUE.equals(s.getAccepted())) throw new IllegalStateException("already accepted");

        JSONObject content = JSON.parseObject(s.getContent());
        // content 可能包含 keys: "setmeal_proposals":[], "delete_setmeals":[], "procurements":[]
        if (autoCreateSetmeal && content.containsKey("setmeal_proposals")) {
            JSONArray proposals = content.getJSONArray("setmeal_proposals");
            for (int i = 0; i < proposals.size(); i++) {
                JSONObject p = proposals.getJSONObject(i);
                // Create Setmeal
                Setmeal setmeal = new Setmeal();
                setmeal.setName(p.getString("title"));

                setmeal.setPrice( p.getDouble("suggested_price") != null?
                        BigDecimal.valueOf(p.getDouble("suggested_price")) :
                        BigDecimal.ZERO);

                setmeal.setDescription(p.getString("rationale"));
                setmeal.setStatus(1);
                setmealMapper.insert(setmeal);

                // Create setmeal_dish entries
                JSONArray items = p.getJSONArray("items");
                if (items != null) {
                    for (int j = 0; j < items.size(); j++) {
                        JSONObject it = items.getJSONObject(j);
                        SetmealDish sd = new SetmealDish();
                        sd.setSetmealId(setmeal.getId());
                        sd.setDishId(it.getLong("dish_id"));
                        sd.setName(it.getString("name"));

                        setmeal.setPrice( p.getDouble("suggested_price") != null?
                                BigDecimal.valueOf(p.getDouble("suggested_price")) :
                                BigDecimal.ZERO);

                        sd.setCopies(it.getInteger("copies") == null ? 1 : it.getInteger("copies"));
                        setmealDishMapper.insert(sd);
                    }
                }
            }
        }

        if (autoDeleteUnused && content.containsKey("delete_setmeals")) {
            // 内容为要删除的 setmeal id list
            JSONArray del = content.getJSONArray("delete_setmeals");
            for (int i = 0; i < del.size(); i++) {
                Long sid = del.getLong(i);
                // 这里调用 SetmealMapper 的删除方法（假定存在 deleteById）
                try {
                    setmealMapper.deleteById(sid);
                } catch (Exception ex) {
                    // 若没有 delete 方法，请在 mapper 中实现 deleteById
                    // 为了幂等性，这里 catch 异常并继续处理
                }
            }
        }

        // 标记 accepted
        s.setAccepted(true);
        s.setAcceptedBy(operatorId);
        s.setAcceptedAt(new Date());
        s.setStatus("APPLIED");
        aiSuggestionMapper.update(s);
    }

    @Override
    public void runTaskNow(Long taskId) {
        AiTask task = aiTaskMapper.selectById(taskId);
        if (task == null) throw new IllegalArgumentException("task not found");

        // 读取订单详情并构造 payload
        Long orderId = task.getBizId();
        List<Map<String, Object>> details = orderDetailMapper.selectByOrderId(orderId); // 你已有方法或实现
        JSONObject payload = new JSONObject();
        payload.put("taskId", taskId);
        payload.put("orderId", orderId);
        payload.put("model", aiModel);
        payload.put("items", details);

        // constraints 可以从配置中取或硬编码
        JSONObject constraints = new JSONObject();
        constraints.put("min_margin", 0.20);
        constraints.put("max_items_in_setmeal", 4);
        payload.put("constraints", constraints);

        // Build prompt wrapper per AI API expectation
        JSONObject requestJson = buildAiRequest(payload);

        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + aiApiKey);
            headers.put("Content-Type", "application/json");

            String resp = AiHttpClient.postJsonWithHeaders(aiApiUrl, requestJson.toJSONString(), headers);
            // 保存响应
            task.setResponseRaw(resp);
            task.setStatus("SUCCESS");
            aiTaskMapper.update(task);

            // 尝试解析并保存 suggestion(s)
            parseAndSaveSuggestions(task.getId(), task.getBizType(), task.getBizId(), resp);

        } catch (Exception ex) {
            task.setStatus("FAILED");
            task.setLastError(ex.getMessage());
            task.setRetries(task.getRetries() == null ? 1 : task.getRetries() + 1);
            aiTaskMapper.update(task);
        }
    }

    private JSONObject buildAiRequest(JSONObject payload) {
        // 根据你实际对接的 AI API 格式调整。下面构造一个通用 wrapper：
        JSONObject req = new JSONObject();
        req.put("model", aiModel);

        // We use single prompt approach: instruct model to output strict JSON schema (see guidance)
        String system = "You are an assistant for restaurant menu optimization. OUTPUT MUST be valid JSON following the schema provided. DO NOT output any explanation.";
        String user = "INPUT_PAYLOAD: " + payload.toJSONString();

        JSONArray messages = new JSONArray();
        JSONObject s = new JSONObject();
        s.put("role", "system"); s.put("content", system);
        JSONObject u = new JSONObject();
        u.put("role", "user"); u.put("content", user);
        messages.add(s); messages.add(u);

        req.put("messages", messages);
        // temperature/other params:
        req.put("temperature", 0.2);
        req.put("max_tokens", 1500);
        return req;
    }

    /**
     * 简单解析 AI 返回，保存 ai_suggestion（仅示例，实际需严格校验 JSON Schema）
     */
    private void parseAndSaveSuggestions(Long taskId, String bizType, Long bizId, String aiRaw) {
        if (aiRaw == null) return;
        try {
            JSONObject root = JSON.parseObject(aiRaw);
            // 若你的 AI 调用返回在 choices[0].message.content 中，请根据返回格式调整这里的解析
            // 我们尝试几种常见的路径：
            String contentJson = null;
            if (root.containsKey("choices")) {
                JSONArray choices = root.getJSONArray("choices");
                if (choices != null && !choices.isEmpty()) {
                    JSONObject first = choices.getJSONObject(0);
                    if (first.containsKey("message")) {
                        contentJson = first.getJSONObject("message").getString("content");
                    } else if (first.containsKey("text")) {
                        contentJson = first.getString("text");
                    }
                }
            } else if (root.containsKey("content")) {
                contentJson = root.getString("content");
            } else {
                contentJson = aiRaw;
            }

            // 强制期望 contentJson 为严格 JSON（包含 setmeal_proposals 等）
            JSONObject parsed = JSON.parseObject(contentJson);

            AiSuggestion s = new AiSuggestion();
            s.setTaskId(taskId);
            s.setBizType(bizType == null ? "ORDER_SUMMARY" : bizType);
            s.setBizId(bizId);
            s.setSuggestionType(parsed.containsKey("setmeal_proposals") ? "SETMEAL_PROPOSAL" : "GENERAL");
            s.setContent(parsed.toJSONString());
            s.setAccepted(false);
            s.setStatus("NEW");
            aiSuggestionMapper.insert(s);

            // 更新 task 的 result_id
            AiTask t = new AiTask();
            t.setId(taskId);
            t.setResultId(s.getId());
            aiTaskMapper.update(t);

        } catch (Exception ex) {
            // 若解析失败则记录原始 response（上一段已保存），并让人工复核
            // 这里不抛出异常
        }
    }
}
