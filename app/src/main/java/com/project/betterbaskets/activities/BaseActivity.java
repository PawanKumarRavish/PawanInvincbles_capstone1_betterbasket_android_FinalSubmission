package com.project.betterbaskets.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import com.project.betterbaskets.R;
import com.project.betterbaskets.utilities.SharedPreference;

public class BaseActivity extends AppCompatActivity {

    SharedPreference sharedPreference;
    public ProgressDialog progress;
    public void showProgressing(Activity context) {
        if (this.progress != null && this.progress.isShowing()) {
            this.progress.dismiss();
            this.progress = null;
        }
        this.progress = new ProgressDialog(context, R.style.MyGravity);
        this.progress.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        this.progress.setContentView(R.layout.progressdialog);
        this.progress.setCanceledOnTouchOutside(false);
        this.progress.setCancelable(false);
        this.progress.show();
    }

    public void hideProgressing() {
        if (this.progress != null && this.progress.isShowing()) {
            this.progress.dismiss();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreference=new SharedPreference(BaseActivity.this);
    }
}