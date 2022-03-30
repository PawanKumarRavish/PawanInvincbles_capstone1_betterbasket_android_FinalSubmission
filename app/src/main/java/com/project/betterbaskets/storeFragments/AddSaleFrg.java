package com.project.betterbaskets.storeFragments;

import static com.project.betterbaskets.utilities.Utils.*;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.project.betterbaskets.BaseFrg;
import com.project.betterbaskets.R;
import com.project.betterbaskets.databinding.AddSaleLayoutBinding;
import com.project.betterbaskets.interfaces.Constants;
import com.project.betterbaskets.models.Products;
import com.project.betterbaskets.models.SaleModel;
import com.project.betterbaskets.models.SearchProductsModel;
import com.project.betterbaskets.models.Users;
import com.project.betterbaskets.utilities.SharedPreference;
import com.project.betterbaskets.utilities.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddSaleFrg extends BaseFrg {

    AddSaleLayoutBinding binding;
    DatabaseReference databaseReference;
    Users loggedStore;
    List<SearchProductsModel> productsList;
    List<SearchProductsModel> searchList;

    AutoCompletePlaceAdapter adapter;
    ProductsAdapter productsAdapter;

    private int mYear, mMonth, mDay;
    private Calendar selectedCal;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = AddSaleLayoutBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        databaseReference = initialiseFirebase();
        loggedStore= SharedPreference.getLoggedStore();
        productsList=new ArrayList<>();
        searchList = new ArrayList<>();

        productsAdapter = new ProductsAdapter(getActivity(), productsList);
        binding.mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.mRecyclerView.setAdapter(productsAdapter);

        showStoreProducts(loggedStore.getId());

        binding.starDateRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.hideKeyboard(getActivity());
                // Get Current Date
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);

                final DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    String fmonth, fDate;
                    int month;

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                        try {
                            if (monthOfYear < 10 && dayOfMonth < 10) {

                                fmonth = "0" + monthOfYear;
                                month = Integer.parseInt(fmonth) + 1;
                                fDate = "0" + dayOfMonth;
                                String paddedMonth = String.format("%02d", month);
                                binding.mStartDateTv.setText(year +"/" + paddedMonth + "/" + fDate);

                            } else {

                                fmonth = "0" + monthOfYear;
                                month = Integer.parseInt(fmonth) + 1;
                                String paddedMonth = String.format("%02d", month);
                                binding.mStartDateTv.setText(year +"/"+ paddedMonth + "/" + dayOfMonth);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                        // mDateTv.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
                        selectedCal = Calendar.getInstance();
                        selectedCal.set(Calendar.YEAR, year);
                        selectedCal.set(Calendar.MONTH, monthOfYear);
                        selectedCal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    }
                }, mYear, mMonth, mDay);
                datePickerDialog.getDatePicker().setMinDate(c.getTimeInMillis());
                datePickerDialog.show();
            }
        });


        binding.endDateRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.hideKeyboard(getActivity());
                // Get Current Date
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);

                final DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    String fmonth, fDate;
                    int month;

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                        try {
                            if (monthOfYear < 10 && dayOfMonth < 10) {

                                fmonth = "0" + monthOfYear;
                                month = Integer.parseInt(fmonth) + 1;
                                fDate = "0" + dayOfMonth;
                                String paddedMonth = String.format("%02d", month);
                                binding.mEndDateTv.setText(year +"/" + paddedMonth + "/" + fDate);

                            } else {

                                fmonth = "0" + monthOfYear;
                                month = Integer.parseInt(fmonth) + 1;
                                String paddedMonth = String.format("%02d", month);
                                binding.mEndDateTv.setText(year +"/"+ paddedMonth + "/" + dayOfMonth );
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                        // mDateTv.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
                        selectedCal = Calendar.getInstance();
                        selectedCal.set(Calendar.YEAR, year);
                        selectedCal.set(Calendar.MONTH, monthOfYear);
                        selectedCal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    }
                }, mYear, mMonth, mDay);
                datePickerDialog.getDatePicker().setMinDate(c.getTimeInMillis());
                datePickerDialog.show();
            }
        });


        binding.mAddProductsToSaleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("ProductList",productsList.size()+"");
                for(int i=0;i<productsList.size();i++){
                    if(!productsList.get(i).isDataAdded()){
                        Toast.makeText(getActivity(), "Some items are not filled or you have not clicked on green tick", Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                if (productsList.size() == 0) {
                    Toast.makeText(getActivity(), "Please add products for sale", Toast.LENGTH_SHORT).show();
                }else if(binding.mTitleEt.getText().toString().trim().isEmpty()){
                    Toast.makeText(getActivity(), "Enter sale title", Toast.LENGTH_SHORT).show();
                }
                else if(binding.mStartDateTv.getText().toString().trim().isEmpty()){
                    Toast.makeText(getActivity(), "Enter sale start date", Toast.LENGTH_SHORT).show();
                }else if(binding.mEndDateTv.getText().toString().trim().isEmpty()){
                    Toast.makeText(getActivity(), "Enter sale end date", Toast.LENGTH_SHORT).show();
                }else if(binding.mDescriptionEt.getText().toString().trim().isEmpty()){
                    Toast.makeText(getActivity(), "Enter sale description", Toast.LENGTH_SHORT).show();
                }
                else {
                    addSale(productsList);

                }
            }
        });
    }

    private void addSale(List<SearchProductsModel> productsList) {

        showProgressing(getActivity());
        DatabaseReference productsRef = databaseReference.child(Constants.REF_STORE_SALES);
        DatabaseReference newProdRef = productsRef.push();

        String uid=newProdRef.getKey();
        newProdRef.setValue(new SaleModel(uid,loggedStore.getId(),loggedStore.getName(),binding.mTitleEt.getText().toString().trim(),binding.mDescriptionEt.getText().toString().trim(),binding.mStartDateTv.getText().toString().trim()
                ,binding.mEndDateTv.getText().toString().trim(), productsList), new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if(error==null){
                    hideProgressing();
                    Toast.makeText(getActivity(), "Sale saved successfully", Toast.LENGTH_LONG).show();


                }else{
                    hideProgressing();
                    Toast.makeText(getActivity(), "Error in saving sale "+error.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        });


    }

    private void showStoreProducts(String loogedStoreId) {
        showProgressing(getActivity());
        databaseReference.child(Constants.REF_STORE_PRODUCTS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                searchList.clear();
                hideProgressing();
                for (DataSnapshot s: snapshot.getChildren()) {
                    SearchProductsModel products = s.getValue(SearchProductsModel.class);
                    if(products.getStoreId().equalsIgnoreCase(loogedStoreId)){
                        searchList.add(products);
                    }

                }

                Log.e("Test",searchList.size()+"");
                if(searchList.size()==0){
                    Toast.makeText(getActivity(), "No products found", Toast.LENGTH_SHORT).show();
                }else{
                    setAdapter(searchList);
                }



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }


    private void setAdapter(List<SearchProductsModel> productsList) {
        adapter = new AutoCompletePlaceAdapter(getActivity(), productsList);
        binding.mProductsSearchEt.setAdapter(adapter);

    }





    public class AutoCompletePlaceAdapter extends ArrayAdapter<SearchProductsModel> {
        private List<SearchProductsModel> allPlacesList;
        private List<SearchProductsModel> filteredPlacesList;

        public AutoCompletePlaceAdapter(@NonNull Context context, @NonNull List<SearchProductsModel> placesList) {
            super(context, 0, placesList);
            allPlacesList = new ArrayList<>(placesList);
        }

        @NonNull
        @Override
        public Filter getFilter() {
            return placeFilter;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.autocomplete_item_place, parent, false);
            }

            ImageView placeImage = convertView.findViewById(R.id.mImage);
            TextView placeLabel = convertView.findViewById(R.id.mProductNameTv);

            SearchProductsModel place = getItem(position);
            if (place != null) {
                placeLabel.setText(place.getName());
               // Glide.with(convertView).load(AppUrl.PRODUCT_IMAGES_URL + place.getImageURL()).into(placeImage);
            }

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(productsList.size()>0){
                        for(int i=0;i<productsList.size();i++){
                            if(place.getName().equalsIgnoreCase(productsList.get(i).getName())){
                                Toast.makeText(getActivity(), "This product is already added. Please select another products", Toast.LENGTH_SHORT).show();
                            }else{
                                productsList.add(place);
                                productsAdapter.notifyDataSetChanged();
                            }
                        }

                    }else{
                        productsList.add(place);
                        productsAdapter.notifyDataSetChanged();
                    }

                }
            });

            return convertView;
        }

        private Filter placeFilter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();

                filteredPlacesList = new ArrayList<>();

                if (constraint == null || constraint.length() == 0) {
                    filteredPlacesList.addAll(allPlacesList);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();
                    for (SearchProductsModel place : allPlacesList) {
                        if (place.getName().toLowerCase().contains(filterPattern)) {
                            filteredPlacesList.add(place);
                        }
                    }
                }

                results.values = filteredPlacesList;
                results.count = filteredPlacesList.size();

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                clear();
                addAll((List) results.values);
                notifyDataSetChanged();
            }

            @Override
            public CharSequence convertResultToString(Object resultValue) {
                return ((SearchProductsModel) resultValue).getName();
            }
        };
    }




    //--------------------------------------Products Adapter-----------------------------------------
    public class ProductsAdapter extends RecyclerView.Adapter<ProductsAdapter.MyViewHolder> {

        Context context;
        List<SearchProductsModel> childFeedList;

        public ProductsAdapter(Context context, List<SearchProductsModel> childFeedList) {
            this.context = context;
            this.childFeedList = childFeedList;

        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.filtered_products_design, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, @SuppressLint("RecyclerView") int position) {

            SearchProductsModel childFeedsModel = childFeedList.get(position);
            holder.mProductNameTv.setText(childFeedsModel.getName());

            //Glide.with(getActivity()).load(AppUrl.PRODUCT_IMAGES_URL + childFeedsModel.getImageURL()).into(holder.mImage);

            holder.mDeleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    childFeedList.remove(childFeedsModel);
                    productsAdapter.notifyItemRemoved(position);
                }
            });

            holder.mTickbBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.mUnitEt.getText().toString().trim().isEmpty() || holder.mPriceEt.getText().toString().isEmpty()) {
                        Toast.makeText(getActivity(), "Please add item quantity and price", Toast.LENGTH_SHORT).show();
                    } else {
                        childFeedsModel.setDataAdded(true);
                        childFeedsModel.setUnitSale(holder.mUnitEt.getText().toString().trim());
                        childFeedsModel.setSalePrice(holder.mPriceEt.getText().toString().trim());
                        Toast.makeText(getActivity(), "Item added successfully.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }


        @Override
        public int getItemCount() {
            return childFeedList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            TextView mProductNameTv;
            ImageView mImage;
            ImageButton mTickbBtn, mDeleteBtn;
            EditText mPriceEt, mUnitEt;

            public MyViewHolder(View itemView) {
                super(itemView);
                mProductNameTv = itemView.findViewById(R.id.mProductNameTv);
                mImage = itemView.findViewById(R.id.mImage);
                mTickbBtn = itemView.findViewById(R.id.mTickBtn);
                mDeleteBtn = itemView.findViewById(R.id.mDeleteBtn);
                mUnitEt = itemView.findViewById(R.id.mUnitEt);
                mPriceEt = itemView.findViewById(R.id.mPriceEt);


            }
        }
    }
}
