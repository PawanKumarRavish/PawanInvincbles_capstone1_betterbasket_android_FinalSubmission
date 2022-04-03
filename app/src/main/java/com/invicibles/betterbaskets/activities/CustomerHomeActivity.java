package com.invicibles.betterbaskets.activities;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.navigation.NavigationView;
import com.invicibles.betterbaskets.R;
import com.invicibles.betterbaskets.databinding.ActivityCustomerHomeBinding;
import com.invicibles.betterbaskets.interfaces.Constants;
import com.invicibles.betterbaskets.userfragments.NearbyStoresFrg;
import com.invicibles.betterbaskets.userfragments.UderOrderHistory;
import com.invicibles.betterbaskets.userfragments.PaymentHistory;
import com.invicibles.betterbaskets.userfragments.UserHomeFrg;
import com.invicibles.betterbaskets.utilities.SharedPreference;
import com.invicibles.betterbaskets.utilities.Utils;

public class CustomerHomeActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    ActivityCustomerHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomerHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.navView.setNavigationItemSelectedListener(this);

        setUpHomeFrg();

        binding.mMenuImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (binding.drawerLayout.isDrawerOpen(Gravity.LEFT)) {
                    binding.drawerLayout.closeDrawer(Gravity.LEFT);
                } else {
                    binding.drawerLayout.openDrawer(Gravity.LEFT);
                }

            }
        });
    }

    private void setUpHomeFrg() {
        Utils.doFragmentTransition(R.id.mFrameLl,new UserHomeFrg(),getSupportFragmentManager(),true);
    }

    @Override
    public void onBackPressed() {

        int stackcount = getSupportFragmentManager().getBackStackEntryCount();
        Log.e("StackCount",stackcount+"");

        if (stackcount == 0) {
            Utils.showDialog(CustomerHomeActivity.this, "Alert", "Are yu sure you want to exit the application?", new Utils.iPostiveBtnListener() {
                @Override
                public void onPositiveBtnClicked() {
                    finishAffinity();

                }
            });
        }

        if (stackcount == 1) {
            Utils.showDialog(CustomerHomeActivity.this, "Alert", "Are yu sure you want to exit the application?", new Utils.iPostiveBtnListener() {
                @Override
                public void onPositiveBtnClicked() {
                    finishAffinity();

                }
            });
        }
        else{
            FragmentManager fragmentManager = getSupportFragmentManager();
            int index = fragmentManager.getBackStackEntryCount() - 1;
            String currentFragmentName=fragmentManager.getBackStackEntryAt(index).getName();
            fragmentManager.popBackStackImmediate();
        }




    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.nav_home) {
            this.recreate();
        } else if (id == R.id.nav_logout) {
            SharedPreference.removeAllData();
            Intent intent = new Intent(CustomerHomeActivity.this, LoginActivity.class);
            startActivity(intent);
            finishAffinity();
        }else if (id == R.id.nav_nearbystores) {
            Utils.doFragmentTransition(R.id.mFrameLl,new NearbyStoresFrg(),getSupportFragmentManager(),true);
        }else if (id == R.id.nav_orders) {
            Bundle bundle=new Bundle();
            bundle.putString(Constants.TYPE,Constants.TYPE_CUSTOMER);
            UderOrderHistory uderOrderHistory=new UderOrderHistory();
            uderOrderHistory.setArguments(bundle);
            Utils.doFragmentTransition(R.id.mFrameLl,uderOrderHistory,getSupportFragmentManager(),true);
        }

        else if (id == R.id.nav_history) {
            Utils.doFragmentTransition(R.id.mFrameLl,new PaymentHistory(),getSupportFragmentManager(),true);
        }

        binding.drawerLayout.closeDrawer(Gravity.LEFT);
        return true;
    }
}