package com.survice.electrofix;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {

    private List<Service> serviceList;

    public ServiceAdapter(List<Service> serviceList) {
        this.serviceList = serviceList;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.service_item, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        Service service = serviceList.get(position);
        holder.tvServiceName.setText(service.getName());
        holder.tvServicePrice.setText(service.getPrice());
        holder.imgService.setImageResource(service.getImageResId());

        // সার্ভিস ক্লিক করলে বিস্তারিত পেজ ওপেন হবে
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ServiceDetailsActivity.class);
            intent.putExtra("service_name", service.getName());
            intent.putExtra("service_price", service.getPrice());
            intent.putExtra("service_image", service.getImageResId());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return serviceList.size();
    }

    public static class ServiceViewHolder extends RecyclerView.ViewHolder {
        TextView tvServiceName, tvServicePrice;
        ImageView imgService;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvServiceName = itemView.findViewById(R.id.tv_service_name);
            tvServicePrice = itemView.findViewById(R.id.tv_service_price);
            imgService = itemView.findViewById(R.id.img_service);
        }
    }
}