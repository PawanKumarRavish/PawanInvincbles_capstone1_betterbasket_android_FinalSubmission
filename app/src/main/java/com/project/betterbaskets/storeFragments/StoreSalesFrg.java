package com.project.betterbaskets.storeFragments;

import static com.project.betterbaskets.utilities.Utils.initialiseFirebase;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.project.betterbaskets.BaseFrg;
import com.project.betterbaskets.R;
import com.project.betterbaskets.databinding.AddSaleLayoutBinding;
import com.project.betterbaskets.databinding.StoreSalesLayoutBinding;
import com.project.betterbaskets.interfaces.Constants;
import com.project.betterbaskets.models.Products;
import com.project.betterbaskets.models.SaleModel;
import com.project.betterbaskets.models.SearchProductsModel;
import com.project.betterbaskets.models.Users;
import com.project.betterbaskets.utilities.SharedPreference;
import com.project.betterbaskets.utilities.Utils;

import java.util.ArrayList;
import java.util.List;

public class StoreSalesFrg extends BaseFrg {

    StoreSalesLayoutBinding binding;
    DatabaseReference databaseReference;
    Users loggedStore;
    List<SaleModel> saleList;
    SalesAdapter salesAdapter;
    String storeId,type;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = StoreSalesLayoutBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        databaseReference = initialiseFirebase();
        loggedStore= SharedPreference.getLoggedStore();
        saleList=new ArrayList<>();
        salesAdapter = new SalesAdapter(getActivity(), saleList);
        binding.mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        Bundle bundle=getArguments();
        if(bundle!=null){
            storeId=bundle.getString(Constants.STORE_ID);
            type=bundle.getString(Constants.TYPE);
        }

        if(type.equalsIgnoreCase(Constants.TYPE_CUSTOMER)){
            binding.mAddSaleBtn.setVisibility(View.GONE);
        }

        getStoreSales(storeId);

        binding.mAddSaleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.doFragmentTransition(R.id.mFrameLl,new AddSaleFrg(),getActivity().getSupportFragmentManager(),true);
            }
        });


    }

    private void getStoreSales(String loogedStoreId) {
        showProgressing(getActivity());
        databaseReference.child(Constants.REF_STORE_SALES).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                saleList.clear();
                hideProgressing();
                for (DataSnapshot s: snapshot.getChildren()) {
                    SaleModel saleModel = s.getValue(SaleModel.class);
                    if(saleModel.getStoreId().equalsIgnoreCase(loogedStoreId)){
                        saleList.add(saleModel);
                    }

                }

                Log.e("Test",saleList.size()+"");

                if(saleList.size()==0){
                    binding.mNoDataLl.setVisibility(View.VISIBLE);
                    binding.mRecyclerView.setVisibility(View.GONE);
                }

                binding.mRecyclerView.setAdapter(salesAdapter);



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }



    //--------------------------------------Adapter-----------------------------------------
    public class SalesAdapter extends RecyclerView.Adapter<SalesAdapter.MyViewHolder> {


        Context context;
        List<SaleModel> childFeedList;

        public SalesAdapter(Context context, List<SaleModel> childFeedList) {
            this.context = context;
            this.childFeedList = childFeedList;

        }

        @NonNull
        @Override
        public SalesAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sales_design, parent, false);
            return new SalesAdapter.MyViewHolder(view);
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onBindViewHolder(SalesAdapter.MyViewHolder holder, int position) {

            SaleModel childFeedsModel = childFeedList.get(position);
            holder.mNameTv.setText(position+1+". "+childFeedsModel.getDescription());
            holder.mDeleteImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteSale(childFeedsModel.getUid());

                }
            });

            holder.mEditImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });

        }

        private void deleteSale(String uid) {
            databaseReference.child(Constants.REF_STORE_SALES).child(uid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(getActivity(), "Sale deleted successfully", Toast.LENGTH_SHORT).show();
                        getStoreSales(storeId);

                    }else{
                        Toast.makeText(getActivity(), "Error in deleting sale", Toast.LENGTH_SHORT).show();
                    }


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

                if(type.equalsIgnoreCase(Constants.TYPE_CUSTOMER)){
                    mDeleteImg.setVisibility(View.GONE);
                    mEditImg.setVisibility(View.GONE);
                }


            }
        }
    }
}
