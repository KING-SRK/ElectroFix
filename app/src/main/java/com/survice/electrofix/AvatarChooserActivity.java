package com.survice.electrofix;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class AvatarChooserActivity extends AppCompatActivity {

    private static final int REQUEST_GALLERY = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_choose_avatar); // <-- your xml file name

        // Avatar click listeners
        setupAvatarClick(R.id.avatar1, R.drawable.avatar1);
        setupAvatarClick(R.id.avatar2, R.drawable.avatar2);
        setupAvatarClick(R.id.avatar3, R.drawable.avatar3);
        setupAvatarClick(R.id.avatar4, R.drawable.avatar4);
        setupAvatarClick(R.id.avatar5, R.drawable.avatar5);
        setupAvatarClick(R.id.avatar6, R.drawable.avatar6);
        setupAvatarClick(R.id.avatar7, R.drawable.avatar7);
        setupAvatarClick(R.id.avatar8, R.drawable.avatar8);

        // Gallery option
        TextView tvGallery = findViewById(R.id.tvChooseFromGallery);
        tvGallery.setOnClickListener(v -> {
            Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickIntent, REQUEST_GALLERY);
        });
    }

    private void setupAvatarClick(int viewId, int drawableRes) {
        ImageView avatar = findViewById(viewId);
        avatar.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("selectedAvatar", drawableRes);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_GALLERY && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            Intent resultIntent = new Intent();
            resultIntent.putExtra("galleryImageUri", selectedImageUri.toString());
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        }
    }
}
