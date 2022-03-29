package com.project.betterbaskets.storeFragments;

import static android.app.Activity.RESULT_OK;
import static com.project.betterbaskets.utilities.Utils.initialiseFirebase;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
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

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.project.betterbaskets.BaseFrg;
import com.project.betterbaskets.R;
import com.project.betterbaskets.activities.DemoActivity;
import com.project.betterbaskets.activities.StripePaymentActivity;
import com.project.betterbaskets.databinding.AddSaleLayoutBinding;
import com.project.betterbaskets.databinding.StoreSalesLayoutBinding;
import com.project.betterbaskets.interfaces.Constants;
import com.project.betterbaskets.models.PaymentModel;
import com.project.betterbaskets.models.Products;
import com.project.betterbaskets.models.SaleModel;
import com.project.betterbaskets.models.SearchProductsModel;
import com.project.betterbaskets.models.Users;
import com.project.betterbaskets.userfragments.UserHomeFrg;
import com.project.betterbaskets.utilities.SharedPreference;
import com.project.betterbaskets.utilities.Utils;
import com.razorpay.Checkout;
import com.razorpay.PaymentData;
import com.razorpay.PaymentResultWithDataListener;
import com.stripe.android.ApiResultCallback;
import com.stripe.android.PaymentIntentResult;
import com.stripe.android.Stripe;
import com.stripe.android.model.ConfirmPaymentIntentParams;
import com.stripe.android.model.PaymentIntent;
import com.stripe.android.model.PaymentMethodCreateParams;
import com.stripe.android.view.CardInputWidget;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class StoreSalesFrg extends BaseFrg implements PaymentResultWithDataListener,ApiResultCallback<PaymentIntentResult> {

    StoreSalesLayoutBinding binding;
    DatabaseReference databaseReference;
    Users loggedStore;
    List<SaleModel> saleList;
    SalesAdapter salesAdapter;
    String storeId,type;
    SaleModel childFeedsModel;
    private Stripe stripe;
    private String paymentIntentClientSecret = "";
    Dialog alertDialog;


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


    private void generateRazorOrder(int amount) {
        showProgressing(getActivity());
        //callServicePOST("https://api.razorpay.com/v1/orders", amount);
    }


    private void openRazorCheckoutPage(String orderId, String currency, String amount, String receipt) {

        final Checkout co = new Checkout();

        try {
            JSONObject options = new JSONObject();
            options.put("name", "Better Baskets");
            options.put("description", "Payment");
            options.put("image", "");
            options.put("order_id", orderId);//from response of step 3.
            options.put("theme.color", "#3fb8d3");
            options.put("currency", currency);
            options.put("amount", amount);//pass amount in currency subunits
            options.put("prefill.email", loggedStore.getName());
            options.put("prefill.contact", loggedStore.getPhone());

            co.open(getActivity(), options);
        } catch (Exception e) {
            Toast.makeText(getActivity(), "Error in payment: " + e.getMessage(), Toast.LENGTH_SHORT)
                    .show();
            e.printStackTrace();
        }

    }

    @Override
    public void onPaymentSuccess(String razorpayPaymentId, PaymentData paymentData) {
        Toast.makeText(getActivity(), "Payment Successful: " + razorpayPaymentId, Toast.LENGTH_LONG).show();
        showSuccessDialog(razorpayPaymentId);
    }

    @Override
    public void onPaymentError(int code, String response, PaymentData paymentData) {
        Toast.makeText(getActivity(), "Payment failed: " + code + " " + response, Toast.LENGTH_LONG).show();

    }

    private void showSuccessDialog(String razorpayPaymentId) {
        Dialog alertDialog = new Dialog(getActivity());
        ;
        View view = getLayoutInflater().inflate(R.layout.payment_success_layout, null);

        Button homeBtn = (Button) view.findViewById(R.id.homeBtn);
        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                savePaymentResponse(razorpayPaymentId);


            }
        });

        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE); //this line MUST BE BEFORE setContentView
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(false);
        alertDialog.setContentView(view);
        alertDialog.show();
        Window window = alertDialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private void savePaymentResponse(String razorpayPaymentId) {
        showProgressing(getActivity());
        DatabaseReference paymentRef = databaseReference.child(Constants.REF_PAYMENTS);
        DatabaseReference newProdRef = paymentRef.push();

        String uid=newProdRef.getKey();
        newProdRef.setValue(new PaymentModel(uid,razorpayPaymentId,childFeedsModel), new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if(error==null){
                    hideProgressing();
                    Toast.makeText(getActivity(), "Payment Data saved successfully", Toast.LENGTH_LONG).show();
                    Utils.doFragmentTransition(R.id.mFrameLl,new UserHomeFrg(),getActivity().getSupportFragmentManager(),false);


                }else{
                    hideProgressing();
                    Toast.makeText(getActivity(), "Error in saving payment data "+error.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        });



    }

    @Override
    public void onError(@NonNull Exception e) {
        Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSuccess(@NonNull PaymentIntentResult paymentIntentResult) {
        Toast.makeText(getActivity(), "Success", Toast.LENGTH_SHORT).show();

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

             childFeedsModel = childFeedList.get(position);
            holder.mTitleTv.setText(childFeedsModel.getSaleTitle());
            holder.mDesTv.setText(childFeedsModel.getDescription());
            holder.mStartDateTv.setText("Start Date: "+childFeedsModel.getSaleStartDate());
            holder.mEndDateTv.setText("End Date: "+childFeedsModel.getSaleEndDate());
            holder.mStoreNameTv.setText(childFeedsModel.getStoreName());

            int totalAmountToPay=0;
            for(int i=0;i<childFeedsModel.getProductsList().size();i++){
                totalAmountToPay=totalAmountToPay+Integer.parseInt(childFeedsModel.getProductsList().get(i).getSalePrice());
            }

            holder.mCheckoutTv.setText("Proceed to Checkout ("+Constants.RUPEE_SYMBOL+totalAmountToPay+")");


            holder.mDeleteTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteSale(childFeedsModel.getUid());
                }
            });

            holder.mEditTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });

            int finalTotalAmountToPay = totalAmountToPay;
            holder.mCheckoutTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    doStripePayment(finalTotalAmountToPay);
                    //startActivity(new Intent(getActivity(), DemoActivity.class));
                    //generateRazorOrder(finalTotalAmountToPay);


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
            TextView mTitleTv,mViewDetailsTv,mCheckoutTv,mDeleteTv,mEditTv,mStoreNameTv,mDesTv,
            mStartDateTv,mEndDateTv;

            LinearLayout mCheckoutLl,deleteLl;


            public MyViewHolder(View itemView) {
                super(itemView);
                mViewDetailsTv = (TextView) itemView.findViewById(R.id.mViewDetailsTv);
                mCheckoutTv = (TextView) itemView.findViewById(R.id.mCheckoutTv);
                mDeleteTv = (TextView) itemView.findViewById(R.id.mDeleteTv);
                mEditTv = (TextView) itemView.findViewById(R.id.mEditTv);
                mStoreNameTv = (TextView) itemView.findViewById(R.id.mStoreNameTv);
                mTitleTv = (TextView) itemView.findViewById(R.id.mTitleTv);
                mDesTv = (TextView) itemView.findViewById(R.id.mDesTv);
                mStartDateTv = (TextView) itemView.findViewById(R.id.mStartDateTv);
                mEndDateTv = (TextView) itemView.findViewById(R.id.mEndDateTv);
                mCheckoutLl = (LinearLayout) itemView.findViewById(R.id.mCheckoutLl);
                deleteLl = (LinearLayout) itemView.findViewById(R.id.deleteLl);



                if(type.equalsIgnoreCase(Constants.TYPE_CUSTOMER)){
                    mCheckoutLl.setVisibility(View.VISIBLE);
                    deleteLl.setVisibility(View.GONE);
                }else{
                    mCheckoutLl.setVisibility(View.GONE);
                    deleteLl.setVisibility(View.VISIBLE);
                }


            }
        }
    }

    private void doStripePayment(int finalTotalAmountToPay) {
        // Configure the SDK with your Stripe publishable key so it can make requests to Stripe
        stripe = new Stripe(getActivity(),
                Objects.requireNonNull("pk_test_51IJzmwJRXiKp3vHjNXHPKAgRtdEp6fPhUOroZQQUyC87TvsfPrX5pA9AtIZQ6z4zjznqWWeRCGdach2SSV8Xk5Xw00A7CPkKrq")
        );
        startCheckout(finalTotalAmountToPay);

    }


    private void startCheckout(int finalAmountToPay) {
        showProgressing(getActivity());
        callServiceUrlEncoded("https://api.stripe.com/v1/payment_intents",finalAmountToPay);

    }







    protected void callServiceUrlEncoded(String url, int finalAmountToPay) {
        if(Utils.isInternetConnected(getActivity())){
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                response(response,finalAmountToPay);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            failure(error);


                        }
                    }){

                @Override
                public String getBodyContentType() {
                    return "application/x-www-form-urlencoded;";
                }

                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    int total=finalAmountToPay*100;
                    params.put("amount",String.valueOf(total));
                    params.put("currency","inr");

                    Log.e("Params",params.toString());

                    return params;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String>  params = new HashMap<String, String>();
                    params.put("Authorization", "Bearer sk_test_51IJzmwJRXiKp3vHjycLc3GYVrIKNGwt8iu6gdsXW7MQyriYt6304clTSiN2r3rbW6GC0G6y3kpCKcXNcD1EX9INx00SBqchXAd");

                    return params;
                }

            };

            RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(35000, 0, 1.0f));
            requestQueue.add(stringRequest);
            requestQueue.getCache().clear();

        }else{
            Toast.makeText(getActivity(), getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
        }

    }


    public void response(String response, int finalAmountToPay) {
        hideProgressing();
        try {
            JSONObject objResponse=new JSONObject(response);
            paymentIntentClientSecret=objResponse.optString("client_secret");
            Toast.makeText(getActivity(), "Got Client secret", Toast.LENGTH_SHORT).show();
            openStripeCheckoutPage(finalAmountToPay);

        } catch (Exception e) {
            hideProgressing();
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void openStripeCheckoutPage(int finalAmountToPay) {
        Intent intent=new Intent(getActivity(), StripePaymentActivity.class);
        intent.putExtra("Secret",paymentIntentClientSecret);
        intent.putExtra("Amount",String.valueOf(finalAmountToPay));
        startActivityForResult(intent,10);
    }

    public void failure(VolleyError error) {
        hideProgressing();
        Toast.makeText(getActivity(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
    }

    public void addParameters(Map<String, String> map) {
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10){
            if (resultCode == RESULT_OK){
                String id = data.getExtras().getString("id");
                Log.e("Id",id);
                showSuccessDialog(id);
            }
        }

    }
}
