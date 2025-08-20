package com.survice.electrofix;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class ForgotPasswordActivity extends Activity {

    private EditText otp1, otp2, otp3, otp4;
    private Button btnVerifyOtp;
    private TextView btnResendOtp, tvTimer;

    private CountDownTimer countDownTimer;
    private static final long OTP_TIMEOUT = 30000; // 30 seconds

    @SuppressLint({"WrongViewCast", "MissingInflatedId"})
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgetpassword); // match your XML

        otp1 = findViewById(R.id.etOtp1);
        otp2 = findViewById(R.id.etOtp2);
        otp3 = findViewById(R.id.etOtp3);
        otp4 = findViewById(R.id.etOtp4);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        btnResendOtp = findViewById(R.id.tvResendOtp);
        tvTimer = findViewById(R.id.tvTimer);

        setupOtpInputs();
        startOtpCountdown();
        startSmsListener();

        // ✅ Verify OTP
        btnVerifyOtp.setOnClickListener(v -> {
            String enteredOtp = otp1.getText().toString().trim() +
                    otp2.getText().toString().trim() +
                    otp3.getText().toString().trim() +
                    otp4.getText().toString().trim();

            String verificationId = getIntent().getStringExtra("verificationId");

            if (enteredOtp.length() == 4 && verificationId != null) {
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, enteredOtp);

                FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                setOtpBoxBackground(R.drawable.otp_correct);
                                Toast.makeText(this, "OTP Verified!", Toast.LENGTH_SHORT).show();

                                // Go to ResetPasswordActivity
                                Intent intent = new Intent(ForgotPasswordActivity.this, ChangePasswordActivity.class);
                                intent.putExtra("userId", getIntent().getStringExtra("userId"));
                                startActivity(intent);
                                finish();
                            } else {
                                setOtpBoxBackground(R.drawable.otp_wrong);
                                Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(this, "Enter full 4-digit OTP", Toast.LENGTH_SHORT).show();
            }
        });

        // ✅ Resend OTP
        btnResendOtp.setOnClickListener(v -> {
            Toast.makeText(this, "OTP resent!", Toast.LENGTH_SHORT).show();
            btnResendOtp.setEnabled(false); // disable again
            startOtpCountdown(); // restart timer
        });
    }

    // ---- Helper methods ----

    private void setOtpBoxBackground(int backgroundResId) {
        otp1.setBackgroundResource(backgroundResId);
        otp2.setBackgroundResource(backgroundResId);
        otp3.setBackgroundResource(backgroundResId);
        otp4.setBackgroundResource(backgroundResId);
    }

    private void resetOtpBackground() {
        setOtpBoxBackground(R.drawable.edittext_background); // your default
    }

    private void setupOtpInputs() {
        otp1.addTextChangedListener(new GenericTextWatcher(otp1, otp2));
        otp2.addTextChangedListener(new GenericTextWatcher(otp2, otp3));
        otp3.addTextChangedListener(new GenericTextWatcher(otp3, otp4));
        otp4.addTextChangedListener(new GenericTextWatcher(otp4, null));
    }

    private void startOtpCountdown() {
        btnResendOtp.setEnabled(false);

        countDownTimer = new CountDownTimer(OTP_TIMEOUT, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                String time = String.format("%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60);

                tvTimer.setText("Resend OTP in " + time);
            }

            @Override
            public void onFinish() {
                tvTimer.setText("You can resend OTP now");
                btnResendOtp.setEnabled(true);
            }
        };
        countDownTimer.start();
    }

    // ✅ Auto-fill OTP using SMS Retriever API
    private void startSmsListener() {
        SmsRetriever.getClient(this).startSmsRetriever()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(ForgotPasswordActivity.this, "Waiting for OTP...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(ForgotPasswordActivity.this, "SMS Retriever failed.", Toast.LENGTH_SHORT).show());
    }

    // ✅ Moves cursor automatically
    private class GenericTextWatcher implements TextWatcher {
        private EditText currentView, nextView;

        public GenericTextWatcher(EditText currentView, EditText nextView) {
            this.currentView = currentView;
            this.nextView = nextView;
        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (editable.length() == 1 && nextView != null) {
                nextView.requestFocus();
            }
        }

        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }
}
