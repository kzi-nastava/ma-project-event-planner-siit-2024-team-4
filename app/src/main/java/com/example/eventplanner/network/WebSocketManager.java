package com.example.eventplanner.network;

import android.util.Log;
import com.example.eventplanner.dto.ChatMessageDTO;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.example.eventplanner.config.ApiConfig;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class WebSocketManager {
    private static final String TAG = "WebSocketManager";
    // For emulator use: ws://10.0.2.2:8080/chat
    // For real device use: ws://[YOUR_COMPUTER_IP]:8080/chat
    private static final String WEBSOCKET_URL = ApiConfig.WEBSOCKET_URL;
    
    private WebSocketListener listener;
    private boolean isConnected = false;
    private String currentUsername;
    private String chatRoom;
    private List<ChatMessageDTO> messageHistory = new ArrayList<>();
    private ScheduledExecutorService scheduler;
    private Gson gson = new Gson();
    private WebSocketClient webSocketClient;
    
    public interface WebSocketListener {
        void onMessageReceived(ChatMessageDTO message);
        void onConnectionStatusChanged(boolean connected);
        void onError(String error);
    }
    
    public WebSocketManager(WebSocketListener listener) {
        this.listener = listener;
        this.scheduler = Executors.newScheduledThreadPool(1);
    }
    
    public void setChatRoom(String contextType, String contextId) {
        if (contextType != null && contextId != null) {
            this.chatRoom = contextType.toLowerCase() + "_" + contextId;
        } else {
            this.chatRoom = "global";
        }
        Log.d(TAG, "Chat room set to: " + this.chatRoom);
    }
    
    public void connect(String username) {
        this.currentUsername = username;
        // chatRoom should be set before connecting via setChatRoom method
        
        Log.d(TAG, "Connecting to chat server for user: " + username + " in room: " + chatRoom);
        
        try {
            URI serverUri = new URI(WEBSOCKET_URL);
            
            webSocketClient = new WebSocketClient(serverUri) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    Log.d(TAG, "WebSocket connected successfully");
                    isConnected = true;
                    if (listener != null) {
                        listener.onConnectionStatusChanged(true);
                    }
                    
                    // Send join message silently (not displayed to user)
                    ChatMessageDTO joinMessage = new ChatMessageDTO(
                        username + " joined chat room: " + chatRoom, 
                        username, 
                        "JOIN"
                    );
                    joinMessage.setChatRoom(chatRoom);
                    try {
                        String jsonMessage = gson.toJson(joinMessage);
                        webSocketClient.send(jsonMessage);
                    } catch (Exception e) {
                        Log.e(TAG, "Error sending join message: " + e.getMessage());
                    }
                }

                @Override
                public void onMessage(String message) {
                    Log.d(TAG, "Message received: " + message);
                    try {
                        ChatMessageDTO chatMessage = gson.fromJson(message, ChatMessageDTO.class);
                        if (chatMessage != null && listener != null) {
                            // Don't show system messages like JOIN/LEAVE
                            if (!"JOIN".equals(chatMessage.getType()) && !"LEAVE".equals(chatMessage.getType())) {
                                listener.onMessageReceived(chatMessage);
                            }
                        }
                    } catch (JsonSyntaxException e) {
                        Log.e(TAG, "Error parsing message: " + e.getMessage());
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.d(TAG, "WebSocket closed: " + reason);
                    isConnected = false;
                    if (listener != null) {
                        listener.onConnectionStatusChanged(false);
                    }
                }

                @Override
                public void onError(Exception ex) {
                    Log.e(TAG, "WebSocket error: " + ex.getMessage());
                    isConnected = false;
                    if (listener != null) {
                        listener.onConnectionStatusChanged(false);
                        listener.onError("Connection error: " + ex.getMessage());
                    }
                }
            };
            
            webSocketClient.connect();
            
        } catch (Exception e) {
            Log.e(TAG, "Error connecting to WebSocket: " + e.getMessage());
            if (listener != null) {
                listener.onError("Connection error: " + e.getMessage());
            }
        }
    }
    
    
    public void sendMessage(ChatMessageDTO message) {
        if (!isConnected || webSocketClient == null) {
            Log.e(TAG, "Cannot send message: not connected");
            if (listener != null) {
                listener.onError("Cannot send message: not connected");
            }
            return;
        }
        
        Log.d(TAG, "Sending message: " + message.getContent());
        
        try {
            // Set chat room for the message
            message.setChatRoom(chatRoom);
            String jsonMessage = gson.toJson(message);
            webSocketClient.send(jsonMessage);
            Log.d(TAG, "Message sent to backend: " + jsonMessage);
        } catch (Exception e) {
            Log.e(TAG, "Error sending message: " + e.getMessage());
            if (listener != null) {
                listener.onError("Error sending message: " + e.getMessage());
            }
        }
    }
    
    
    public void disconnect() {
        Log.d(TAG, "Disconnecting from chat server");
        
        if (webSocketClient != null) {
            webSocketClient.close();
            webSocketClient = null;
        }
        
        if (scheduler != null) {
            scheduler.shutdown();
        }
        
        isConnected = false;
        if (listener != null) {
            listener.onConnectionStatusChanged(false);
        }
    }
    
    public boolean isConnected() {
        return isConnected;
    }
}