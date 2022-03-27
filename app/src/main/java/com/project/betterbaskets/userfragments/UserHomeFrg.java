package com.project.betterbaskets.userfragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.project.betterbaskets.BaseFrg;
import com.project.betterbaskets.R;
import com.project.betterbaskets.databinding.UserHomeFrgBinding;
import com.project.betterbaskets.interfaces.Constants;
import com.project.betterbaskets.models.Products;
import com.project.betterbaskets.models.Users;
import com.project.betterbaskets.utilities.SharedPreference;
import com.project.betterbaskets.utilities.Utils;

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

                setMarkers(storeList);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });


    }

    private void setMarkers(List<Users> storeList) {
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
                        /*Bundle bundle=new Bundle();
                        bundle.putString(Constants.STORE_ID,place.getStoreid());
                        StoreDetailsFrg storeDetailsFrg=new StoreDetailsFrg();
                        storeDetailsFrg.setArguments(bundle);
                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.mFrameLl, storeDetailsFrg).addToBackStack(null).commit();*/
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
