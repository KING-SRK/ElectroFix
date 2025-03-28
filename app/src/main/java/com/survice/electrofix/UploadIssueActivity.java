package com.survice.electrofix;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class UploadIssueActivity extends BaseActivity {

    private ChipGroup chipGroup;
    private TextInputEditText etUserName, etUserEmail, etMessage;
    private Button btnUploadScreenshot, btnSubmit;
    private ImageView btnBack;  // Back Button
    private String selectedCategory = "Issue";  // Default category
    private Uri imageUri;
    private ProgressDialog progressDialog;

    private FirebaseFirestore db;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_issue);

        // Initialize UI Components
        chipGroup = findViewById(R.id.chipGroup);
        etUserName = findViewById(R.id.etUserName);
        etUserEmail = findViewById(R.id.etUserEmail);
        etMessage = findViewById(R.id.etMessage);
        btnUploadScreenshot = findViewById(R.id.btnUploadScreenshot);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnBack = findViewById(R.id.btnBack); // Back Button Initialization

        // Firebase Initialization
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("issue_screenshots");

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Submitting...");

        // Chip Selection Listener
        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipSuggestion) {
                selectedCategory = "Suggestion";
            } else {
                selectedCategory = "Issue";
            }
        });

        // Upload Screenshot Button Click
        btnUploadScreenshot.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, 100);
        });

        // Submit Button Click
        btnSubmit.setOnClickListener(v -> uploadData());

        // Back Button Click
        btnBack.setOnClickListener(v -> onBackPressed());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            Toast.makeText(this, "Screenshot Selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadData() {
        String name = etUserName.getText().toString().trim();
        String email = etUserEmail.getText().toString().trim();
        String message = etMessage.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || message.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();

        if (imageUri != null) {
            // Upload Screenshot
            StorageReference fileRef = storageRef.child(System.currentTimeMillis() + ".jpg");
            fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                            fileRef.getDownloadUrl().addOnSuccessListener(uri -> saveToFirestore(name, email, message, uri.toString())))
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Screenshot Upload Failed", Toast.LENGTH_SHORT).show();
                    });
        } else {
            saveToFirestore(name, email, message, "");
        }
    }

    private void saveToFirestore(String name, String email, String message, String imageUrl) {
        Map<String, Object> issueData = new HashMap<>();
        issueData.put("userName", name);
        issueData.put("userEmail", email);
        issueData.put("message", message);
        issueData.put("type", selectedCategory);
        issueData.put("screenshotUrl", imageUrl);
        issueData.put("timestamp", System.currentTimeMillis());

        db.collection("IssuesAndSuggestions").add(issueData)
                .addOnSuccessListener(documentReference -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Submitted Successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> progressDialog.dismiss());
    }
}