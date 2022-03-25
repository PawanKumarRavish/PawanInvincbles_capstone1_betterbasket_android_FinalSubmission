package com.project.betterbaskets.activities;

import static com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
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
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.project.betterbaskets.R;
import com.project.betterbaskets.databinding.ActivityAddressBinding;
import com.project.betterbaskets.utilities.SharedPreference;
import com.project.betterbaskets.utilities.Utils;

import java.util.Arrays;
import java.util.List;

public class AddressActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    ;
    ActivityAddressBinding binding;
    Double lat, lng;
    FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(AddressActivity.this);


        binding = ActivityAddressBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.myMap);
        mapFragment.getMapAsync(this);

        binding.addressEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME);
                //Create Intent
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList).build(AddressActivity.this);
                startActivityForResult(intent, 100);

            }
        });


        binding.currentPositionIMg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //fetchLocation();

                mMap.clear();


                if (ActivityCompat.checkSelfPermission(AddressActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(AddressActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                fusedLocationClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY, new CancellationToken() {
                    @Override
                    public boolean isCancellationRequested() {
                        return false;
                    }

                    @NonNull
                    @Override
                    public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                        return null;
                    }
                }).addOnSuccessListener(location -> {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        Log.e("Location ", "-->" + location.getLatitude());
                        lat = location.getLatitude();
                        lng =location.getLongitude();

                        SharedPreference.setLat(String.valueOf(lat));
                        SharedPreference.setLng(String.valueOf(lng));

                        LatLng latLng = new LatLng(Double.parseDouble(SharedPreference.getLat()), Double.parseDouble(SharedPreference.getLng()));
                        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("I am here!");
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5));
                        mMap.addMarker(markerOptions);

                        binding.addressEt.invalidate();
                        binding.addressEt.setText(Utils.getCompleteAddressString(AddressActivity.this,Double.parseDouble(SharedPreference.getLat()), Double.parseDouble(SharedPreference.getLng())));

                    }
                });




            }
        });
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        LatLng latLng = new LatLng(Double.parseDouble(SharedPreference.getLat()), Double.parseDouble(SharedPreference.getLng()));
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("I am here!");
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5));
        googleMap.addMarker(markerOptions);

        binding.addressEt.invalidate();
        binding.addressEt.setText(Utils.getCompleteAddressString(AddressActivity.this,Double.parseDouble(SharedPreference.getLat()), Double.parseDouble(SharedPreference.getLng())));

        //Initialise Places Api
        Places.initialize(AddressActivity.this, getString(R.string.google_maps_key));
        binding.addressEt.setFocusable(false);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            //initialise place
            Place place = Autocomplete.getPlaceFromIntent(data);
            binding.addressEt.invalidate();
            binding.addressEt.setText(place.getAddress());
            LatLng queriedLocation = place.getLatLng();
            Double lat = queriedLocation.latitude;
            Double lng = queriedLocation.longitude;
            Log.v("Latitude is", "" + queriedLocation.latitude);
            Log.v("Longitude is", "" + queriedLocation.longitude);


            SharedPreference.setLat(String.valueOf(lat));
            SharedPreference.setLng(String.valueOf(lng));

            mMap.clear();
            MarkerOptions markerOptions = new MarkerOptions().position(place.getLatLng()).title(place.getAddress()).draggable(false);
            mMap.animateCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15));
            mMap.addMarker(markerOptions);

            //saveToFavourites(place.getAddress(),lat,lng);


        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            //initialise status
            Status status = Autocomplete.getStatusFromIntent(data);
            Toast.makeText(AddressActivity.this, status.getStatusMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = getIntent();
        intent.putExtra("lat", SharedPreference.getLat());
        intent.putExtra("lng", SharedPreference.getLng());
        setResult(RESULT_OK, intent);
        finish();
    }
}