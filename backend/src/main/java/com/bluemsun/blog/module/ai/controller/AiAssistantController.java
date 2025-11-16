package com.bluemsun.blog.module.ai.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.bluemsun.blog.common.core.ApiResponse;
import com.bluemsun.blog.module.ai.dto.AiRequest;
import com.bluemsun.blog.module.ai.dto.AiResponseDTO;
import com.bluemsun.blog.module.ai.service.AiAssistantService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/ai/assistant")
public class AiAssistantController {

    private final AiAssistantService aiAssistantService;

    public AiAssistantController(AiAssistantService aiAssistantService) {
        this.aiAssistantService = aiAssistantService;
    }

    /** POST /ai/assistant - 智能助手生成内容 */
    @SaCheckLogin
    @PostMapping
    public ApiResponse<AiResponseDTO> process(@Valid @RequestBody AiRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        AiResponseDTO response = aiAssistantService.process(userId, request);
        return ApiResponse.success(response, "AI 生成成功");
    }

    /** GET /ai/assistant/history - 查询智能助手交互历史 */
    @SaCheckLogin
    @GetMapping("/history")
    public ApiResponse<List<AiResponseDTO>> history(@RequestParam(defaultValue = "10") int limit) {
        Long userId = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(aiAssistantService.history(userId, limit));
    }
}


