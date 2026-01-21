package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.ValidationMessage;
import com.sky.context.BaseContext;
import com.sky.entity.AiSuggestion;
import com.sky.entity.AiTask;
import com.sky.mapper.*;
import com.sky.service.AiService;
import com.sky.utils.AiRequestBuilder;
import com.sky.utils.JsonSchemaValidator;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

/**
 * 说明：
 * - 使用 DeepSeek style 的 response_format=json_object，返回 choices[0].message.content 为严格 JSON（按 schema）
 * - 校验 JSON 是否满足 schema（resources/schemas/suggestion_schema.json）
 */
@Service
public class AiServiceImpl implements AiService {

    @Value("${sky.ai.api-url}")
    private String aiApiUrl;

    @Value("${sky.ai.api-key}")
    private String aiApiKey;

    @Value("${sky.ai.model}")
    private String aiModel;

    @Resource
    private OrderDetailMapper orderDetailMapper;

    @Resource
    private AiTaskMapper aiTaskMapper;

    @Resource
    private AiSuggestionMapper aiSuggestionMapper;

    @Resource
    private SetmealMapper setmealMapper;

    @Resource
    private SetmealDishMapper setmealDishMapper;

    @Resource
    private DishMapper dishMapper; // optional: for validation of dish existence

    @Resource
    private OrderMapper orderMapper;

    private static final ObjectMapper JACKSON = new ObjectMapper();
    private final JsonSchemaValidator schemaValidator;

    public AiServiceImpl() throws Exception {
        // 加载 schema（resources/schemas/suggestion_schema.json）
        this.schemaValidator = JsonSchemaValidator.fromResource("schemas/suggestion_schema.json");
    }

    @Override
    public AiTask generateSuggestions( ) throws Exception {
        Long userId = BaseContext.getCurrentId();
        List<Long> orderIds = orderMapper.getOrderIds();
        // 1. 构建订单摘要（聚合菜品数量）
        Map<Long, Integer> dishCount = new LinkedHashMap<>();
        for (Long orderId : orderIds) {
            List<Map<String, Object>> details = orderDetailMapper.selectByOrderId(orderId);
            if (details == null) continue;
            for (Map<String, Object> d : details) {
                Number dishIdNum = (Number) d.get("dish_id");
                if (dishIdNum == null) continue;
                Long dishId = dishIdNum.longValue();
                Integer num = (Integer) d.getOrDefault("number", 1);
                dishCount.put(dishId, dishCount.getOrDefault(dishId, 0) + num);
            }
        }

        // 2. 构造 user prompt 文本
        StringBuilder userPrompt = new StringBuilder("请根据以下订单汇总生成套餐建议（输出严格遵循 JSON schema）：\n");
        for (Map.Entry<Long, Integer> e : dishCount.entrySet()) {
            Long dishId = e.getKey();
            Integer qty = e.getValue();
            String dishName = dishMapper == null ? ("dish#" + dishId) : dishMapper.selectNameById(dishId); // 需要你实现 selectNameById 方法
            userPrompt.append(String.format("- %s (ID:%d) 数量: %d\n", dishName == null ? ("dish#" + dishId) : dishName, dishId, qty));
        }
        userPrompt.append("\n请输出 JSON 格式，顶级对象为 {\"suggestions\": [...] }，每个建议包含 setmealName, dishIds, quantity, totalPrice, note。");

        // 3. 系统提示（说明 schema） - 附带示例
        String systemPrompt = "你是餐厅菜单优化助手。请严格输出 JSON，不要输出任何多余文本。返回格式示例：\n"
                + "{\n  \"suggestions\": [\n    {\"setmealName\": \"示例套餐\", \"dishIds\": [3,5], \"quantity\":1, \"totalPrice\": 88.0, \"note\":\"理由\"}\n  ]\n}";

        // 4. 保存初始任务（payload 可保存订单摘要）
        Map<String, Object> payload = new HashMap<>();
        payload.put("prompt", userPrompt.toString());

        AiTask task = new AiTask();
        task.setUserId(userId);
        task.setOrderIds(JACKSON.writeValueAsString(orderIds));
        task.setStatus("PENDING");
        task.setPayload(JACKSON.writeValueAsString(payload));
        aiTaskMapper.insert(task);

        // 5. 调用 DeepSeek API
        JSONObject req = AiRequestBuilder.buildDeepseekRequest(systemPrompt, userPrompt.toString(), aiModel);
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + aiApiKey);
        headers.put("Content-Type", "application/json");

        String resp = null;
        try {
            resp = com.sky.utils.AiHttpClient.postJsonWithHeaders(aiApiUrl, req.toJSONString(), headers);
            // 保存原始 response
            task.setResponseRaw(resp);
            task.setStatus("SUCCESS");
            aiTaskMapper.update(task);
        } catch (Exception ex) {
            task.setStatus("FAILED");
            aiTaskMapper.update(task);
            throw ex;
        }

        // 6. 解析 DeepSeek 返回的 JSON（通常在 choices[0].message.content）
        String contentJson = extractContentFromAiRaw(resp);

        // 7. 校验 JSON
        Set<ValidationMessage> errors = schemaValidator.validate(contentJson);
        boolean valid = errors == null || errors.isEmpty();

