package com.sky.entity;

import com.sky.entity.AiSuggestion;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class AiTask {
    private Long id;
    private Long userId;
    private String orderIds; // json array string
    private String status;
    private String payload;
    private String responseRaw;
    private Date createTime;
    private Date updateTime;

    // transient: suggestions loaded after parsing
    private List<AiSuggestion> suggestions;

    // getters/setters ...
    // (omitted for brevity)
}