package com.survice.electrofix;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> implements Filterable {

    private List<Service> serviceList;
    private List<Service> serviceListFull;
    private Context context;
    private OnServiceClickListener listener;
    private boolean onlyNameMode = false; // নতুন ফ্ল্যাগ

    // ক্লিক ইন্টারফেস
    public interface OnServiceClickListener {
        void onServiceClick(Service service);
    }

    // Default Constructor (ছবি ও দাম সহ)
    public ServiceAdapter(List<Service> serviceList, Context context, OnServiceClickListener listener) {
        this(serviceList, context, listener, false);
    }

    // নতুন Constructor (onlyNameMode সহ)
    public ServiceAdapter(List<Service> serviceList, Context context, OnServiceClickListener listener, boolean onlyNameMode) {
        this.serviceList = serviceList;
        this.context = context;
        this.listener = listener;
        this.onlyNameMode = onlyNameMode;
        this.serviceListFull = new ArrayList<>(serviceList); // filtering এর জন্য কপি
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.service_item, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        Service service = serviceList.get(position);

        holder.serviceNameTextView.setText(service.getServiceName());

        if (onlyNameMode) {
            holder.servicePriceTextView.setVisibility(View.GONE);
            holder.serviceIconImageView.setVisibility(View.GONE);
        } else {
            holder.servicePriceTextView.setText(service.getServicePrice());
            holder.serviceIconImageView.setImageResource(service.getServiceIcon());
            holder.servicePriceTextView.setVisibility(View.VISIBLE);
            holder.serviceIconImageView.setVisibility(View.VISIBLE);
        }

        // ক্লিক ইভেন্ট
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onServiceClick(service);
            }
        });
    }

    @Override
    public int getItemCount() {
        return serviceList.size();
    }

    @Override
    public Filter getFilter() {
        return serviceFilter;
    }

    private Filter serviceFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Service> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(serviceListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Service service : serviceListFull) {
                    if (service.getServiceName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(service);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            results.count = filteredList.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            serviceList.clear();
            if (results != null && results.count > 0) {
                serviceList.addAll((List<Service>) results.values);
            }
            notifyDataSetChanged();
        }
    };

    public static class ServiceViewHolder extends RecyclerView.ViewHolder {
        TextView serviceNameTextView, servicePriceTextView;
        ImageView serviceIconImageView;

        public ServiceViewHolder(View itemView) {
            super(itemView);
            serviceNameTextView = itemView.findViewById(R.id.tv_service_name);
            servicePriceTextView = itemView.findViewById(R.id.tv_service_price);
            serviceIconImageView = itemView.findViewById(R.id.img_service);
        }
    }
}
