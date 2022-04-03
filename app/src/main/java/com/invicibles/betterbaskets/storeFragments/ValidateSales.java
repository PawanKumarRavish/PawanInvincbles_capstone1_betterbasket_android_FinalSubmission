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

import com.android.volley.VolleyError;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.invicibles.betterbaskets.BaseFrg;
import com.invicibles.betterbaskets.databinding.StoreHomeFrgBinding;
import com.invicibles.betterbaskets.databinding.ValidateSalesBinding;
import com.invicibles.betterbaskets.interfaces.Constants;
import com.invicibles.betterbaskets.models.OrderModel;
import com.invicibles.betterbaskets.models.PaymentModel;
import com.invicibles.betterbaskets.models.SaleModel;
import com.invicibles.betterbaskets.models.Users;
import com.invicibles.betterbaskets.utilities.SharedPreference;
import com.invicibles.betterbaskets.utilities.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ValidateSales extends BaseFrg {

    ValidateSalesBinding binding;
    Users loggedStore;
    DatabaseReference databaseReference;
    List<SaleModel> saleList;
    String saleId,saleCode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ValidateSalesBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        databaseReference = initialiseFirebase();
        loggedStore= SharedPreference.getLoggedStore();

        saleList=new ArrayList<>();

        getStoreSales(loggedStore.getId());

        binding.mSearchLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.mSearchEt.getText().toString().trim().isEmpty()) {
                    Toast.makeText(getActivity(), "Please enter the sale code", Toast.LENGTH_SHORT).show();
                } else {
                    Utils.hideKeyboard(getActivity());
                    validateSale();

                }
            }
        });

        binding.mCompleteTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseReference.child(Constants.REF_STORE_SALES).child(saleId).child("isCompleted").setValue("true").addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        changeOrderStatus();
                    }
                });


            }
        });


    }


    private void changeOrderStatus() {
        databaseReference.child(Constants.REF_ORDERS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot s: snapshot.getChildren()) {
                    OrderModel orders = s.getValue(OrderModel.class);
                    if(orders.getSaleModel().getCode().equalsIgnoreCase(binding.mSearchEt.getText().toString().trim())){
                        databaseReference.child(Constants.REF_ORDERS).child(orders.getUid()).child("orderStatus").setValue("Delivered");
                        break;
                    }

                }

                Utils.showDialog(getActivity(), "Alert", "Sale Completed Successfully.", new Utils.iPostiveBtnListener() {
                    @Override
                    public void onPositiveBtnClicked() {
                    }
                });
                binding.mSearchEt.setText("");
                binding.mSaleLl.setVisibility(View.GONE);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void validateSale() {
        if(isCodeValid(saleList)){
            binding.mSaleLl.setVisibility(View.VISIBLE);
        }else{
            binding.mSaleLl.setVisibility(View.GONE);
            Utils.showDialog(getActivity(), "Alert", "Either this code is invalid or its already completed", new Utils.iPostiveBtnListener() {
                @Override
                public void onPositiveBtnClicked() {

                }
            });
        }

    }

    public boolean isCodeValid(List<SaleModel> saleList){
        boolean isCodeValid=false;
        for(int i=0;i<saleList.size();i++){
            if(saleList.get(i).getIsCompleted().equalsIgnoreCase("false")&& saleList.get(i).getCode().equalsIgnoreCase(binding.mSearchEt.getText().toString().trim())){
                isCodeValid=true;
                binding.mTitleTv.setText(saleList.get(i).getSaleTitle());
                binding.mDesTv.setText(saleList.get(i).getDescription());
                binding.mStartDateTv.setText(saleList.get(i).getSaleStartDate());
                binding.mEndDateTv.setText(saleList.get(i).getSaleEndDate());
                saleId=saleList.get(i).getUid();
                saleCode=saleList.get(i).getCode();

            }
        }
        return isCodeValid;
    }

    private void getStoreSales(String id) {
        showProgressing(getActivity());
        databaseReference.child(Constants.REF_STORE_SALES).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                saleList.clear();
                hideProgressing();
                for (DataSnapshot s: snapshot.getChildren()) {
                    SaleModel saleModel = s.getValue(SaleModel.class);
                    if(saleModel.getStoreId().equalsIgnoreCase(id)){
                        saleList.add(saleModel);
                    }

                }

                Log.e("Test",saleList.size()+"");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });


    }
}
