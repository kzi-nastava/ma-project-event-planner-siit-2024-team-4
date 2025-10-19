package com.example.eventplanner.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventplanner.R;
import com.example.eventplanner.dto.ChatMessageDTO;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {
    
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;
    
    private List<ChatMessageDTO> messages;
    private String currentUsername;
    
    public ChatAdapter(List<ChatMessageDTO> messages, String currentUsername) {
        this.messages = messages;
        this.currentUsername = currentUsername;
    }
    
    @Override
    public int getItemViewType(int position) {
        ChatMessageDTO message = messages.get(position);
        if (message.getSender() != null && message.getSender().equals(currentUsername)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }
    
    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
        }
        return new MessageViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessageDTO message = messages.get(position);
        holder.bind(message);
    }
    
    @Override
    public int getItemCount() {
        return messages.size();
    }
    
    static class MessageViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewMessage;
        private TextView textViewSender;
        private TextView textViewTime;
        
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.textViewMessage);
            textViewSender = itemView.findViewById(R.id.textViewSender);
            textViewTime = itemView.findViewById(R.id.textViewTime);
        }
        
        public void bind(ChatMessageDTO message) {
            if (textViewMessage != null) {
                if (message.getContent() != null && !message.getContent().trim().isEmpty()) {
                    textViewMessage.setText(message.getContent());
                    textViewMessage.setVisibility(View.VISIBLE);
                } else {
                    textViewMessage.setVisibility(View.GONE);
                }
            }
            
            if (textViewSender != null) {
                if (message.getSender() != null && !message.getSender().trim().isEmpty()) {
                    textViewSender.setText(message.getSender());
                    textViewSender.setVisibility(View.VISIBLE);
                } else {
                    textViewSender.setVisibility(View.GONE);
                }
            }
            
            if (textViewTime != null) {
                // Set current time for now (in real implementation, you'd get timestamp from server)
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                textViewTime.setText(sdf.format(new Date()));
            }
        }
    }
}
