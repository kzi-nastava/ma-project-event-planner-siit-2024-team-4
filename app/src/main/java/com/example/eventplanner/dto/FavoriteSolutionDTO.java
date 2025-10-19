package com.example.eventplanner.dto;

import com.google.gson.annotations.SerializedName;

public class FavoriteSolutionDTO {
    @SerializedName("solution")
    private ServiceDTO solution;
    
    public FavoriteSolutionDTO() {}
    
    public FavoriteSolutionDTO(ServiceDTO solution) {
        this.solution = solution;
    }
    
    public ServiceDTO getSolution() {
        return solution;
    }
    
    public void setSolution(ServiceDTO solution) {
        this.solution = solution;
    }
}
