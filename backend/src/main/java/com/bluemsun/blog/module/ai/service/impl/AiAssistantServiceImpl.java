package com.bluemsun.blog.module.ai.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bluemsun.blog.common.core.ApiCode;
import com.bluemsun.blog.common.exception.BizException;
import com.bluemsun.blog.module.ai.dto.AiRequest;
import com.bluemsun.blog.module.ai.dto.AiResponseDTO;
import com.bluemsun.blog.module.ai.entity.AiSession;
import com.bluemsun.blog.module.ai.mapper.AiSessionMapper;
import com.bluemsun.blog.module.ai.service.AiAssistantService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AiAssistantServiceImpl implements AiAssistantService {

    private final AiSessionMapper aiSessionMapper;
    private final ObjectMapper objectMapper;

    @Value("${ai.siliconflow.api-key}")
    private String apiKey;
    @Value("${ai.siliconflow.base-url:https://api.siliconflow.cn}")
    private String baseUrl;
    @Value("${ai.siliconflow.model:deepseek-ai/DeepSeek-V3.1-Terminus}")
    private String model;
    @Value("${ai.siliconflow.timeout-ms:15000}")
    private int timeoutMs;

    public AiAssistantServiceImpl(AiSessionMapper aiSessionMapper) {
        this.aiSessionMapper = aiSessionMapper;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiResponseDTO process(Long userId, AiRequest request) {
        String scene = request.getScene().toUpperCase(Locale.ROOT);
        String prompt = request.getContent().trim();
        String extra = StringUtils.hasText(request.getExtra()) ? request.getExtra().trim() : null;

        if (!("POLISH".equals(scene) || "EXTEND".equals(scene) || "SUMMARY".equals(scene) || "TRANSLATE".equals(scene) || "CONTINUE".equals(scene))) {
            throw new BizException(ApiCode.BAD_REQUEST, "不支持的 AI 场景");
        }

        AiCallResult callResult = callSiliconFlow(scene, prompt, extra);

        AiSession session = new AiSession();
        session.setUserId(userId);
        session.setScene(scene);
        session.setPrompt(prompt);
        session.setResponse(callResult.content);
        session.setTokenUsage(callResult.tokenUsage);
        session.setStatus("SUCCESS");
        aiSessionMapper.insert(session);

        AiResponseDTO dto = toDto(session);
        dto.setResponse(callResult.content);
        return dto;
    }

    @Override
    public List<AiResponseDTO> history(Long userId, int limit) {
        return aiSessionMapper.selectList(Wrappers.<AiSession>lambdaQuery()
                        .eq(AiSession::getUserId, userId)
                        .orderByDesc(AiSession::getCreateTime)
                        .last("LIMIT " + Math.min(limit, 50)))
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private AiResponseDTO toDto(AiSession session) {
        AiResponseDTO dto = new AiResponseDTO();
        dto.setId(session.getId());
        dto.setScene(session.getScene());
        dto.setPrompt(session.getPrompt());
        dto.setResponse(session.getResponse());
        dto.setTokenUsage(session.getTokenUsage());
        dto.setCreateTime(session.getCreateTime() != null ? session.getCreateTime() : LocalDateTime.now());
        return dto;
    }

    private AiCallResult callSiliconFlow(String scene, String prompt, String extra) {
        if (!StringUtils.hasText(apiKey)) {
            throw new BizException(ApiCode.BAD_REQUEST, "AI 服务未配置 API Key");
        }
        try {
            String systemPrompt = buildSystemPrompt(scene, extra);

            Map<String, Object> payload = new HashMap<>();
            payload.put("model", model);
            // messages
            var messages = new java.util.ArrayList<Map<String, String>>();
            Map<String, String> sys = new HashMap<>();
            sys.put("role", "system");
            sys.put("content", systemPrompt);
            messages.add(sys);
            Map<String, String> user = new HashMap<>();
            user.put("role", "user");
            user.put("content", prompt);
            messages.add(user);
            payload.put("messages", messages);
            payload.put("temperature", 0.7);
            payload.put("stream", false);

            String body = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(normalizeBaseUrl(baseUrl) + "/v1/chat/completions"))
                    .timeout(Duration.ofMillis(Math.max(5000, timeoutMs)))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(Math.max(5000, timeoutMs)))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                throw new BizException(ApiCode.SERVER_ERROR, "AI 服务调用失败：" + response.statusCode());
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode choices = root.path("choices");
            if (choices.isMissingNode() || !choices.isArray() || choices.size() == 0) {
                throw new BizException(ApiCode.SERVER_ERROR, "AI 服务无有效返回");
            }
            String content = choices.get(0).path("message").path("content").asText("");
            if (!StringUtils.hasText(content)) {
                content = choices.get(0).path("delta").path("content").asText("");
            }
            int tokenUsage = root.path("usage").path("total_tokens").asInt(0);
            if (tokenUsage == 0) {
                int pt = root.path("usage").path("prompt_tokens").asInt(0);
                int ct = root.path("usage").path("completion_tokens").asInt(0);
                tokenUsage = pt + ct;
            }
            AiCallResult result = new AiCallResult();
            result.content = content;
            result.tokenUsage = tokenUsage;
            return result;
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException(ApiCode.SERVER_ERROR, "AI 服务异常：" + e.getMessage());
        }
    }

    private String buildSystemPrompt(String scene, String extra) {
        switch (scene) {
            case "POLISH":
                return "你是擅长中文文字润色与表达优化的助手，请在不改变原意的前提下提升清晰度、逻辑性与可读性，保留关键事实。输出仅返回润色后的文本。";
            case "EXTEND":
                return "你是资深技术写作者，请基于输入内容进行结构化扩展，从动机、步骤、注意事项与实践建议四方面补充，避免无信息堆砌。输出为扩展后的文本。";
            case "SUMMARY":
                return "你是专业的编辑，请提炼输入内容的要点，输出精炼摘要（100-150字），用中文，避免口水话与无意义形容。";
            case "TRANSLATE":
                String target = (extra != null && extra.toLowerCase(Locale.ROOT).contains("en")) ? "英语" : "中文";
                return "你是严谨的翻译助手，请精准翻译为" + target + "，保留术语一致性与人类可读性，不添加解释说明。只输出译文。";
            case "CONTINUE":
                return "你是续写助手，请在保持风格一致的前提下继续内容，注意承接上下文并给出具体细节与示例，避免重复与空话。";
            default:
                return "You are a helpful assistant.";
        }
    }

    private String normalizeBaseUrl(String base) {
        if (base == null || base.isEmpty()) {
            return "https://api.siliconflow.cn";
        }
        if (base.endsWith("/")) {
            return base.substring(0, base.length() - 1);
        }
        return base;
    }

    private static class AiCallResult {
        private String content;
        private int tokenUsage;
    }
}


