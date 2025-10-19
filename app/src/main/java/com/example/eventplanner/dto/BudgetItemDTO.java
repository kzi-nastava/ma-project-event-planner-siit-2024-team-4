package com.example.eventplanner.dto;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class BudgetItemDTO implements Serializable {
    private Long id;
    @SerializedName("categoryId")
    private Long categoryId;
    private Double amount;
    @SerializedName("purchaseId")
    private Long purchaseId;
    @SerializedName("reservationId")
    private Long reservationId;
    @SerializedName("budgetPlanId")
    private Long budgetPlanId;

    // Constructors
    public BudgetItemDTO() {}

    public BudgetItemDTO(Long categoryId, Double amount) {
        this.categoryId = categoryId;
        this.amount = amount;
    }

    public BudgetItemDTO(Long id, Long categoryId, Double amount, Long purchaseId, Long reservationId, Long budgetPlanId) {
        this.id = id;
        this.categoryId = categoryId;
        this.amount = amount;
        this.purchaseId = purchaseId;
        this.reservationId = reservationId;
        this.budgetPlanId = budgetPlanId;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Long getPurchaseId() {
        return purchaseId;
    }

    public void setPurchaseId(Long purchaseId) {
        this.purchaseId = purchaseId;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public Long getBudgetPlanId() {
        return budgetPlanId;
    }

    public void setBudgetPlanId(Long budgetPlanId) {
        this.budgetPlanId = budgetPlanId;
    }
}
