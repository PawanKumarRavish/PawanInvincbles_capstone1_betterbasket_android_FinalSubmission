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
import com.invicibles.betterbaskets.databinding.PaymentHistoryBinding;
import com.invicibles.betterbaskets.interfaces.Constants;
import com.invicibles.betterbaskets.models.PaymentModel;
import com.invicibles.betterbaskets.models.Users;
import com.invicibles.betterbaskets.utilities.SharedPreference;

import java.util.ArrayList;
import java.util.List;

public class PaymentHistory extends BaseFrg {

    PaymentHistoryBinding binding;
    Users loggedStore;
    DatabaseReference databaseReference;

    List<PaymentModel> paymentList;
    PaymentAdapter paymentAdapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = PaymentHistoryBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        databaseReference = initialiseFirebase();
        loggedStore= SharedPreference.getLoggedStore();

        paymentList=new ArrayList<>();
        paymentAdapter = new PaymentAdapter(getActivity(), paymentList);
        binding.mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        getPaymentHistory();


    }

    private void getPaymentHistory() {
        showProgressing(getActivity());
        databaseReference.child(Constants.REF_PAYMENTS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                paymentList.clear();
                hideProgressing();
                for (DataSnapshot s: snapshot.getChildren()) {
                    PaymentModel users = s.getValue(PaymentModel.class);
                    if(loggedStore.getId().equalsIgnoreCase(users.getUserId())){
                        paymentList.add(users);
                    }

                }

                Log.e("Test",paymentList.size()+"");

                if(paymentList.size()==0){
                    binding.mNoDataLl.setVisibility(View.VISIBLE);
                    binding.mRecyclerView.setVisibility(View.GONE);
                }

                binding.mRecyclerView.setAdapter(paymentAdapter);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }


    //--------------------------------------Adapter-----------------------------------------
    public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.MyViewHolder> {


        Context context;
        List<PaymentModel> childFeedList;

        public PaymentAdapter(Context context, List<PaymentModel> childFeedList) {
            this.context = context;
            this.childFeedList = childFeedList;

        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history_design, parent, false);
            return new MyViewHolder(view);
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {

            PaymentModel childFeedsModel = childFeedList.get(position);
            holder.mOrderIdTv.setText("Payment Id: "+childFeedsModel.getPaymentId());
            holder.mHeadingTv.setText(childFeedsModel.getSaleModel().getSaleTitle());
            int amountToShow=0;
            for(int i=0;i<childFeedsModel.getSaleModel().getProductsList().size();i++){
                amountToShow=amountToShow+Integer.parseInt(childFeedsModel.getSaleModel().getProductsList().get(i).getSalePrice());
            }

            holder.mAmountTv.setText("Paid: "+Constants.RUPEE_SYMBOL+amountToShow);

        }





        @Override
        public int getItemCount() {
            return childFeedList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            TextView mDateTv, mHeadingTv, mAmountTv, mOrderIdTv;
            ImageView mImage;

            public MyViewHolder(View itemView) {
                super(itemView);
                mDateTv = (TextView) itemView.findViewById(R.id.mDateTv);
                mHeadingTv = (TextView) itemView.findViewById(R.id.mHeadingTv);
                mOrderIdTv = (TextView) itemView.findViewById(R.id.mOrderId);
                mOrderIdTv = (TextView) itemView.findViewById(R.id.mOrderId);
                mAmountTv = (TextView) itemView.findViewById(R.id.mAmountTv);
                mImage = (ImageView) itemView.findViewById(R.id.mImage);


            }
        }
    }
}
