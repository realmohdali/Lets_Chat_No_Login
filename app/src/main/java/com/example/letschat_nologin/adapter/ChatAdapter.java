package com.example.letschat_nologin.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.letschat_nologin.R;
import com.example.letschat_nologin.ViewImage;
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
        switch (type) {
            case "1":
                if (sender.equalsIgnoreCase("other")) {
                    holder.receivedText.setVisibility(View.VISIBLE);
                    holder.receivedTextView.setText(newMessageData.getMessage());
                } else {
                    holder.sentText.setVisibility(View.VISIBLE);
                    holder.sentTextView.setText(newMessageData.getMessage());
                }
                break;
            case "2":
                if (sender.equalsIgnoreCase("other")) {
                    String url = "https://chat.curioustechguru.com/" + newMessageData.getMessage();
                    holder.receivedImage.setVisibility(View.VISIBLE);
                    Glide.with(holder.receivedTextView.getContext()).load(url).into(holder.receivedImageView);
                    holder.receivedImageProgress.setVisibility(View.GONE);
                    holder.receivedImageView.setOnClickListener(v -> {
                        Intent intent = new Intent(v.getContext(), ViewImage.class);
                        intent.putExtra("sender", "other");
                        intent.putExtra("url", url);
                        v.getContext().startActivity(intent);
                    });
                } else {
                    holder.sentImage.setVisibility(View.VISIBLE);
                    holder.sentImageView.setImageBitmap(newMessageData.getBitmap());
                    holder.sentImageProgress.setVisibility(View.VISIBLE);
                }
                break;
            case "3":
                String url = "https://chat.curioustechguru.com/" + newMessageData.getMessage();
                holder.sentImage.setVisibility(View.VISIBLE);
                Glide.with(holder.sentImageView.getContext()).load(url).into(holder.sentImageView);
                holder.sentImageProgress.setVisibility(View.GONE);

                holder.sentImageView.setOnClickListener(v -> {
                    Intent intent = new Intent(v.getContext(), ViewImage.class);
                    intent.putExtra("sender", "other");
                    intent.putExtra("url", url);
                    v.getContext().startActivity(intent);
                });
                break;
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
        ProgressBar sentImageProgress, receivedImageProgress;

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

            sentImageProgress = itemView.findViewById(R.id.sent_image_progress);
            receivedImageProgress = itemView.findViewById(R.id.received_image_progress);
        }
    }
}
