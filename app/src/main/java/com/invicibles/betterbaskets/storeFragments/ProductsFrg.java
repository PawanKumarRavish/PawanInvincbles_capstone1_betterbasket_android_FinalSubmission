package com.invicibles.betterbaskets.storeFragments;

import static android.app.Activity.RESULT_OK;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.invicibles.betterbaskets.BaseFrg;
import com.invicibles.betterbaskets.R;
import com.invicibles.betterbaskets.databinding.ProductsLayoutBinding;
import com.invicibles.betterbaskets.databinding.StoreHomeFrgBinding;
import com.invicibles.betterbaskets.interfaces.Constants;
import com.invicibles.betterbaskets.models.Products;
import com.invicibles.betterbaskets.models.Users;
import com.invicibles.betterbaskets.utilities.SharedPreference;
import com.invicibles.betterbaskets.utilities.Utils;

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

    //constant to track image chooser intent
    private static final int PICK_IMAGE_REQUEST = 234;
    //uri to store file
    private Uri filePath;

    //firebase objects
    private StorageReference storageReference;
    ImageView mProductImg;
    Uri downloadUrl=null;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ProductsLayoutBinding.inflate(inflater,container,false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        storageReference = FirebaseStorage.getInstance().getReference();



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
         LinearLayout mProductImgLl=view.findViewById(R.id.mProductImgLl);
        mProductImg=view.findViewById(R.id.mProductImg);

        mProductImgLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });


        mAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (downloadUrl == null) {
                    Toast.makeText(getActivity(), "Please add image", Toast.LENGTH_SHORT).show();
                } else if(mName.getText().toString().trim().isEmpty()){
                    Toast.makeText(getActivity(), "Add product name", Toast.LENGTH_SHORT).show();
                } else{
                    addProduct(mName.getText().toString().trim(),downloadUrl);
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

    private void addProduct(String name, Uri downloadUrl) {
        showProgressing(getActivity());
        DatabaseReference productsRef = databaseReference.child(Constants.REF_STORE_PRODUCTS);
        DatabaseReference newProdRef = productsRef.push();

        String uid=newProdRef.getKey();
        newProdRef.setValue(new Products(uid,name,loggedStore.getId(),downloadUrl.toString()), new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if(error==null){
                    hideProgressing();
                    Toast.makeText(getActivity(), "Product saved successfully", Toast.LENGTH_LONG).show();
                    alertDialog.dismiss();


                }else{
                    hideProgressing();
                    Toast.makeText(getActivity(), "Error in saving product "+error.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        });

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filePath);
                mProductImg.setImageBitmap(bitmap);

                uploadImage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImage() {
        if (filePath != null) {
            showProgressing(getActivity());
            StorageReference sRef = storageReference.child(Constants.REF_PRODUCTS_IMAGES).child(System.currentTimeMillis() + "." + Utils.getFileExtension(getActivity(),filePath));

            //adding the file to reference
            sRef.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            hideProgressing();

                            //displaying success toast
                            Toast.makeText(getActivity(), "File Uploaded ", Toast.LENGTH_LONG).show();
                           sRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                               @Override
                               public void onSuccess(Uri uri) {
                                   downloadUrl = uri;
                                   Log.e("Uri",downloadUrl+"");
                               }
                           });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                           hideProgressing();
                            Toast.makeText(getActivity(), exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                    
        } 
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
            holder.mNameTv.setText(childFeedsModel.getName());
            Glide.with(getActivity()).load(childFeedsModel.getDownloadUrl()).into(holder.mProductImg);
            holder.mDeleteImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Utils.showDialog(getActivity(), "Alert", "Are you sure you want to delete the product", new Utils.iPostiveBtnListener() {
                        @Override
                        public void onPositiveBtnClicked() {
                            deleteProduct(childFeedsModel.getUid());
                        }
                    });


                }
            });

            holder.mEditImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showEditProductDialog(childFeedsModel);

                }
            });

        }


        @Override
        public int getItemCount() {
            return childFeedList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            TextView mNameTv;
            ImageView mDeleteImg,mEditImg,mProductImg;


            public MyViewHolder(View itemView) {
                super(itemView);
                mNameTv = (TextView) itemView.findViewById(R.id.mNameTv);
                mDeleteImg=itemView.findViewById(R.id.mDeleteImg);
                mEditImg=itemView.findViewById(R.id.mEditImg);
                mProductImg=itemView.findViewById(R.id.mProductImg);


            }
        }
    }

    private void showEditProductDialog(Products childFeedsModel) {
        View view = getLayoutInflater().inflate(R.layout.dialog_edit_product, null);

        Button mEditProduct=view.findViewById(R.id.mAddProductBtn);
        TextInputEditText mName=view.findViewById(R.id.mNameEt);

        mName.setText(childFeedsModel.getName());


        mEditProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mName.getText().toString().trim().isEmpty()){
                    Toast.makeText(getActivity(), "Add product name", Toast.LENGTH_SHORT).show();
                } else{
                    editProduct(childFeedsModel,mName.getText().toString().trim());
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

    private void editProduct(Products childFeedsModel, String name) {
        showProgressing(getActivity());
        DatabaseReference productsRef = databaseReference.child(Constants.REF_STORE_PRODUCTS).child(childFeedsModel.getUid());

        productsRef.setValue(new Products(childFeedsModel.getUid(),name,loggedStore.getId(),childFeedsModel.getDownloadUrl()), new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if(error==null){
                    hideProgressing();
                    Toast.makeText(getActivity(), "Product edited successfully", Toast.LENGTH_LONG).show();
                    alertDialog.dismiss();

                }else{
                    hideProgressing();
                    Toast.makeText(getActivity(), "Error in editing product "+error.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        });

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
