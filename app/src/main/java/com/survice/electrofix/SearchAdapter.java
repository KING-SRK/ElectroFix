package com.survice.electrofix;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    private List<String> recommendations;
    private List<String> filteredList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // Constructor: Add categories
    public SearchAdapter(List<String> recommendations) {
        this.recommendations = new ArrayList<>();
        this.filteredList = new ArrayList<>();

        // Adding categories to recommendations list
        this.recommendations.add("AC Repair");
        this.recommendations.add("Computer Repair");
        this.recommendations.add("Washing Machine Repair");
        this.recommendations.add("Refrigerator Repair");
        this.recommendations.add("TV Repair");

        // Initially, filtered list contains all recommendations
        filteredList.addAll(this.recommendations);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_item_recommendetion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String item = filteredList.get(position);
        holder.tvRecommendation.setText(item);

        // Click listener for each item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void filter(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(recommendations);
        } else {
            for (String item : recommendations) {
                if (item.toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRecommendation;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRecommendation = itemView.findViewById(R.id.tv_recommendation);
        }
    }
}