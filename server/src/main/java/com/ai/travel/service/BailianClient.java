package com.ai.travel.service;

import com.ai.travel.dto.GenerateRequest;
import com.ai.travel.dto.ItineraryResponse;
import com.ai.travel.service.dto.OpenAIChatRequest;
import com.ai.travel.service.dto.OpenAIChatResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class BailianClient {

    @Value("${app.ai.bailian.apiKey:}")
    private String apiKey;

    @Value("${app.ai.bailian.baseUrl:https://dashscope.aliyuncs.com/compatible-mode/v1}")
    private String baseUrl;

    @Value("${app.ai.bailian.model:qwen-turbo}")
    private String model;

    private final ObjectMapper mapper = new ObjectMapper();

    public boolean isConfigured() {
        return StringUtils.hasText(apiKey);
    }

    public ItineraryResponse generateItinerary(GenerateRequest req) throws Exception {
        OpenAIChatRequest payload = new OpenAIChatRequest();
        payload.setModel(model);
        payload.setTemperature(0.7);

        List<OpenAIChatRequest.Message> messages = new ArrayList<>();
        messages.add(new OpenAIChatRequest.Message("system", systemPrompt()));
        messages.add(new OpenAIChatRequest.Message("user", buildUserPrompt(req)));
        payload.setMessages(messages);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        String url = normalizeBase(baseUrl) + "/chat/completions";
        RestTemplate rt = new RestTemplate();
        ResponseEntity<OpenAIChatResponse> resp = rt.exchange(url, HttpMethod.POST,
                new HttpEntity<>(payload, headers), OpenAIChatResponse.class);

        OpenAIChatResponse body = resp.getBody();
        if (body == null || body.getChoices() == null || body.getChoices().isEmpty()) {
            throw new IllegalStateException("Empty response from Bailian");
        }
        String content = body.getChoices().get(0).getMessage().getContent();
        // 期望 content 为 JSON；若不是，尝试提取代码块
        String json = extractJson(content);
        JsonNode node = mapper.readTree(json);
        // 将 JSON 映射到 ItineraryResponse（字段名需契合 systemPrompt 的输出约定）
        ItineraryResponse res = mapper.convertValue(node, ItineraryResponse.class);
        if (res.getDestination() == null) res.setDestination(req.getDestination());
        if (res.getDays() == null) res.setDays(req.getDays());
        return res;
    }

    private String systemPrompt() {
        return "你是旅行规划助手。根据用户给定的目的地/天数/人数/预算/偏好，生成 JSON，不要输出多余说明。" +
                "严格输出以下 JSON 结构: {\n" +
                "  \"itineraryId\": string,\n" +
                "  \"destination\": string,\n" +
                "  \"days\": number,\n" +
                "  \"dayPlans\": [{ \"date\": string, \"activities\": [{ \"type\": string, \"name\": string, \"lat\"?: number, \"lng\"?: number, \"start\": string, \"end\": string, \"costEstimate\"?: number, \"notes\"?: string }] }],\n" +
                "  \"budgetEstimate\": { \"total\": number, \"transport\": number, \"lodging\": number, \"food\": number, \"ticket\": number, \"other\": number }\n" +
                "}";
    }

    private String buildUserPrompt(GenerateRequest r) {
        String prefs = (r.getPreferences() == null || r.getPreferences().isEmpty()) ? "无" : String.join("/", r.getPreferences());
        String pace = (r.getPace() == null || r.getPace().isBlank()) ? "均衡" : r.getPace();
        String datePart = (r.getStartDate() != null && !r.getStartDate().isBlank() && r.getEndDate() != null && !r.getEndDate().isBlank())
                ? (r.getStartDate() + " 至 " + r.getEndDate())
                : "未提供";
        return String.format(
                "目的地:%s\n日期范围:%s\n天数:%d\n人数:%d\n预算:%s\n偏好:%s\n节奏:%s\n要求: 按日期输出每日时间安排（标注日期/或D1..），给出餐饮/景点/交通建议，包含费用估算与注意事项。地点尽量附经纬度；无法给出经纬度时返回可检索名称。",
                r.getDestination(), datePart, r.getDays(), r.getPeople(), r.getBudget() == null ? "未提供" : r.getBudget().toString(), prefs, pace);
    }

    private String normalizeBase(String b) {
        return b.endsWith("/") ? b.substring(0, b.length()-1) : b;
    }

    private String extractJson(String content) {
        String c = content == null ? "" : content.trim();
        if (c.startsWith("{")) return c;
        int s = c.indexOf("{");
        int e = c.lastIndexOf("}");
        if (s >= 0 && e > s) return c.substring(s, e + 1);
        return c; // 尝试直接解析
    }
}
