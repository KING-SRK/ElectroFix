package com.survice.electrofix;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

public class UploadIssueActivity extends BaseActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText edtIssueDescription, edtSuggestion;
    private Button btnSubmit, btnUploadScreenshot;
    private Uri screenshotUri = null;  // To hold the screenshot URI

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_issue);

        edtIssueDescription = findViewById(R.id.edtIssueDescription);
        edtSuggestion = findViewById(R.id.edtSuggestion);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnUploadScreenshot = findViewById(R.id.btnUploadScreenshot);

        // Back Button
        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());

        // Upload Screenshot Button Action
        btnUploadScreenshot.setOnClickListener(v -> openFilePicker());

        // Submit Button Action
        btnSubmit.setOnClickListener(v -> submitIssue());
    }

    // Open File Picker to select screenshot
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // Handle the selected screenshot
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            screenshotUri = data.getData();
        }
    }

    // Submit Issue along with optional screenshot and suggestion
    private void submitIssue() {
        String issueDescription = edtIssueDescription.getText().toString();
        String suggestion = edtSuggestion.getText().toString();

        if (issueDescription.isEmpty()) {
            Toast.makeText(this, "Issue description is required!", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Create a report data object
        Report report = new Report(issueDescription, suggestion);

        if (screenshotUri != null) {
            uploadScreenshot(report);
        } else {
            saveReportToFirestore(report);
        }
    }

    // Upload Screenshot to Firebase Storage
    private void uploadScreenshot(Report report) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("screenshots/" + System.currentTimeMillis());
        storageReference.putFile(screenshotUri)
                .addOnSuccessListener(taskSnapshot -> {
                    storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        report.setScreenshotUrl(uri.toString());
                        saveReportToFirestore(report);
                    });
                })
                .addOnFailureListener(e -> Toast.makeText(UploadIssueActivity.this, "Screenshot upload failed!", Toast.LENGTH_SHORT).show());
    }

    // Save report data to Firestore
    private void saveReportToFirestore(Report report) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("reports")
                .add(report)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(UploadIssueActivity.this, "Issue reported successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Close the activity
                })
                .addOnFailureListener(e -> Toast.makeText(UploadIssueActivity.this, "Failed to report issue!", Toast.LENGTH_SHORT).show());
    }
}