package com.invicibles.betterbaskets.storeFragments;

import static com.invicibles.betterbaskets.utilities.Utils.initialiseFirebase;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;
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
import java.util.concurrent.ExecutionException;

public class StoreHomeFrg extends BaseFrg /*implements OnChartValueSelectedListener*/ {

    StoreHomeFrgBinding binding;
    Users loggedStore;
    DatabaseReference databaseReference;
   int totalMoneyEarned=0;
   int totalSales=0;
    List<SaleModel> saleList;
    List<PaymentModel> paymentList;

    BarChart barChart;
    BarData barData;
    BarDataSet barDataSet;
    ArrayList barEntries;



    Legend legend;
    PieDataSet dataSet;

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
        paymentList=new ArrayList<>();

        binding.mStoreNameTv.setText("Hi, "+loggedStore.getName());

        getPaymentHistory();

        getStoreSales(loggedStore.getId());



    }


    private void initiateBarChart2(List<PaymentModel> paymentList) {
        try {

            binding.mBarChart.invalidate();
            binding.mBarChart.clear();

            getBarEntries2(paymentList);
            barDataSet = new BarDataSet(barEntries, "Store Sales");
            barData = new BarData(barDataSet);
            binding.mBarChart.setData(barData);
            barDataSet.setColors(ColorTemplate.JOYFUL_COLORS);
            barDataSet.setValueTextColor(Color.BLACK);
            barDataSet.setValueTextSize(18f);

        }catch (Exception e){
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }


    }

    private void getBarEntries2(List<PaymentModel> paymentList) {
        barEntries = new ArrayList<>();
        for(int i=0;i<paymentList.size();i++){

            float amountToShow=0;
            for(int j=0;j<paymentList.get(i).getSaleModel().getProductsList().size();j++){
                amountToShow=amountToShow+Float.parseFloat(paymentList.get(i).getSaleModel().getProductsList().get(j).getSalePrice());
            }

            barEntries.add(new BarEntry(i, amountToShow));

        }


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

    /*private void initiatePieChart(List<PaymentModel> paymentList) {
        try {
            ArrayList<Entry> yvalues = new ArrayList<Entry>();
            ArrayList<String> xVals = new ArrayList<String>();

            for (int i = 0; i < paymentList.size(); i++) {
               // yvalues.add(new Entry(Float.valueOf(saleList.get(i).getAmount()), i));
                float amountToShow=0;
                for(int j=0;j<paymentList.get(i).getSaleModel().getProductsList().size();j++){
                    amountToShow=amountToShow+Float.parseFloat(paymentList.get(i).getSaleModel().getProductsList().get(j).getSalePrice());
                }
                yvalues.add(new Entry(amountToShow, i));
                xVals.add(paymentList.get(i).getStoreId());
            }



            dataSet = new PieDataSet(yvalues, "");
            dataSet.setSliceSpace(3); // adding slice between pie chart
            dataSet.setSelectionShift(5);


            PieData data = new PieData(xVals, dataSet);
                data.setValueFormatter(new PercentFormatter() {
                    @Override
                    public String getFormattedValue(float value, YAxis yAxis) {
                        return mFormat.format(value);
                    }

                    @Override
                    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                        return mFormat.format(value);
                    }
                });
            binding.mBarChart.setData(data);
            binding.mBarChart.setDescription("");

            legend = binding.mBarChart.getLegend();
            int colourCodes[]=legend.getColors();
            legend.setForm(Legend.LegendForm.SQUARE);
            legend.setPosition(Legend.LegendPosition.ABOVE_CHART_RIGHT);
            legend.setXEntrySpace(7);
            legend.setYEntrySpace(5);
            legend.setWordWrapEnabled(true);

            binding.mBarChart.setDrawHoleEnabled(true);
            binding.mBarChart.setTransparentCircleRadius(25f);
            binding.mBarChart.setHoleRadius(65f);

            dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
            data.setValueTextSize(10f);
            binding.mBarChart.animateXY(1400, 1400);
            data.setValueTextColor(Color.DKGRAY);

            data.setDrawValues(false);  // removing x values
            binding.mBarChart.setDrawSliceText(false); // removing y values
            binding.mBarChart.invalidate();
        } catch (Exception e) {
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }*/

    private void getPaymentHistory() {
        showProgressing(getActivity());
        databaseReference.child(Constants.REF_PAYMENTS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                hideProgressing();
                paymentList.clear();
                for (DataSnapshot s: snapshot.getChildren()) {
                    PaymentModel users = s.getValue(PaymentModel.class);
                    if(loggedStore.getId().equalsIgnoreCase(users.getStoreId())){
                        paymentList.add(users);
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

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                //initiatePieChart(paymentList);
                initiateBarChart2(paymentList);
            }
        }, 1000);




    }

}
