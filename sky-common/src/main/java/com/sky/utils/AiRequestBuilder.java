package com.sky.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * 构造 DeepSeek (or OpenAI-like) 的 request JSON。
 * 我们使用 messages + response_format={"type":"json_object"} 的方式请求严格 JSON 输出。
 */
public class AiRequestBuilder {

    /**
     * 构建请求体（DeepSeek 风格）
     * @param system  系统提示（说明 json schema / 输出格式）
     * @param user    用户提示（包含订单摘要）
     * @param model   模型名
     * @return JSONObject 可序列化为字符串
     */
    public static JSONObject buildDeepseekRequest(String system, String user, String model) {
        JSONObject req = new JSONObject();
        req.put("model", model);

        JSONArray messages = new JSONArray();
        JSONObject s = new JSONObject(); s.put("role", "system"); s.put("content", system);
        JSONObject u = new JSONObject(); u.put("role", "user"); u.put("content", user);
        messages.add(s); messages.add(u);
        req.put("messages", messages);

        // DeepSeek strict json mode
        JSONObject responseFormat = new JSONObject();
        responseFormat.put("type", "json_object");
        req.put("response_format", responseFormat);

        // optional params
        req.put("temperature", 0.0);
        req.put("max_tokens", 1200);

        return req;
    }
}
