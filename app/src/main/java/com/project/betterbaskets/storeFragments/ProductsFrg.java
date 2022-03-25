package com.project.betterbaskets.storeFragments;

import static android.app.Activity.RESULT_OK;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.project.betterbaskets.BaseFrg;
import com.project.betterbaskets.R;
import com.project.betterbaskets.activities.LoginActivity;
import com.project.betterbaskets.activities.OTPVerificationActivity;
import com.project.betterbaskets.activities.RegisterActivity;
import com.project.betterbaskets.activities.StoreHomeActivity;
import com.project.betterbaskets.databinding.ProductsLayoutBinding;
import com.project.betterbaskets.databinding.StoreHomeFrgBinding;
import com.project.betterbaskets.interfaces.Constants;
import com.project.betterbaskets.models.Products;
import com.project.betterbaskets.models.Users;
import com.project.betterbaskets.utilities.SharedPreference;
import com.project.betterbaskets.utilities.Utils;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProductsFrg extends BaseFrg {

    ProductsLayoutBinding binding;
    DatabaseReference databaseReference;
    Dialog alertDialog;
    Uri image_uri;
    Bitmap resizedBitmap=null;
    String encodedTaxImage = "";
    ImageView mImg;
    Users loggedStore;
    List<Products> productsList;
    ProductsAdapter productsAdapter;
    private static int SPLASH_TIME_OUT = 3000;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ProductsLayoutBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        databaseReference = Utils.initialiseFirebase();
        loggedStore= SharedPreference.getLoggedStore();
        productsList=new ArrayList<>();
        productsAdapter = new ProductsAdapter(getActivity(), productsList);
        binding.mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));


        showStoreProducts(loggedStore.getId());

        binding.mAddProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addProductDialog(getActivity());
            }
        });


    }

    private void showStoreProducts(String loogedStoreId) {
        showProgressing(getActivity());
        databaseReference.child(Constants.REF_STORE_PRODUCTS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productsList.clear();
                hideProgressing();
                for (DataSnapshot s: snapshot.getChildren()) {
                    Products products = s.getValue(Products.class);
                    if(products.getStoreId().equalsIgnoreCase(loogedStoreId)){
                        productsList.add(products);
                    }

                }

                Log.e("Test",productsList.size()+"");

                if(productsList.size()==0){
                    binding.mNoDataLl.setVisibility(View.VISIBLE);
                    binding.mRecyclerView.setVisibility(View.GONE);
                }

                binding.mRecyclerView.setAdapter(productsAdapter);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });




    }


    public void addProductDialog(Context context) {
        View view = getLayoutInflater().inflate(R.layout.dialog_add_product, null);
        Button mAddProduct=view.findViewById(R.id.mAddProductBtn);
        TextInputEditText mName=view.findViewById(R.id.mNameEt);


        mAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mName.getText().toString().trim().isEmpty()){
                    Toast.makeText(getActivity(), "Add product name", Toast.LENGTH_SHORT).show();
                } else{
                    addProduct(mName.getText().toString().trim());
                }
            }
        });


        alertDialog = new Dialog(getActivity());
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE); //this line MUST BE BEFORE setContentView
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(true);
        alertDialog.setContentView(view);
        alertDialog.show();
        Window window = alertDialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);




    }

    private void addProduct(String name) {
        showProgressing(getActivity());
        DatabaseReference productsRef = databaseReference.child(Constants.REF_STORE_PRODUCTS);
        DatabaseReference newProdRef = productsRef.push();

        String uid=newProdRef.getKey();
        newProdRef.setValue(new Products(uid,name,loggedStore.getId()), new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if(error==null){
                    hideProgressing();
                    Toast.makeText(getActivity(), "Product saved successfully", Toast.LENGTH_LONG).show();
                    alertDialog.dismiss();
                   /* new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            showStoreProducts(loggedStore.getId());
                        }
                    }, SPLASH_TIME_OUT);*/


                }else{
                    hideProgressing();
                    Toast.makeText(getActivity(), "Error in saving product "+error.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        });

    }



    //--------------------------------------SalonAdapter-----------------------------------------
    public class ProductsAdapter extends RecyclerView.Adapter<ProductsAdapter.MyViewHolder> {

        Context context;
        List<Products> childFeedList;

        public ProductsAdapter(Context context, List<Products> childFeedList) {
            this.context = context;
            this.childFeedList = childFeedList;

        }

        @NonNull
        @Override
        public ProductsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_products_design, parent, false);
            return new ProductsAdapter.MyViewHolder(view);
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onBindViewHolder(ProductsAdapter.MyViewHolder holder, int position) {

            Products childFeedsModel = childFeedList.get(position);
            holder.mNameTv.setText(position+1+". "+childFeedsModel.getName());
            holder.mDeleteImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteProduct(childFeedsModel.getUid());

                }
            });

            holder.mEditImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });

        }


        @Override
        public int getItemCount() {
            return childFeedList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            TextView mNameTv;
            ImageView mDeleteImg,mEditImg;


            public MyViewHolder(View itemView) {
                super(itemView);
                mNameTv = (TextView) itemView.findViewById(R.id.mNameTv);
                mDeleteImg=itemView.findViewById(R.id.mDeleteImg);
                mEditImg=itemView.findViewById(R.id.mEditImg);


            }
        }
    }

    private void deleteProduct(String uid) {
        databaseReference.child(Constants.REF_STORE_PRODUCTS).child(uid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(getActivity(), "Product deleted successfully", Toast.LENGTH_SHORT).show();
                    showStoreProducts(loggedStore.getId());

                }else{
                    Toast.makeText(getActivity(), "Error in deleting product", Toast.LENGTH_SHORT).show();
                }


            }
        });

    }
}
