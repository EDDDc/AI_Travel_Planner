package com.ai.travel.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/api/places")
public class PlacesController {

    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${app.map.amap.key:}")
    private String amapKey;

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> search(@RequestParam("q") String q) {
        if (!StringUtils.hasText(q)) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "missing query param q");
            return ResponseEntity.badRequest().body(err);
        }

        try {
            if (!StringUtils.hasText(amapKey)) {
                return ResponseEntity.ok(stubResults(q));
            }

            String url = "https://restapi.amap.com/v3/place/text?keywords=" +
                    URLEncoder.encode(q, StandardCharsets.UTF_8) +
                    "&key=" + URLEncoder.encode(amapKey, StandardCharsets.UTF_8) +
                    "&offset=10&page=1&extensions=base";

            RestTemplate rt = new RestTemplate();
            String body = rt.getForObject(url, String.class);

            List<Map<String, Object>> items = new ArrayList<>();
            if (body != null) {
                JsonNode root = mapper.readTree(body);
                JsonNode pois = root.path("pois");
                if (pois.isArray()) {
                    for (JsonNode p : pois) {
                        String id = optText(p, "id");
                        String name = optText(p, "name");
                        String address = optText(p, "address");
                        String loc = optText(p, "location"); // lng,lat
                        Double lat = null, lng = null;
                        if (loc != null && loc.contains(",")) {
                            String[] parts = loc.split(",");
                            if (parts.length == 2) {
                                try {
                                    lng = Double.parseDouble(parts[0]);
                                    lat = Double.parseDouble(parts[1]);
                                } catch (NumberFormatException ignored) { }
                            }
                        }
                        Map<String, Object> one = new HashMap<>();
                        one.put("id", id);
                        one.put("name", name);
                        one.put("address", address);
                        one.put("lat", lat);
                        one.put("lng", lng);
                        items.add(one);
                    }
                }
            }

            Map<String, Object> res = new HashMap<>();
            res.put("source", "amap");
            res.put("query", q);
            res.put("items", items);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            // 失败时退回到本地示例，保证最小可用
            return ResponseEntity.ok(stubResults(q));
        }
    }

    private Map<String, Object> stubResults(String q) {
        Map<String, Object> res = new HashMap<>();
        res.put("source", "stub");
        res.put("query", q);
        List<Map<String, Object>> items = new ArrayList<>();
        items.add(item("poi_stub_1", q + " 地标A", 39.9042, 116.4074, "示例地址A"));
        items.add(item("poi_stub_2", q + " 美食街", 35.6895, 139.6917, "示例地址B"));
        res.put("items", items);
        return res;
    }

    private Map<String, Object> item(String id, String name, Double lat, Double lng, String address) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id);
        m.put("name", name);
        m.put("lat", lat);
        m.put("lng", lng);
        m.put("address", address);
        return m;
    }

    private String optText(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return v != null && !v.isNull() ? v.asText() : null;
    }
}

