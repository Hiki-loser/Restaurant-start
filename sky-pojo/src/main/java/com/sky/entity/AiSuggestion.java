package com.sky.entity;

import lombok.Data;
import java.util.Date;


@Data
public class AiSuggestion {
    private Long id;
    private Long taskId;
    private String bizType;
    private Long bizId;
    private String suggestionType;
    private String content; // JSON string
    private Boolean accepted;
    private Long acceptedBy;
    private Date acceptedAt;
    private String status;
    private Date createTime;
}

