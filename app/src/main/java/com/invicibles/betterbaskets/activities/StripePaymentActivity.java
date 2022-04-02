package com.invicibles.betterbaskets.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.invicibles.betterbaskets.R;
import com.invicibles.betterbaskets.interfaces.Constants;
import com.stripe.android.ApiResultCallback;
import com.stripe.android.PaymentIntentResult;
import com.stripe.android.Stripe;
import com.stripe.android.model.ConfirmPaymentIntentParams;
import com.stripe.android.model.PaymentIntent;
import com.stripe.android.model.PaymentMethodCreateParams;
import com.stripe.android.view.CardInputWidget;

import java.lang.ref.WeakReference;
import java.util.Objects;

public class StripePaymentActivity extends BaseActivity {
    String secret;
    CardInputWidget mCardInputWidget;
    Button mPay;
    private Stripe stripe;
    String amount;
    TextView mAmt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stripe_payment);

        mCardInputWidget=findViewById(R.id.cardInputWidget);
        mAmt=findViewById(R.id.mAmt);
        mPay=findViewById(R.id.mPayBtn);

        stripe = new Stripe(
                getApplicationContext(),
                Objects.requireNonNull("pk_test_51IJzmwJRXiKp3vHjNXHPKAgRtdEp6fPhUOroZQQUyC87TvsfPrX5pA9AtIZQ6z4zjznqWWeRCGdach2SSV8Xk5Xw00A7CPkKrq")
        );

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            secret = extras.getString("Secret");
            amount = extras.getString("Amount");
        }

        mAmt.setText("Total Amount To Pay: "+ Constants.RUPEE_SYMBOL+amount);

        mPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PaymentMethodCreateParams params = mCardInputWidget.getPaymentMethodCreateParams();
                if (params != null) {
                    ConfirmPaymentIntentParams confirmParams = ConfirmPaymentIntentParams
                            .createWithPaymentMethodCreateParams(params, secret);
                    stripe.confirmPayment(StripePaymentActivity.this, confirmParams);
                }
            }
        });


    }


    private void displayAlert(@NonNull String title, @Nullable String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message);
        builder.setPositiveButton("Ok", null);
        builder.create().show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Handle the result of stripe.confirmPayment
        stripe.onPaymentResult(requestCode, data, new PaymentResultCallback(StripePaymentActivity.this));
    }


    private  final class PaymentResultCallback implements ApiResultCallback<PaymentIntentResult> {
        @NonNull private final WeakReference<StripePaymentActivity> activityRef;
        PaymentResultCallback(@NonNull StripePaymentActivity activity) {
            activityRef = new WeakReference<>(activity);
        }
        @Override
        public void onSuccess(@NonNull PaymentIntentResult result) {
            final StripePaymentActivity activity = activityRef.get();
            if (activity == null) {
                return;
            }
            PaymentIntent paymentIntent = result.getIntent();
            PaymentIntent.Status status = paymentIntent.getStatus();
            if (status == PaymentIntent.Status.Succeeded) {
                Intent intent = new Intent();
                intent.putExtra("id", paymentIntent.getId());
                setResult(RESULT_OK, intent);
                finish();
                /*// Payment completed successfully
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                activity.displayAlert("Payment completed", gson.toJson(paymentIntent)
                );*/
            } else if (status == PaymentIntent.Status.RequiresPaymentMethod) {
                // Payment failed – allow retrying using a different payment method
                activity.displayAlert(
                        "Payment failed",
                        Objects.requireNonNull(paymentIntent.getLastPaymentError()).getMessage()
                );
            }
        }
        @Override
        public void onError(@NonNull Exception e) {
            final StripePaymentActivity activity = activityRef.get();
            if (activity == null) {
                return;
            }
            // Payment request failed – allow retrying using the same payment method
            activity.displayAlert("Error", e.toString());
        }
    }
}