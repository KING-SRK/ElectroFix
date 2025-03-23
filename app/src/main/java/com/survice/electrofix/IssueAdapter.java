package com.survice.electrofix;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class IssueAdapter extends RecyclerView.Adapter<IssueAdapter.ViewHolder> {

    private List<IssueModel> issueList;

    public IssueAdapter(List<IssueModel> issueList) {
        this.issueList = issueList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_issue, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        IssueModel issue = issueList.get(position);
        holder.tvUserName.setText("Name: " + issue.getUserName());
        holder.tvUserEmail.setText("Email: " + issue.getUserEmail());
        holder.tvMessage.setText("Message: " + issue.getMessage());
        holder.tvType.setText("Type: " + issue.getType());

        if (!issue.getScreenshotUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext()).load(issue.getScreenshotUrl()).into(holder.ivScreenshot);
        } else {
            holder.ivScreenshot.setVisibility(View.GONE);
        }

        // Mark as Resolved Button Click
        holder.btnMarkResolved.setOnClickListener(v -> {
            FirebaseFirestore.getInstance().collection("IssuesAndSuggestions")
                    .document(issue.getId())
                    .update("status", "Resolved")
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(holder.itemView.getContext(), "Marked as Resolved", Toast.LENGTH_SHORT).show();
                        issueList.remove(position);
                        notifyItemRemoved(position);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(holder.itemView.getContext(), "Failed to update", Toast.LENGTH_SHORT).show());
        });

        // Delete Button Click
        holder.btnDelete.setOnClickListener(v -> {
            FirebaseFirestore.getInstance().collection("IssuesAndSuggestions")
                    .document(issue.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(holder.itemView.getContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
                        issueList.remove(position);
                        notifyItemRemoved(position);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(holder.itemView.getContext(), "Failed to delete", Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    public int getItemCount() {
        return issueList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvUserEmail, tvMessage, tvType;
        ImageView ivScreenshot;
        Button btnMarkResolved, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvType = itemView.findViewById(R.id.tvType);
            ivScreenshot = itemView.findViewById(R.id.ivScreenshot);
            btnMarkResolved = itemView.findViewById(R.id.btnMarkResolved);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}