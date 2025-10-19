package com.example.eventplanner.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventplanner.R;
import com.example.eventplanner.adapters.ChatAdapter;
import com.example.eventplanner.dto.ChatMessageDTO;
import com.example.eventplanner.network.WebSocketManager;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends BaseActivity implements WebSocketManager.WebSocketListener {
    
    private static final String TAG = "ChatActivity";
    
    private RecyclerView recyclerViewMessages;
    private EditText editTextMessage;
    private Button buttonSend;
    private Button buttonTestConnection;
    private TextView textViewStatus;
    private ProgressBar progressBar;
    
    private ChatAdapter chatAdapter;
    private List<ChatMessageDTO> messages;
    private WebSocketManager webSocketManager;
    private String currentUsername;
    private String recipientName;
    private String contextType;
    private String contextId;
    private String contextName;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_chat, contentFrame, true);
        
        // Get data from intent
        recipientName = getIntent().getStringExtra("recipient_name");
        if (recipientName == null) {
            recipientName = "Unknown";
        }
        
        contextType = getIntent().getStringExtra("context_type");
        contextId = getIntent().getStringExtra("context_id");
        contextName = getIntent().getStringExtra("event_name");
        if (contextName == null) {
            contextName = getIntent().getStringExtra("product_name");
        }
        
        // Get current user info
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        Long userId = prefs.getLong("user_id", -1L);
        String userRole = prefs.getString("user_role", "");
        
        if (userId == -1L) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // For now, use user role and ID as username (simpler approach)
        currentUsername = userRole + "_" + userId;
        
        initViews();
        setupRecyclerView();
        setupClickListeners();
        
        // Set title with context
        String title = "Chat with " + recipientName;
        if (contextName != null && contextType != null) {
            if ("EVENT".equals(contextType)) {
                title += " - Event: " + contextName;
            } else if ("PRODUCT".equals(contextType)) {
                title += " - Product: " + contextName;
            }
        }
        setTitle(title);
        
        // Initialize WebSocket connection
        Log.d(TAG, "Initializing WebSocket connection with username: " + currentUsername);
        webSocketManager = new WebSocketManager(this);
        
        // Set chat room if we have context
        if (contextType != null && contextId != null) {
            webSocketManager.setChatRoom(contextType, contextId);
            Log.d(TAG, "Chat room set to: " + contextType.toLowerCase() + "_" + contextId);
        } else {
            webSocketManager.setChatRoom(null, null); // Will default to "global"
        }
        
        webSocketManager.connect(currentUsername);
    }
    
    private void initViews() {
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);
        buttonTestConnection = findViewById(R.id.buttonTestConnection);
        textViewStatus = findViewById(R.id.textViewStatus);
        progressBar = findViewById(R.id.progressBar);
        
        messages = new ArrayList<>();
    }
    
    private void setupRecyclerView() {
        chatAdapter = new ChatAdapter(messages, currentUsername);
        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMessages.setAdapter(chatAdapter);
    }
    
    private void setupClickListeners() {
        buttonSend.setOnClickListener(v -> sendMessage());
        
        editTextMessage.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
        
        buttonTestConnection.setOnClickListener(v -> {
            Log.d(TAG, "Testing WebSocket connection...");
            Toast.makeText(this, "Testing connection...", Toast.LENGTH_SHORT).show();
            if (webSocketManager != null) {
                webSocketManager.disconnect();
                webSocketManager.connect(currentUsername);
            }
        });
    }
    
    private void sendMessage() {
        String messageText = editTextMessage.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) {
            return;
        }
        
        if (webSocketManager != null && webSocketManager.isConnected()) {
            ChatMessageDTO chatMessage = new ChatMessageDTO(messageText, currentUsername, "CHAT");
            webSocketManager.sendMessage(chatMessage);
            editTextMessage.setText("");
        } else {
            Toast.makeText(this, "Not connected to chat server", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void onMessageReceived(ChatMessageDTO message) {
        runOnUiThread(() -> {
            if (message != null && !TextUtils.isEmpty(message.getContent())) {
                // Filter out system messages and generic messages
                String content = message.getContent().toLowerCase();
                if (!content.contains("connected") && 
                    !content.contains("joined") && 
                    !content.contains("left") &&
                    !content.contains("chat server") &&
                    !message.getSender().equals("System")) {
                    messages.add(message);
                    chatAdapter.notifyItemInserted(messages.size() - 1);
                    recyclerViewMessages.scrollToPosition(messages.size() - 1);
                }
            }
        });
    }
    
    @Override
    public void onConnectionStatusChanged(boolean connected) {
        runOnUiThread(() -> {
            if (connected) {
                textViewStatus.setText("Connected");
                textViewStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                progressBar.setVisibility(View.GONE);
                buttonSend.setEnabled(true);
                Log.d(TAG, "WebSocket connected successfully");
            } else {
                textViewStatus.setText("Disconnected");
                textViewStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                progressBar.setVisibility(View.VISIBLE);
                buttonSend.setEnabled(false);
                Log.d(TAG, "WebSocket disconnected or connecting...");
            }
        });
    }
    
    @Override
    public void onError(String error) {
        runOnUiThread(() -> {
            Log.e(TAG, "WebSocket error: " + error);
            Toast.makeText(this, "Chat error: " + error, Toast.LENGTH_SHORT).show();
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webSocketManager != null) {
            webSocketManager.disconnect();
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (webSocketManager != null) {
            webSocketManager.disconnect();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (webSocketManager != null && !webSocketManager.isConnected()) {
            webSocketManager.connect(currentUsername);
        }
    }
}
