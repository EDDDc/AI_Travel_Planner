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

    // 最小可行：占位 ASR。接收音频文件并返回固定识别文本
    @PostMapping(value = "/asr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> asr(@RequestParam("audio") MultipartFile audio,
                                                   @RequestParam(value = "lang", required = false) String lang) {
        Map<String, Object> res = new HashMap<>();
        res.put("text", "这是示例识别结果（占位），后续接入讯飞 ASR");
        res.put("bytes", audio != null ? audio.getSize() : 0);
        res.put("lang", lang == null ? "auto" : lang);
        return ResponseEntity.ok(res);
    }
}

