package com.example.eventplanner.dto;

import com.google.gson.annotations.SerializedName;

public class CreateReviewDTO {
    @SerializedName("userId")
    private Long userId;
    
    @SerializedName("rating")
    private Double rating;
    
    @SerializedName("comment")
    private String comment;
    
    @SerializedName("solutionId")
    private Long solutionId;
    
    public CreateReviewDTO() {}
    
    public CreateReviewDTO(Long userId, Double rating, String comment, Long solutionId) {
        this.userId = userId;
        this.rating = rating;
        this.comment = comment;
        this.solutionId = solutionId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Double getRating() {
        return rating;
    }
    
    public void setRating(Double rating) {
        this.rating = rating;
    }
    
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    public Long getSolutionId() {
        return solutionId;
    }
    
    public void setSolutionId(Long solutionId) {
        this.solutionId = solutionId;
    }
}
