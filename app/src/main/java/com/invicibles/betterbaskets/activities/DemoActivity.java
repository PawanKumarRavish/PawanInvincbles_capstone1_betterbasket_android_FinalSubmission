package com.invicibles.betterbaskets.activities;

import android.os.Bundle;

import com.invicibles.betterbaskets.R;
import com.stripe.android.Stripe;


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