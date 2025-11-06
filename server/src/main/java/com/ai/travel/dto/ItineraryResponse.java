package com.ai.travel.dto;

import java.util.List;

public class ItineraryResponse {
    private String itineraryId;
    private String destination;
    private Integer days;
    private List<DayPlan> dayPlans;
    private BudgetEstimate budgetEstimate;

    public String getItineraryId() { return itineraryId; }
    public void setItineraryId(String itineraryId) { this.itineraryId = itineraryId; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public Integer getDays() { return days; }
    public void setDays(Integer days) { this.days = days; }
    public List<DayPlan> getDayPlans() { return dayPlans; }
    public void setDayPlans(List<DayPlan> dayPlans) { this.dayPlans = dayPlans; }
    public BudgetEstimate getBudgetEstimate() { return budgetEstimate; }
    public void setBudgetEstimate(BudgetEstimate budgetEstimate) { this.budgetEstimate = budgetEstimate; }

    public static class DayPlan {
        private String date; // 可选，v1 用文本
        private List<Activity> activities;

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public List<Activity> getActivities() { return activities; }
        public void setActivities(List<Activity> activities) { this.activities = activities; }
    }

    public static class Activity {
        private String type; // sight/food/transport/rest
        private String name;
        private Double lat;
        private Double lng;
        private String start;
        private String end;
        private Integer costEstimate;
        private String notes;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Double getLat() { return lat; }
        public void setLat(Double lat) { this.lat = lat; }
        public Double getLng() { return lng; }
        public void setLng(Double lng) { this.lng = lng; }
        public String getStart() { return start; }
        public void setStart(String start) { this.start = start; }
        public String getEnd() { return end; }
        public void setEnd(String end) { this.end = end; }
        public Integer getCostEstimate() { return costEstimate; }
        public void setCostEstimate(Integer costEstimate) { this.costEstimate = costEstimate; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public static class BudgetEstimate {
        private Integer total;
        private Integer transport;
        private Integer lodging;
        private Integer food;
        private Integer ticket;
        private Integer other;

        public Integer getTotal() { return total; }
        public void setTotal(Integer total) { this.total = total; }
        public Integer getTransport() { return transport; }
        public void setTransport(Integer transport) { this.transport = transport; }
        public Integer getLodging() { return lodging; }
        public void setLodging(Integer lodging) { this.lodging = lodging; }
        public Integer getFood() { return food; }
        public void setFood(Integer food) { this.food = food; }
        public Integer getTicket() { return ticket; }
        public void setTicket(Integer ticket) { this.ticket = ticket; }
        public Integer getOther() { return other; }
        public void setOther(Integer other) { this.other = other; }
    }
}

