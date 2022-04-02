package com.invicibles.betterbaskets;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.drawable.ColorDrawable;

import androidx.fragment.app.Fragment;

public class BaseFrg extends Fragment {

    public Activity activity;
    ProgressDialog progress;
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


}