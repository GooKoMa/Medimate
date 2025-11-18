package com.example.medimate.OCR;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medimate.R;

import java.util.List;

public class DetailAdapter extends RecyclerView.Adapter<DetailAdapter.ViewHolder> {

    public interface OnSpeakClick {
        void onSpeak(String text);
    }

    private List<DetailItem> items;
    private OnSpeakClick listener;

    public DetailAdapter(List<DetailItem> items, OnSpeakClick listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_detail_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DetailItem item = items.get(position);

        holder.titleButton.setText(item.getTitle());

        holder.titleButton.setOnClickListener(v -> listener.onSpeak(item.getContent()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        Button titleButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleButton = itemView.findViewById(R.id.tvTitle);  // XML에서 Button으로 변경됨
        }
    }
}
