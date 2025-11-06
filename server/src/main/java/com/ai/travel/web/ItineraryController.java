package com.ai.travel.web;

import com.ai.travel.dto.GenerateRequest;
import com.ai.travel.dto.ItineraryResponse;
import com.ai.travel.dto.ItineraryResponse.Activity;
import com.ai.travel.dto.ItineraryResponse.BudgetEstimate;
import com.ai.travel.dto.ItineraryResponse.DayPlan;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import com.ai.travel.service.BailianClient;

@RestController
@RequestMapping("/api/itineraries")
public class ItineraryController {

    @Autowired(required = false)
    private BailianClient bailianClient;

    // 最小可行：返回固定结构的示例数据，占位后续接入阿里百炼
    @PostMapping("/generate")
    public ResponseEntity<ItineraryResponse> generate(@Valid @RequestBody GenerateRequest req) {
        // 若已配置阿里百炼，则调用真实模型；失败或未配置回退到示例
        if (bailianClient != null && bailianClient.isConfigured()) {
            try {
                ItineraryResponse ai = bailianClient.generateItinerary(req);
                if (ai != null) {
                    return ResponseEntity.ok()
                            .header("X-AI-Source", "bailian")
                            .body(ai);
                }
            } catch (Exception ignored) { }
        }

        ItineraryResponse res = new ItineraryResponse();
        res.setItineraryId(UUID.randomUUID().toString());
        res.setDestination(req.getDestination());
        res.setDays(req.getDays());

        List<DayPlan> days = new ArrayList<>();

        DayPlan d1 = new DayPlan();
        d1.setDate("Day 1");
        List<Activity> a1 = new ArrayList<>();
        a1.add(activity("transport", "抵达 " + req.getDestination() + " 机场/车站", null, null, "09:00", "10:00", 0, "建议购买当地交通卡"));
        // 去掉硬编码的经纬度，交给前端基于名称检索或后端代理检索补全
        a1.add(activity("sight", req.getDestination() + " 市中心地标", null, null, "10:30", "12:00", 100, "拍照与简餐"));
        a1.add(activity("food", "本地特色餐厅", null, null, "12:15", "13:30", 120, "人均参考"));
        a1.add(activity("sight", "亲子友好景点", null, null, "14:30", "17:00", 200, "室内外均可"));
        d1.setActivities(a1);
        days.add(d1);

        DayPlan d2 = new DayPlan();
        d2.setDate("Day 2");
        List<Activity> a2 = new ArrayList<>();
        a2.add(activity("sight", "经典路线-景点A", null, null, "09:00", "11:30", 150, "避开高峰时段"));
        a2.add(activity("food", "美食打卡街区", null, null, "12:00", "13:30", 120, "可尝试当地特色小吃"));
        a2.add(activity("sight", "景点B/购物区", null, null, "14:30", "17:30", 0, "可选购物"));
        d2.setActivities(a2);
        days.add(d2);

        res.setDayPlans(days);

        BudgetEstimate be = new BudgetEstimate();
        int base = req.getBudget() != null ? req.getBudget() : (req.getPeople() * req.getDays() * 300);
        be.setTotal(base);
        be.setTransport((int) (base * 0.25));
        be.setLodging((int) (base * 0.35));
        be.setFood((int) (base * 0.25));
        be.setTicket((int) (base * 0.10));
        be.setOther(base - be.getTransport() - be.getLodging() - be.getFood() - be.getTicket());
        res.setBudgetEstimate(be);

        return ResponseEntity.ok()
                .header("X-AI-Source", "stub")
                .body(res);
    }

    private Activity activity(String type, String name, Double lat, Double lng,
                              String start, String end, Integer cost, String notes) {
        Activity a = new Activity();
        a.setType(type);
        a.setName(name);
        a.setLat(lat);
        a.setLng(lng);
        a.setStart(start);
        a.setEnd(end);
        a.setCostEstimate(cost);
        a.setNotes(notes);
        return a;
    }
}
