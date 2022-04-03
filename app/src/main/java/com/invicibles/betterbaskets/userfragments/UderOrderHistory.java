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
import com.invicibles.betterbaskets.databinding.OrderHistoryBinding;
import com.invicibles.betterbaskets.databinding.PaymentHistoryBinding;
import com.invicibles.betterbaskets.interfaces.Constants;
import com.invicibles.betterbaskets.models.OrderModel;
import com.invicibles.betterbaskets.models.PaymentModel;
import com.invicibles.betterbaskets.models.Users;
import com.invicibles.betterbaskets.utilities.SharedPreference;

import java.util.ArrayList;
import java.util.List;


public class UderOrderHistory extends BaseFrg {

    OrderHistoryBinding binding;
    Users loggedStore;
    DatabaseReference databaseReference;

    List<OrderModel> orderList;
    OrderAdapter orderAdapter;
    String type;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = OrderHistoryBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle bundle=getArguments();
        if(bundle!=null){
            type=bundle.getString(Constants.TYPE);
        }

        databaseReference = initialiseFirebase();
        loggedStore= SharedPreference.getLoggedStore();

        orderList=new ArrayList<>();
        orderAdapter = new OrderAdapter(getActivity(), orderList);
        binding.mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        getOrderHistory();
    }



    private void getOrderHistory() {
        showProgressing(getActivity());
        databaseReference.child(Constants.REF_ORDERS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderList.clear();
                hideProgressing();
                if(type.equalsIgnoreCase(Constants.TYPE_CUSTOMER)){
                    for (DataSnapshot s: snapshot.getChildren()) {
                        OrderModel orders = s.getValue(OrderModel.class);
                        if(loggedStore.getId().equalsIgnoreCase(orders.getUserId())){
                            orderList.add(orders);
                        }

                    }
                }else{
                    for (DataSnapshot s: snapshot.getChildren()) {
                        OrderModel orders = s.getValue(OrderModel.class);
                        if(loggedStore.getId().equalsIgnoreCase(orders.getStoreId())){
                            orderList.add(orders);
                        }

                    }
                }


                Log.e("Test",orderList.size()+"");

                if(orderList.size()==0){
                    binding.mNoDataLl.setVisibility(View.VISIBLE);
                    binding.mRecyclerView.setVisibility(View.GONE);
                }

                binding.mRecyclerView.setAdapter(orderAdapter);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }


    //--------------------------------------Adapter-----------------------------------------
    public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.MyViewHolder> {


        Context context;
        List<OrderModel> childFeedList;

        public OrderAdapter(Context context, List<OrderModel> childFeedList) {
            this.context = context;
            this.childFeedList = childFeedList;

        }

        @NonNull
        @Override
        public OrderAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_order_history_design, parent, false);
            return new OrderAdapter.MyViewHolder(view);
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onBindViewHolder(OrderAdapter.MyViewHolder holder, int position) {

            OrderModel childFeedsModel = childFeedList.get(position);
            holder.mOrderIdTv.setText("Id: "+childFeedsModel.getPaymentId());
            holder.mHeadingTv.setText(childFeedsModel.getSaleModel().getSaleTitle());
            int amountToShow=0;
            for(int i=0;i<childFeedsModel.getSaleModel().getProductsList().size();i++){
                amountToShow=amountToShow+Integer.parseInt(childFeedsModel.getSaleModel().getProductsList().get(i).getSalePrice());
            }

            holder.mAmountTv.setText("Paid: "+Constants.RUPEE_SYMBOL+amountToShow);
            holder.mOrderStatusTv.setText(childFeedsModel.getOrderStatus());

        }





        @Override
        public int getItemCount() {
            return childFeedList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            TextView mDateTv, mHeadingTv, mAmountTv, mOrderIdTv,mOrderStatusTv;
            ImageView mImage;

            public MyViewHolder(View itemView) {
                super(itemView);
                mOrderStatusTv = (TextView) itemView.findViewById(R.id.mOrderStatusTv);
                mHeadingTv = (TextView) itemView.findViewById(R.id.mHeadingTv);
                mOrderIdTv = (TextView) itemView.findViewById(R.id.mOrderId);
                mOrderIdTv = (TextView) itemView.findViewById(R.id.mOrderId);
                mAmountTv = (TextView) itemView.findViewById(R.id.mAmountTv);
                mImage = (ImageView) itemView.findViewById(R.id.mImage);


            }
        }
    }
}
