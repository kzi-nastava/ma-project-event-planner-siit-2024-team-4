package com.example.eventplanner.dto;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class UpdateBudgetPlanDTO implements Serializable {
    private Long id;
    @SerializedName("itemsDTO")
    private List<BudgetItemDTO> itemsDTO;
    private Double total;

    // Constructors
    public UpdateBudgetPlanDTO() {}

    public UpdateBudgetPlanDTO(Long id, List<BudgetItemDTO> itemsDTO) {
        this.id = id;
        this.itemsDTO = itemsDTO;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
