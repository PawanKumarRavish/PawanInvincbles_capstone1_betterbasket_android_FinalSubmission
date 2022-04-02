package com.invicibles.betterbaskets.userfragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.invicibles.betterbaskets.BaseFrg;
import com.invicibles.betterbaskets.R;
import com.invicibles.betterbaskets.activities.CustomerHomeActivity;
import com.invicibles.betterbaskets.activities.RegisterActivity;
import com.invicibles.betterbaskets.databinding.UserLoginFrgBinding;
import com.invicibles.betterbaskets.interfaces.Constants;
import com.invicibles.betterbaskets.models.Users;
import com.invicibles.betterbaskets.utilities.SharedPreference;
import com.invicibles.betterbaskets.utilities.Utils;

import java.util.ArrayList;
import java.util.List;

public class UserLoginFrg extends BaseFrg {

    UserLoginFrgBinding binding;

    DatabaseReference databaseReference;
    List<Users> userList;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = UserLoginFrgBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        databaseReference = Utils.initialiseFirebase();
        userList=new ArrayList<>();


        binding.mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validateFields()){
                    doLogin();
                }


            }
        });

        binding.mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getActivity(),RegisterActivity.class);
                intent.putExtra(Constants.TYPE,Constants.TYPE_CUSTOMER);
                startActivity(intent);
            }
        });
    }

    private void doLogin() {
        showProgressing(getActivity());
        databaseReference.child(Constants.REF_USERS).child(Constants.REF_CUSTOMERS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot s: snapshot.getChildren()) {
                    Users users = s.getValue(Users.class);
                    userList.add(users);
                }

                if(isUserAvailable(userList)){
                   hideProgressing();
                    SharedPreference.setUserLogin();
                   startActivity(new Intent(getActivity(), CustomerHomeActivity.class));
                   getActivity().finish();
                }else{
                    hideProgressing();
                    Toast.makeText(getActivity(), "Invalid Credentials", Toast.LENGTH_SHORT).show();
                }

                Log.e("Test",userList+"");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    public boolean isUserAvailable(List<Users> userList){
        boolean isStoreAvailable=false;
        for(int i =0; i<userList.size();i++){
            if(userList.get(i).getType().equalsIgnoreCase(Constants.TYPE_CUSTOMER)
                    && userList.get(i).getPhone().equalsIgnoreCase(binding.mMobileNumberEt.getText().toString().trim())
                    && userList.get(i).getPassword().equalsIgnoreCase(binding.mPasswordEt.getText().toString().trim())){
                isStoreAvailable=true;

                SharedPreference.setLoggedStore(new Users(userList.get(i).getId(),userList.get(i).getName(),userList.get(i).getPhone(),
                        userList.get(i).getPassword(),userList.get(i).getType(),userList.get(i).getProfilePic(),
                        userList.get(i).getAddress(),userList.get(i).getLat(),userList.get(i).getLng()));

            }
        }

        return isStoreAvailable;

    }


    private boolean validateFields() {
        Utils.hideKeyboard(getActivity());
        if (binding.mMobileNumberEt.getText().toString().trim().isEmpty()) {
            Toast.makeText(getActivity(), getString(R.string.enter_phone), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (binding.mMobileNumberEt.getText().toString().length() < 10) {
            Toast.makeText(getActivity(), getString(R.string.enter_valid_phone), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (binding.mPasswordEt.getText().toString().trim().isEmpty()) {
            Toast.makeText(getActivity(), getString(R.string.enter_password), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (binding.mPasswordEt.getText().toString().length() < 6) {
            Toast.makeText(getActivity(), getString(R.string.password_not_less_than_6), Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;

    }
}
