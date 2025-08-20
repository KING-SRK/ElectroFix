package com.survice.electrofix; // Make sure this matches your package name

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
// You might need this import if you want to show a Toast directly in the adapter,
// but for cleaner architecture, it's better to pass data to the activity.
// import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {

    private final List<Integer> imageList;
    private final OnItemClickListener onItemClickListener;

    // --- MODIFIED INTERFACE ---
    // The interface now defines only ONE onItemClick method
    public interface OnItemClickListener {
        void onItemClick(int position, int imageResId);
    }
    // --- END MODIFIED INTERFACE ---

    public BannerAdapter(List<Integer> imageList, OnItemClickListener listener) {
        this.imageList = imageList;
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banner, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        int imageResId = imageList.get(position);
        holder.bannerImage.setImageResource(imageResId);

        // Handle banner click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    // Now call the single onItemClick method, passing both position and imageResId
                    onItemClickListener.onItemClick(holder.getAdapterPosition(), imageResId);

                    // IMPORTANT: If you see a Toast message, it's NOT coming from this code.
                    // It must be in an older version of this file, or in MainActivity,
                    // or in another part of your code.
                    // DO NOT add Toast.makeText here if you want MainActivity to handle clicks.
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    static class BannerViewHolder extends RecyclerView.ViewHolder {
        final ImageView bannerImage;

        BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            bannerImage = itemView.findViewById(R.id.banner_image);
        }
    }
}