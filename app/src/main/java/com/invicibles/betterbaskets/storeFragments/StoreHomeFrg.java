package com.invicibles.betterbaskets.storeFragments;

import static com.invicibles.betterbaskets.utilities.Utils.initialiseFirebase;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DatabaseReference;
import com.invicibles.betterbaskets.BaseFrg;
import com.invicibles.betterbaskets.databinding.StoreHomeFrgBinding;
import com.invicibles.betterbaskets.models.Users;
import com.invicibles.betterbaskets.utilities.SharedPreference;

public class StoreHomeFrg extends BaseFrg {

    StoreHomeFrgBinding binding;
    Users loggedStore;
    DatabaseReference databaseReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = StoreHomeFrgBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        databaseReference = initialiseFirebase();
        loggedStore= SharedPreference.getLoggedStore();

        binding.mStoreNameTv.setText("Hi, "+loggedStore.getName());
    }
}
