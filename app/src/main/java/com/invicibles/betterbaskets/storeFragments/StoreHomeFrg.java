package com.invicibles.betterbaskets.storeFragments;

import static com.invicibles.betterbaskets.utilities.Utils.initialiseFirebase;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.invicibles.betterbaskets.BaseFrg;
import com.invicibles.betterbaskets.databinding.StoreHomeFrgBinding;
import com.invicibles.betterbaskets.interfaces.Constants;
import com.invicibles.betterbaskets.models.PaymentModel;
import com.invicibles.betterbaskets.models.SaleModel;
import com.invicibles.betterbaskets.models.Users;
import com.invicibles.betterbaskets.utilities.SharedPreference;

import java.util.ArrayList;
import java.util.List;

public class StoreHomeFrg extends BaseFrg {

    StoreHomeFrgBinding binding;
    Users loggedStore;
    DatabaseReference databaseReference;
   int totalMoneyEarned=0;
   int totalSales=0;
    List<SaleModel> saleList;

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
        saleList=new ArrayList<>();

        binding.mStoreNameTv.setText("Hi, "+loggedStore.getName());

        getPaymentHistory();

        getStoreSales(loggedStore.getId());
    }

    private void getStoreSales(String loogedStoreId) {
        databaseReference.child(Constants.REF_STORE_SALES).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                hideProgressing();
                saleList.clear();
                for (DataSnapshot s: snapshot.getChildren()) {
                    SaleModel saleModel = s.getValue(SaleModel.class);
                    if(saleModel.getStoreId().equalsIgnoreCase(loogedStoreId)){
                        saleList.add(saleModel);
                    }

                }

                if(saleList.size()==0){
                    binding.mTotalSalesTv.setText("0");
                }else{
                    binding.mTotalSalesTv.setText(saleList.size()+"");
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void getPaymentHistory() {
        showProgressing(getActivity());
        databaseReference.child(Constants.REF_PAYMENTS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                hideProgressing();
                for (DataSnapshot s: snapshot.getChildren()) {
                    PaymentModel users = s.getValue(PaymentModel.class);
                    if(loggedStore.getId().equalsIgnoreCase(users.getStoreId())){
                        for(int i=0;i<users.getSaleModel().getProductsList().size();i++){
                            totalMoneyEarned=totalMoneyEarned+Integer.parseInt(users.getSaleModel().getProductsList().get(i).getSalePrice());
                        }
                    }

                }

                binding.mTotalMoneyEarnedTv.setText("$"+String.valueOf(totalMoneyEarned));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }
}
