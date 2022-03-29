package com.project.betterbaskets.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.project.betterbaskets.R;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.Stripe;
import com.stripe.android.model.PaymentIntent;


import java.math.BigDecimal;
import java.util.Objects;

public class DemoActivity extends BaseActivity {

    Stripe stripe;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        // Configure the SDK with your Stripe publishable key so it can make requests to Stripe
        stripe = new Stripe(
                getApplicationContext(),
                Objects.requireNonNull("pk_test_51IJzmwJRXiKp3vHjNXHPKAgRtdEp6fPhUOroZQQUyC87TvsfPrX5pA9AtIZQ6z4zjznqWWeRCGdach2SSV8Xk5Xw00A7CPkKrq")
        );

       // startCheckout();






    }

}