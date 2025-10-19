package com.example.eventplanner.dto;

import com.google.gson.annotations.SerializedName;

public class ChatMessageDTO {
    
    @SerializedName("content")
    private String content;
    
    @SerializedName("sender")
    private String sender;
    
    @SerializedName("type")
    private String type;
    
    @SerializedName("chatRoom")
    private String chatRoom;
    
    public ChatMessageDTO() {}
    
    public ChatMessageDTO(String content, String sender, String type) {
        this.content = content;
        this.sender = sender;
        this.type = type;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getSender() {
        return sender;
    }
    
    public void setSender(String sender) {
        this.sender = sender;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getChatRoom() {
        return chatRoom;
    }
    
    public void setChatRoom(String chatRoom) {
        this.chatRoom = chatRoom;
    }
}
