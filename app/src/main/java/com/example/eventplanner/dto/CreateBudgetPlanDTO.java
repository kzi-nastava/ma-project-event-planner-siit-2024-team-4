package com.example.eventplanner.dto;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class CreateBudgetPlanDTO implements Serializable {
    @SerializedName("eventId")
    private Long eventId;
    @SerializedName("itemsDTO")
    private List<BudgetItemDTO> itemsDTO;
    private Double total;

    // Constructors
    public CreateBudgetPlanDTO() {}

    public CreateBudgetPlanDTO(Long eventId, List<BudgetItemDTO> itemsDTO) {
        this.eventId = eventId;
        this.itemsDTO = itemsDTO;
    }

    // Getters and Setters
    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public List<BudgetItemDTO> getItemsDTO() {
        return itemsDTO;
    }

    public void setItemsDTO(List<BudgetItemDTO> itemsDTO) {
        this.itemsDTO = itemsDTO;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }
}
