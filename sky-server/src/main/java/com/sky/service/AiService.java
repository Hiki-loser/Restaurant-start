package com.sky.service;

import com.sky.entity.AiTask;
import com.sky.entity.AiSuggestion;

import java.util.List;

public interface AiService {

    AiTask generateSuggestions() throws Exception;

    List<AiSuggestion> listSuggestions(Long taskId);

    AiSuggestion getSuggestion(Long suggestionId);

    void acceptSuggestion(Long suggestionId) throws Exception;
}
