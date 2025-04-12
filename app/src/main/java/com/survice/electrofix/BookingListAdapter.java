package com.survice.electrofix;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class BookingListAdapter extends RecyclerView.Adapter<BookingListAdapter.BookingViewHolder> {

    private Context context;
    private List<BookingModel> bookingList;
    private String repairerId;

    public BookingListAdapter(Context context, List<BookingModel> bookingList, String repairerId) {
        this.context = context;
        this.bookingList = bookingList;
        this.repairerId = repairerId;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        BookingModel booking = bookingList.get(position);

        holder.tvName.setText(Html.fromHtml("<b>Customer:</b> " + booking.getName()));
        holder.tvPhone.setText(Html.fromHtml("<b>Phone:</b> " + booking.getPhoneNumber()));
        holder.tvAddress.setText(Html.fromHtml("<b>Address:</b> " + booking.getAddress()));
        holder.tvPreferredTime.setText(Html.fromHtml("<b>Preferred Time:</b> " + booking.getPreferredTime()));
        holder.tvServiceName.setText(Html.fromHtml("<b>Service:</b> " + booking.getServiceName()));
        holder.tvServicePrice.setText(Html.fromHtml("<b>Price:</b> ₹" + booking.getServicePrice()));

        String status = booking.getStatus() != null ? booking.getStatus() : "";
        String acceptedBy = booking.getAcceptedBy() != null ? booking.getAcceptedBy() : "";

        // Default all buttons gone
        holder.btnAccept.setVisibility(View.GONE);
        holder.btnReject.setVisibility(View.GONE);
        holder.btnTrack.setVisibility(View.GONE);

        // Logic to manage visibility and enabling
        if ("pending".equalsIgnoreCase(status)) {
            holder.btnAccept.setVisibility(View.VISIBLE);
            holder.btnReject.setVisibility(View.VISIBLE);
            holder.btnTrack.setVisibility(View.GONE);

            holder.btnAccept.setEnabled(true);
            holder.btnReject.setEnabled(false); // Reject disabled until accept
        } else if ("accepted".equalsIgnoreCase(status)) {
            if (repairerId.equals(acceptedBy)) {
                holder.btnAccept.setVisibility(View.VISIBLE);
                holder.btnReject.setVisibility(View.VISIBLE);
                holder.btnTrack.setVisibility(View.VISIBLE);

                holder.btnAccept.setEnabled(false); // Already accepted
                holder.btnReject.setEnabled(true);  // Now reject possible
            }
        }

        holder.btnAccept.setOnClickListener(v -> acceptBooking(booking));
        holder.btnReject.setOnClickListener(v -> rejectBooking(booking));
        holder.btnTrack.setOnClickListener(v -> trackCustomerLocation(booking));
    }

    private void acceptBooking(BookingModel booking) {
        String bookingId = booking.getBookingId();

        FirebaseFirestore.getInstance().collection("Bookings").document(bookingId)
                .update("status", "accepted", "acceptedBy", repairerId)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Booking Accepted!", Toast.LENGTH_SHORT).show();
                    booking.setStatus("accepted");
                    booking.setAcceptedBy(repairerId);
                    notifyItemChanged(bookingList.indexOf(booking));
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to accept booking: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void rejectBooking(BookingModel booking) {
        String bookingId = booking.getBookingId();

        if (repairerId.equals(booking.getAcceptedBy())) {
            FirebaseFirestore.getInstance().collection("Bookings").document(bookingId)
                    .update("status", "pending", "acceptedBy", "")
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Booking Rejected!", Toast.LENGTH_SHORT).show();
                        booking.setStatus("pending");
                        booking.setAcceptedBy("");
                        notifyItemChanged(bookingList.indexOf(booking));
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Failed to reject booking: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(context, "You cannot reject this booking!", Toast.LENGTH_SHORT).show();
        }
    }

    private void trackCustomerLocation(BookingModel booking) {
        double latitude = booking.getCustomerLatitude();
        double longitude = booking.getCustomerLongitude();

        Intent intent = new Intent(context, TrackCustomerActivity.class);
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    public static class BookingViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvPhone, tvAddress, tvPreferredTime, tvServiceName, tvServicePrice;
        Button btnAccept, btnReject, btnTrack;

        public BookingViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvPhone = itemView.findViewById(R.id.tv_phone_number);
            tvAddress = itemView.findViewById(R.id.tv_address);
            tvPreferredTime = itemView.findViewById(R.id.tv_preferred_time);
            tvServiceName = itemView.findViewById(R.id.tv_service_name);
            tvServicePrice = itemView.findViewById(R.id.tv_service_price);
            btnAccept = itemView.findViewById(R.id.btn_accept);
            btnReject = itemView.findViewById(R.id.btn_reject);
            btnTrack = itemView.findViewById(R.id.btn_track);
        }
    }
}