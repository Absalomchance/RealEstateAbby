package com.example.realestate.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.util.Log;

import com.example.realestate.MyUtils;
import com.example.realestate.R;
import com.example.realestate.databinding.ActivityLoginPhoneBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class LoginPhoneActivity extends AppCompatActivity {

    private ActivityLoginPhoneBinding binding;
    private static final String TAG = "LOGIN_PHONE_TAG";
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private PhoneAuthProvider.ForceResendingToken forceResendingToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBacks;
    private String mVerificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginPhoneBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.phoneInputRl.setVisibility(View.VISIBLE);
        binding.otpInputRl.setVisibility(View.GONE);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();

        phoneLoginCallbacks();  // Initialize phone login callbacks

        // Back button click listener
        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();  // Go back to the previous screen
            }
        });

        // Send OTP button click listener
        binding.sendOtpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();  // Validate and start the phone number verification process
            }
        });

        // Resend OTP button click listener
        binding.resendOtpTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resendVerificationCode(forceResendingToken);  // Resend the OTP code
            }
        });

        // Verify OTP button click listener
        binding.verifyOtpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String otp = binding.otpEt.getText().toString().trim();

                if (otp.isEmpty()){
                    binding.otpEt.setError("Enter OTP");
                    binding.otpEt.requestFocus();
                } else if(otp.length() < 6){
                    binding.otpEt.setError("OTP must be 6 characters");
                    binding.otpEt.requestFocus();
                } else {
                    verifyPhoneNumberWithCode(otp);  // Verify the OTP
                }
            }
        });
    }

    private String phoneCode = "", phoneNumber = "", phoneNumberWithCode = "";

    // Method to validate phone number input and start verification
    private void validateData(){
        phoneCode = binding.phoneCodeTil.getSelectedCountryCodeWithPlus();
        phoneNumber = binding.phoneNumberEt.getText().toString().trim();
        phoneNumberWithCode = phoneCode + phoneNumber;

        Log.d(TAG, "validateData: Phone Code: "+phoneCode);
        Log.d(TAG, "validateData: Phone Number: "+phoneNumber);
        Log.d(TAG, "validateData: Phone Code With Number: "+phoneNumberWithCode);

        if (phoneNumber.isEmpty()){
            binding.phoneNumberEt.setError("Enter Phone Number");
            binding.phoneNumberEt.requestFocus();
        } else {
            startPhoneNumberVerification();  // Start verification process if phone number is valid
        }
    }

    // Method to resend verification code
    private void resendVerificationCode(PhoneAuthProvider.ForceResendingToken token){
        progressDialog.setMessage("Resending OTP to " +phoneNumberWithCode);
        progressDialog.show();

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phoneNumberWithCode)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(mCallBacks)
                .setForceResendingToken(token)
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    // Method to verify phone number with OTP code
    private void verifyPhoneNumberWithCode(String otp){
        Log.d(TAG, "verifyPhoneNumberWithCode: OTP "+otp);

        progressDialog.setMessage("Verifying OTP...");
        progressDialog.show();

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, otp);
        signInWithPhoneAuthCredential(credential);
    }

    // Method to start phone number verification
    private void startPhoneNumberVerification(){
        progressDialog.setMessage("Sending OTP to "+phoneNumberWithCode);
        progressDialog.show();

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phoneNumberWithCode)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(mCallBacks)
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    // Method to handle phone login callbacks
    private void phoneLoginCallbacks() {
        Log.d(TAG, "phoneLoginCallbacks: ");

        mCallBacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onCodeSent(@NonNull String verificationID, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                super.onCodeSent(verificationID, token);
                Log.d(TAG, "onCodeSent: ");

                mVerificationId = verificationID;
                forceResendingToken = token;

                progressDialog.dismiss();

                binding.phoneInputRl.setVisibility(View.GONE);
                binding.otpInputRl.setVisibility(View.VISIBLE);

                MyUtils.toast(LoginPhoneActivity.this,"OTP sent to "+phoneNumberWithCode);

                binding.loginPhoneLabel.setText("Please type the verification code sent to "+phoneNumberWithCode);
            }

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                Log.d(TAG, "onVerificationCompleted: ");

                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Log.e(TAG, "onVerificationFailed: ", e);

                progressDialog.dismiss();
                MyUtils.toast(LoginPhoneActivity.this, "Failed to verify due to "+e.getMessage());
            }
        };
    }

    // Method to sign in using the phone auth credential
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential){
        Log.d(TAG, "signInWithPhoneAuthCredential: ");

        progressDialog.setMessage("Logging In...");
        progressDialog.show();

        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.d(TAG, "onSuccess: ");

                        if (authResult.getAdditionalUserInfo().isNewUser()){
                            updateUserInfo();  // Save user info if it's a new user
                        } else {
                            Log.d(TAG, "onSuccess: Existing User, Logged In...");
                            startActivity(new Intent(LoginPhoneActivity.this, MainActivity.class));
                            finishAffinity();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ",e );
                        progressDialog.dismiss();
                        MyUtils.toast(LoginPhoneActivity.this, "Login Failed due to "+ e.getMessage());
                    }
                });
    }

    // Method to save user information to Firebase
    private void updateUserInfo(){
        Log.d(TAG, "updateUserInfo: ");

        progressDialog.setMessage("Saving User Info...");
        progressDialog.show();

        long timestamp = MyUtils.timestamp();
        String registeredUserUid = firebaseAuth.getUid();

        HashMap<String, Object>  hashMap= new HashMap<>();
        hashMap.put("uid", registeredUserUid);
        hashMap.put("email", "");
        hashMap.put("name", "");
        hashMap.put("timestamp", timestamp);
        hashMap.put("phoneCode", phoneCode);
        hashMap.put("phoneNumber", phoneNumber);
        hashMap.put("profileImageUrl", "");
        hashMap.put("dob", "");
        hashMap.put("userType", "user");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(registeredUserUid)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: ");

                        progressDialog.dismiss();
                        MyUtils.toast(LoginPhoneActivity.this,"Account Created...");
                        startActivity(new Intent(LoginPhoneActivity.this, MainActivity.class));
                        finishAffinity();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ",e );
                        progressDialog.dismiss();
                        MyUtils.toast(LoginPhoneActivity.this, "Failed saving user info due to "+e.getMessage());
                    }
                });
    }
}