        // 8. 如果不合法，插入一条 suggestion 标注 REVIEW_NEEDED 并返回
        List<AiSuggestion> saved = new ArrayList<>();
        if (!valid) {
            AiSuggestion s = new AiSuggestion();
            s.setTaskId(task.getId());
            s.setSuggestionType("SETMEAL_PROPOSAL");
            s.setSummary("AI returned invalid suggestion (schema mismatch)");
            s.setSuggestionJson(contentJson);
            s.setAccepted(false);
            s.setStatus("REVIEW_NEEDED");
            aiSuggestionMapper.insert(s);
            saved.add(s);
        } else {
            // 9. 解析 suggestions 数组并逐条入库
            JSONObject parsed = JSON.parseObject(contentJson);
            JSONArray suggestions = parsed.getJSONArray("suggestions");
            for (int i = 0; i < suggestions.size(); i++) {
                JSONObject item = suggestions.getJSONObject(i);
                AiSuggestion s = new AiSuggestion();
                s.setTaskId(task.getId());
                s.setSuggestionType("SETMEAL_PROPOSAL");
                s.setSummary(item.getString("setmealName"));
                s.setSuggestionJson(item.toJSONString());
                s.setAccepted(false);
                s.setStatus("NEW");
                aiSuggestionMapper.insert(s);
                saved.add(s);
            }
        }
        task.setSuggestions(saved);
        return task;
    }

    /**
     * 尝试从 AI raw resp 中抽取严格 JSON content。支持 DeepSeek/OpenAI 风格：
     * - root.choices[0].message.content
     * - root.choices[0].text
     * - 若 root 本身是 JSON（直接返回）
     */
    private String extractContentFromAiRaw(String raw) {
        if (raw == null) return null;
        try {
            JSONObject root = JSON.parseObject(raw);
            if (root.containsKey("choices")) {
                JSONArray choices = root.getJSONArray("choices");
                if (choices != null && !choices.isEmpty()) {
                    JSONObject first = choices.getJSONObject(0);
                    if (first.containsKey("message")) {
                        JSONObject message = first.getJSONObject("message");
                        if (message != null && message.containsKey("content")) {
                            return message.getString("content");
                        }
                    } else if (first.containsKey("text")) {
                        return first.getString("text");
                    }
                }
            }
            if (root.containsKey("content")) {
                return root.getString("content");
            }
            // fallback: return whole raw
            return raw;
        } catch (Exception ex) {
            // raw 不一定是 json，直接返回
            return raw;
        }
    }

    @Override
    public List<AiSuggestion> listSuggestions(Long taskId) {
        return aiSuggestionMapper.selectByTaskId(taskId);
    }

    @Override
    public AiSuggestion getSuggestion(Long suggestionId) {
        return aiSuggestionMapper.selectById(suggestionId);
    }

    /**
     * 采纳建议：解析 suggestion_json（单条建议 json），创建 setmeal 与 setmeal_dish
     * 并将 suggestion 标为 accepted（带事务）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acceptSuggestion(Long suggestionId) throws Exception {
        Long operatorId = BaseContext.getCurrentId();
        AiSuggestion s = aiSuggestionMapper.selectById(suggestionId);
        if (s == null) throw new IllegalArgumentException("suggestion not found");
        if (Boolean.TRUE.equals(s.getAccepted())) throw new IllegalStateException("already accepted");

        // parse suggestionJson (EXPECT single suggestion object)
        JSONObject item = JSON.parseObject(s.getSuggestionJson());
        String setmealName = item.getString("setmealName");
        JSONArray dishIds = item.getJSONArray("dishIds");
        Integer quantity = item.getInteger("quantity");
        Double totalPrice = item.getDouble("totalPrice");

        // 幂等：避免重复创建同名套餐
        Setmeal exists = setmealMapper.selectByName(setmealName);
        Long setmealId;
        if (exists != null) {
            setmealId = exists.getId();
        } else {
            // create setmeal
            Setmeal setmeal = new Setmeal();
            setmeal.setName(setmealName);
            setmeal.setDescription(item.getString("note"));
            setmeal.setPrice(totalPrice == null ? BigDecimal.ZERO : BigDecimal.valueOf(totalPrice));
            setmeal.setStatus(1);
            // 你可能需要设置 category_id，create_user 等字段（这里置默认）
            setmeal.setCategoryId(1L);
            setmealMapper.insert(setmeal);
            setmealId = setmeal.getId();
        }

        // create setmeal_dish entries (delete existing mapping for this setmeal? here append but check duplicates)
        for (int i = 0; i < dishIds.size(); i++) {
            Long dishId = dishIds.getLong(i);
            // optional validation: dish exists
            if (dishMapper != null && dishMapper.selectById(dishId) == null) {
                // 如果菜品不存在，跳过或抛异常（这里选择跳过）
                continue;
            }
            // 检查是否已存在组合记录（防重复）
            boolean existsRel = setmealDishMapper.existsBySetmealIdAndDishId(setmealId, dishId);
            if (existsRel) continue;

            SetmealDish sd = new SetmealDish();
            sd.setSetmealId(setmealId);
            sd.setDishId(dishId);
            // 尝试填充 name 和 price（冗余字段），从 dish 表读取
            if (dishMapper != null) {
                com.sky.entity.Dish d = dishMapper.selectById(dishId);
                if (d != null) {
                    sd.setName(d.getName());
                    sd.setPrice(d.getPrice());
                }
            }
            sd.setCopies(quantity == null ? 1 : quantity);
            setmealDishMapper.insert(sd);
        }

        // 标记 suggestion 为 accepted
        s.setAccepted(true);
        s.setAcceptedBy(operatorId);
        s.setAcceptedAt(new Date());
        s.setStatus("APPLIED");
        aiSuggestionMapper.update(s);
    }
}
