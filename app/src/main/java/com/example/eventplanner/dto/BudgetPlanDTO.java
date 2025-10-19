package com.example.eventplanner.dto;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class BudgetPlanDTO implements Serializable {
    private Long id;
    @SerializedName("eventId")
    private Long eventId;
    private List<BudgetItemDTO> items;
    private Double total;

    // Constructors
    public BudgetPlanDTO() {}

    public BudgetPlanDTO(Long eventId, List<BudgetItemDTO> items, Double total) {
        this.eventId = eventId;
        this.items = items;
        this.total = total;
    }

    public BudgetPlanDTO(Long id, Long eventId, List<BudgetItemDTO> items, Double total) {
        this.id = id;
        this.eventId = eventId;
        this.items = items;
        this.total = total;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public List<BudgetItemDTO> getItems() {
        return items;
    }

    public void setItems(List<BudgetItemDTO> items) {
        this.items = items;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }
}
