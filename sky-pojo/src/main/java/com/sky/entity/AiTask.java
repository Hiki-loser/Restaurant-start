package com.sky.entity;

import lombok.Data;
import java.util.Date;

@Data
public class AiTask {
    private Long id;
    private String bizType;
    private Long bizId;
    private String payload; // JSON string
    private String status;
    private Integer retries;
    private String responseRaw;
    private Long resultId;
    private String lastError;
    private Date createdAt;
    private Date updatedAt;
}
