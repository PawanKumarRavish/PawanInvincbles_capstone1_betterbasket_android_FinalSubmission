package com.invicibles.betterbaskets.userfragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.invicibles.betterbaskets.BaseFrg;
import com.invicibles.betterbaskets.R;
import com.invicibles.betterbaskets.databinding.UserHomeFrgBinding;
import com.invicibles.betterbaskets.interfaces.Constants;
import com.invicibles.betterbaskets.models.Users;
import com.invicibles.betterbaskets.storeFragments.StoreSalesFrg;
import com.invicibles.betterbaskets.utilities.SharedPreference;
import com.invicibles.betterbaskets.utilities.Utils;

import java.util.ArrayList;
import java.util.List;

public class UserHomeFrg extends BaseFrg implements OnMapReadyCallback {

    UserHomeFrgBinding binding;
    DatabaseReference databaseReference;
    Users loggedStore;
    List<Users> storeList;

    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int REQUEST_CODE = 101;
    GoogleMap mMap;
    double range=30.0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = UserHomeFrgBinding.inflate(inflater,container,false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        databaseReference = Utils.initialiseFirebase();
        loggedStore= SharedPreference.getLoggedStore();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        fetchLocation();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.myMap);
        mapFragment.getMapAsync(this);

        storeList=new ArrayList<>();
        showNearbyStoresUpto30Km();

        binding.mSelectedRangeTv.setText("Selected Range: 30 km");


        binding.simpleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                binding.mSelectedRangeTv.setText("Selected Range: "+progressChangedValue+" km");
                range=Double.parseDouble(String.valueOf(progressChangedValue));
                //Toast.makeText(getActivity(), "Seek bar progress is :" + progressChangedValue+" Km", Toast.LENGTH_SHORT).show();
            }
        });

        binding.mApplyTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("Range",range+"");
                showNearbyStoresUpto30Km();

            }
        });
    }

    private void showNearbyStoresUpto30Km() {
        showProgressing(getActivity());
        databaseReference.child(Constants.REF_USERS).child(Constants.REF_STORES).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                storeList.clear();
                hideProgressing();
                for (DataSnapshot s: snapshot.getChildren()) {
                    Users users = s.getValue(Users.class);
                    double distanceInMeters = Utils.calculateDistance(Double.parseDouble(SharedPreference.getLat()), Double.parseDouble(SharedPreference.getLng()), Double.parseDouble(users.getLat()),
                            Double.parseDouble(users.getLng()));
                    double distanceInKm=distanceInMeters/1000;
                    Log.e("km",distanceInKm+"");
                    if(distanceInKm<=range){
                        storeList.add(users);
                    }


                }

                Log.e("Test",storeList.size()+"");

                setMarkers(storeList);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });


    }

    private void setMarkers(List<Users> storeList) {
        mMap.clear();
        LatLng currentUserLatLng = new LatLng(Double.parseDouble(SharedPreference.getLat()), Double.parseDouble(SharedPreference.getLng()));
        //LatLng latLng = new LatLng(43.653225, -79.383186);
        MarkerOptions markerOptions = new MarkerOptions().position(currentUserLatLng).title("I am here!").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        mMap.animateCamera(CameraUpdateFactory.newLatLng(currentUserLatLng));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentUserLatLng, 12));
        mMap.addMarker(markerOptions);

        for(int i=0;i<storeList.size();i++){
            LatLng latLng = new LatLng(Double.parseDouble(storeList.get(i).getLat()), Double.parseDouble(storeList.get(i).getLng()));
            Marker marker = mMap.addMarker(new MarkerOptions().position(latLng).title(storeList.get(i).getName()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
            marker.setTag(storeList.get(i));

        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng latLng = new LatLng(Double.parseDouble(SharedPreference.getLat()), Double.parseDouble(SharedPreference.getLng()));
        //LatLng latLng = new LatLng(43.653225, -79.383186);
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("I am here!").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
        mMap.addMarker(markerOptions);


        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                LatLng latLon = marker.getPosition();
                //Cycle through places array
                for(Users place : storeList){
                    LatLng newLatLng=new LatLng(Double.parseDouble(place.getLat()),Double.parseDouble(place.getLng()));
                    if (latLon.equals(newLatLng)){
                        Toast.makeText(getActivity(), place.getName(), Toast.LENGTH_SHORT).show();
                        Bundle bundle=new Bundle();
                        bundle.putString(Constants.STORE_ID,place.getId());
                        bundle.putString(Constants.TYPE,Constants.TYPE_CUSTOMER);
                        StoreSalesFrg storeSalesFrg=new StoreSalesFrg();
                        storeSalesFrg.setArguments(bundle);
                        Utils.doFragmentTransition(R.id.mFrameLl,storeSalesFrg,getActivity().getSupportFragmentManager(),true);

                    }

                }
            }
        });

    }


    private void fetchLocation() {
        if (ActivityCompat.checkSelfPermission(
                getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                    Toast.makeText(getActivity(), currentLocation.getLatitude() + "" + currentLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                    SharedPreference.setLat(String.valueOf(currentLocation.getLatitude()));
                    SharedPreference.setLng(String.valueOf(currentLocation.getLongitude()));

                }
            }
        });
    }
}
