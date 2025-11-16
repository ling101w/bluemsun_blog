package com.bluemsun.blog.common.controller;

import com.bluemsun.blog.common.core.ApiCode;
import com.bluemsun.blog.common.core.ApiResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@RestController
@RequestMapping("/common")
public class CommonController {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /** GET /common/bing-daily - 获取必应每日一图地址 */
    @GetMapping("/bing-daily")
    public ApiResponse<String> bingDaily() {
        String fallback = "https://www.bing.com/th?id=OHR.Default_D.jpg";
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://www.bing.com/HPImageArchive.aspx?format=js&n=1"))
                    .timeout(Duration.ofSeconds(8))
                    .GET()
                    .build();
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                return ApiResponse.success(fallback, "使用兜底图");
            }
            JsonNode root = objectMapper.readTree(response.body());
            String url = root.path("images").path(0).path("url").asText(null);
            if (url == null || url.isEmpty()) {
                return ApiResponse.success(fallback, "使用兜底图");
            }
            String full = url.startsWith("http") ? url : "https://www.bing.com" + url;
            return ApiResponse.success(full);
        } catch (Exception e) {
            return ApiResponse.fail(ApiCode.SERVER_ERROR, "获取必应每日一图失败：" + e.getMessage());
        }
    }
}


