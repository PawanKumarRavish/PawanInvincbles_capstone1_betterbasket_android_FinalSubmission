package com.invicibles.betterbaskets.userfragments;

import static com.invicibles.betterbaskets.utilities.Utils.initialiseFirebase;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.invicibles.betterbaskets.BaseFrg;
import com.invicibles.betterbaskets.R;
import com.invicibles.betterbaskets.databinding.NearbyStoresLayoutBinding;
import com.invicibles.betterbaskets.interfaces.Constants;
import com.invicibles.betterbaskets.models.Users;
import com.invicibles.betterbaskets.storeFragments.StoreSalesFrg;
import com.invicibles.betterbaskets.utilities.SharedPreference;
import com.invicibles.betterbaskets.utilities.Utils;

import java.util.ArrayList;
import java.util.List;

public class NearbyStoresFrg extends BaseFrg {

    NearbyStoresLayoutBinding binding;
    DatabaseReference databaseReference;
    Users loggedStore;
    List<Users> storeList;
    StoresAdapter storesAdapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = NearbyStoresLayoutBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        databaseReference = initialiseFirebase();
        loggedStore= SharedPreference.getLoggedStore();

        storeList=new ArrayList<>();
        storesAdapter = new StoresAdapter(getActivity(), storeList);
        binding.mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        showNearbyStores();
    }


    private void showNearbyStores() {
        showProgressing(getActivity());
        databaseReference.child(Constants.REF_USERS).child(Constants.REF_STORES).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                storeList.clear();
                hideProgressing();
                for (DataSnapshot s: snapshot.getChildren()) {
                    Users users = s.getValue(Users.class);
                    storeList.add(users);

                }

                Log.e("Test",storeList.size()+"");

                if(storeList.size()==0){
                    binding.mNoDataLl.setVisibility(View.VISIBLE);
                    binding.mRecyclerView.setVisibility(View.GONE);
                }

                binding.mRecyclerView.setAdapter(storesAdapter);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });


    }


    //--------------------------------------Adapter-----------------------------------------
    public class StoresAdapter extends RecyclerView.Adapter<StoresAdapter.MyViewHolder> {


        Context context;
        List<Users> childFeedList;

        public StoresAdapter(Context context, List<Users> childFeedList) {
            this.context = context;
            this.childFeedList = childFeedList;

        }

        @NonNull
        @Override
        public StoresAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stores_design, parent, false);
            return new StoresAdapter.MyViewHolder(view);
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onBindViewHolder(StoresAdapter.MyViewHolder holder, int position) {

            Users childFeedsModel = childFeedList.get(position);
            holder.mAddressTv.setText(childFeedsModel.getAddress());
            holder.mStoreNameTv.setText(childFeedsModel.getName());

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle bundle=new Bundle();
                    bundle.putString(Constants.STORE_ID,childFeedsModel.getId());
                    bundle.putString(Constants.TYPE,Constants.TYPE_CUSTOMER);
                    StoreSalesFrg storeSalesFrg=new StoreSalesFrg();
                    storeSalesFrg.setArguments(bundle);
                    Utils.doFragmentTransition(R.id.mFrameLl,storeSalesFrg,getActivity().getSupportFragmentManager(),true);

                }
            });


        }





        @Override
        public int getItemCount() {
            return childFeedList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            TextView mStoreNameTv, mAddressTv;
            ImageView mImage;

            public MyViewHolder(View itemView) {
                super(itemView);
                mStoreNameTv = (TextView) itemView.findViewById(R.id.mStoreNameTv);
                mAddressTv = (TextView) itemView.findViewById(R.id.mAddressTv);
                mImage = (ImageView) itemView.findViewById(R.id.mImage);


            }
        }
    }
}
