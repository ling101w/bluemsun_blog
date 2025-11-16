package com.bluemsun.blog.module.ai.service;

import com.bluemsun.blog.module.ai.dto.AiRequest;
import com.bluemsun.blog.module.ai.dto.AiResponseDTO;

import java.util.List;

public interface AiAssistantService {

    AiResponseDTO process(Long userId, AiRequest request);

    List<AiResponseDTO> history(Long userId, int limit);
}


