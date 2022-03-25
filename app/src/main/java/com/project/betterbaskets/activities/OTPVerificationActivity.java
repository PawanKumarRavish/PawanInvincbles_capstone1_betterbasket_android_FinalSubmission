package com.project.betterbaskets.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mukesh.OnOtpCompletionListener;
import com.project.betterbaskets.R;
import com.project.betterbaskets.databinding.ActivityOtpverificationBinding;
import com.project.betterbaskets.databinding.ActivityRegisterBinding;
import com.project.betterbaskets.interfaces.Constants;
import com.project.betterbaskets.models.Users;
import com.project.betterbaskets.utilities.SharedPreference;
import com.project.betterbaskets.utilities.Utils;

import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class OTPVerificationActivity extends BaseActivity implements OnOtpCompletionListener {

    ActivityOtpverificationBinding binding;
    private FirebaseAuth mAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    String verificationId;
    String name ,phone, profilePic,password,type,address;
    String OTP = "";
    DatabaseReference mDatabaseReference;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtpverificationBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        mDatabaseReference = Utils.initialiseFirebase();

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            name = bundle.getString(Constants.NAME);
            phone = bundle.getString(Constants.MOBILE);
            password = bundle.getString(Constants.PASSWORD);
            type = bundle.getString(Constants.TYPE);
            profilePic = bundle.getString(Constants.PROFILE_IMG);
            address = bundle.getString(Constants.ADDRESS);
        }

        startCounter();


        setToolbar();

        binding.otpView.setOtpCompletionListener(this);

        mAuth = FirebaseAuth.getInstance();
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                signInWithPhoneAuthCredential(phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(OTPVerificationActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Toast.makeText(OTPVerificationActivity.this, "Verification code sent to mobile number.", Toast.LENGTH_LONG).show();
                verificationId = s;

            }
        };

        sendVerificationCode(phone);


    }

    private void setToolbar() {
        Utils.setToolbar(OTPVerificationActivity.this, binding.toolbar, getString(R.string.otp_verification));
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential phoneAuthCredential) {
        mAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isComplete()) {
                    doSignup();
                } else {
                    hideProgressing();
                    if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(OTPVerificationActivity.this, "Verification Failed, Invalid OTP", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(OTPVerificationActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }


    private void doSignup() {
        showProgressing(OTPVerificationActivity.this);
        DatabaseReference usersRef = mDatabaseReference.child(Constants.REF_USERS);
        DatabaseReference storesRef = usersRef.child(Constants.REF_STORES);
        DatabaseReference customersRef = usersRef.child(Constants.REF_CUSTOMERS);
        DatabaseReference newUserRef = null;
        if(type.equalsIgnoreCase(Constants.TYPE_STORE)){
            newUserRef = storesRef.push();

        }else{
            newUserRef = customersRef.push();
        }

        String uid=newUserRef.getKey();
        newUserRef.setValue(new Users(uid,name, phone, password, type, "", address, SharedPreference.getLat(), SharedPreference.getLng()), new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if(error==null){
                    hideProgressing();
                    Toast.makeText(OTPVerificationActivity.this, "Data saved successfully", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(OTPVerificationActivity.this,LoginActivity.class));
                    finish();

                }else{
                    hideProgressing();
                    Toast.makeText(OTPVerificationActivity.this, "Error in saving user "+error.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        });


    }

    private void sendVerificationCode(String mobileNumber) {

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                Constants.PHONE_CODE + mobileNumber,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallbacks);
    }

    private void startCounter() {
        new CountDownTimer(90000, 1000) {  // 1.30 minute
            public void onTick(long millisUntilFinished) {
                // Used for formatting digit to be in 2 digits only
                NumberFormat f = new DecimalFormat("00");
                long hour = (millisUntilFinished / 3600000) % 24;
                long min = (millisUntilFinished / 60000) % 60;
                long sec = (millisUntilFinished / 1000) % 60;
                //mCounterTv.setText(f.format(hour) + ":" + f.format(min) + ":" + f.format(sec));
                binding.counterTv.setText("Resend in " + f.format(min) + ":" + f.format(sec));
            }

            // When the task is over it will print 00:00:00 there
            public void onFinish() {
                //mCounterTv.setText("00:00:00");
                //mCounterTv.setText("00:00");
                binding.mCounterLl.setVisibility(View.GONE);
                binding.mResendOTP.setVisibility(View.VISIBLE);
            }
        }.start();
    }

    private void verifyOTP(String verificationId) {

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, OTP);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    doSignup();
                } else {
                    hideProgressing();
                    if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(OTPVerificationActivity.this, "Verification Failed, Invalid OTP", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(OTPVerificationActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }


    @Override
    public void onOtpCompleted(String otp) {
        OTP = otp;
        Log.e("OTP", OTP);
        Utils.hideKeyboard(OTPVerificationActivity.this);
        verifyOTP(verificationId);

    }
}