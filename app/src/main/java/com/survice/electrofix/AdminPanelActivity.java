package com.survice.electrofix;

import android.os.Bundle;
import android.util.Log;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

public class AdminPanelActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private IssueAdapter issueAdapter;
    private List<IssueModel> issueList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        issueList = new ArrayList<>();
        issueAdapter = new IssueAdapter(issueList);
        recyclerView.setAdapter(issueAdapter);

        db = FirebaseFirestore.getInstance();
        fetchIssues();
    }

    private void fetchIssues() {
        db.collection("IssuesAndSuggestions").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Firestore Error", error.getMessage());
                    return;
                }

                for (DocumentChange dc : value.getDocumentChanges()) {
                    if (dc.getType() == DocumentChange.Type.ADDED) {
                        IssueModel issue = dc.getDocument().toObject(IssueModel.class);
                        issueList.add(issue);
                    }
                }
                issueAdapter.notifyDataSetChanged();
            }
        });
    }
}