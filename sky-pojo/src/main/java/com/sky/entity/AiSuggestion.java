package com.sky.entity;

import lombok.Data;

import java.util.Date;

@Data
public class AiSuggestion {
    private Long id;
    private Long taskId;
    private String suggestionType;
    private String summary;
    private String suggestionJson;
    private Boolean accepted;
    private Long acceptedBy;
    private Date acceptedAt;
    private String status;
    private Date createTime;
    // getters/setters ...
    // (omitted for brevity)
}
