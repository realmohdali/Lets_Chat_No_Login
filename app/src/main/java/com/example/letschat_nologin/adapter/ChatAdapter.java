package com.example.letschat_nologin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letschat_nologin.R;
import com.example.letschat_nologin.data.MessageData;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    ArrayList<MessageData> messageData;

    public ChatAdapter(ArrayList<MessageData> messageData) {
        this.messageData = messageData;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View messageView = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_view, parent, false);
        return new ViewHolder(messageView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MessageData newMessageData = messageData.get(position);
        holder.sentImage.setVisibility(View.GONE);
        holder.sentText.setVisibility(View.GONE);
        holder.receivedImage.setVisibility(View.GONE);
        holder.receivedText.setVisibility(View.GONE);

        String type = newMessageData.getType();
        String sender = newMessageData.getSender();
        if(type.equals("1")) {
            if(sender.equalsIgnoreCase("other")) {
                holder.receivedText.setVisibility(View.VISIBLE);
                holder.receivedTextView.setText(newMessageData.getMessage());
            } else {
                holder.sentText.setVisibility(View.VISIBLE);
                holder.sentTextView.setText(newMessageData.getMessage());
            }
        } else {
            if(sender.equalsIgnoreCase("other")) {
                holder.receivedText.setVisibility(View.VISIBLE);
                holder.receivedTextView.setText("New Image Received");
            } else {
                holder.sentTextView.setVisibility(View.VISIBLE);
                holder.sentTextView.setText("New Image Sent");
            }
        }
    }

    @Override
    public int getItemCount() {
        return messageData.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView sentImage, sentText, receivedImage, receivedText;
        TextView sentTextView, receivedTextView;
        ImageView sentImageView, receivedImageView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            sentImage = itemView.findViewById(R.id.sent_image);
            sentText = itemView.findViewById(R.id.sent_message);
            receivedImage = itemView.findViewById(R.id.received_image);
            receivedText = itemView.findViewById(R.id.received_message);

            sentTextView = itemView.findViewById(R.id.sent_message_view);
            receivedTextView = itemView.findViewById(R.id.received_message_view);

            sentImageView = itemView.findViewById(R.id.sent_image_view);
            receivedImageView = itemView.findViewById(R.id.received_image_view);
        }
    }
}
