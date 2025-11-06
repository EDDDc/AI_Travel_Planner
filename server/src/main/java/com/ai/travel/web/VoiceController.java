package com.ai.travel.web;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/voice")
public class VoiceController {

    private final com.ai.travel.service.XFYunAsrClient xfClient;

    public VoiceController(com.ai.travel.service.XFYunAsrClient xfClient) {
        this.xfClient = xfClient;
    }

    // 最小可行：占位 ASR。接收音频文件并返回固定识别文本
    @PostMapping(value = "/asr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> asr(@RequestParam("audio") MultipartFile audio,
                                                   @RequestParam(value = "lang", required = false) String lang) {
        Map<String, Object> res = new HashMap<>();
        try {
            if (audio == null || audio.isEmpty()) {
                res.put("error", "缺少音频文件");
                return ResponseEntity.badRequest().body(res);
            }
            byte[] bytes = audio.getBytes();
            String text;
            if (xfClient != null && xfClient.isConfigured()) {
                text = xfClient.recognizeMp3(bytes);
            } else {
                text = "（占位）请配置讯飞 APP_ID/API_KEY/API_SECRET 以启用真实识别";
            }
            res.put("text", text);
            res.put("bytes", audio.getSize());
            res.put("lang", lang == null ? "auto" : lang);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }
}
