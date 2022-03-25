package com.project.betterbaskets.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Toast;

import com.project.betterbaskets.R;
import com.project.betterbaskets.databinding.ActivityRegisterBinding;
import com.project.betterbaskets.interfaces.Constants;
import com.project.betterbaskets.utilities.SharedPreference;
import com.project.betterbaskets.utilities.Utils;

import java.io.ByteArrayOutputStream;
import java.io.Console;
import java.io.FileDescriptor;
import java.io.IOException;

public class RegisterActivity extends BaseActivity {

    ActivityRegisterBinding binding;
    Dialog alertDialog;
    Uri image_uri;
    Bitmap resizedBitmap=null;
    String encodedTaxImage = "";
    String type= Constants.TYPE_CUSTOMER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        binding.mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
               finish();
            }
        });

        binding.mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validateFields()){
                    sendData();
                }
            }
        });

        binding.mProfileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showFileChooser();

            }
        });

        binding.customerCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if(checked){
                    type=Constants.TYPE_CUSTOMER;
                    binding.storeCb.setChecked(false);
                    binding.customerCb.setChecked(true);
                }

            }
        });

        binding.storeCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if(checked){
                    type=Constants.TYPE_STORE;
                    binding.customerCb.setChecked(false);
                    binding.storeCb.setChecked(true);
                }

            }
        });

        binding.currentPositionIMg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(RegisterActivity.this,AddressActivity.class),3);
            }
        });

        binding.addressEt.setText(Utils.getCompleteAddressString(RegisterActivity.this,Double.parseDouble(SharedPreference.getLat()),Double.parseDouble(SharedPreference.getLng())));
    }

    private void sendData() {
        Intent intent=new Intent(RegisterActivity.this, OTPVerificationActivity.class);
        intent.putExtra(Constants.NAME,binding.mNameEt.getText().toString().trim());
        intent.putExtra(Constants.MOBILE,binding.mMobileNumberEt.getText().toString().trim());
        intent.putExtra(Constants.PASSWORD,binding.mPasswordEt.getText().toString().trim());
        intent.putExtra(Constants.TYPE,type);

        intent.putExtra(Constants.LAT, SharedPreference.getLat());
        intent.putExtra(Constants.LNG, SharedPreference.getLng());
        intent.putExtra(Constants.PROFILE_IMG, "data:image/png;base64,"+encodedTaxImage);
        intent.putExtra(Constants.ADDRESS, binding.addressEt.getText().toString().trim());

        startActivity(intent);

    }


    private void showFileChooser() {
        View view = getLayoutInflater().inflate(R.layout.camera_dialog, null);

        Button okBtn = (Button) view.findViewById(R.id.btnOk);
        RadioButton mRbCamera = (RadioButton) view.findViewById(R.id.rbCamera);
        RadioButton mRbGallery = (RadioButton) view.findViewById(R.id.rbGallery);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRbCamera.isChecked())
                {
                    alertDialog.dismiss();
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.TITLE, "New Picture");
                    values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera");
                    image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
                    startActivityForResult(cameraIntent, 1);
                    return;
                }
                if (mRbGallery.isChecked())
                {
                    alertDialog.dismiss();
                    Intent intent = new   Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 2);
                    return;
                }
                Toast.makeText(RegisterActivity.this, "Please select any one upload type", Toast.LENGTH_SHORT).show();

            }
        });


        alertDialog = new Dialog(RegisterActivity.this);
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE); //this line MUST BE BEFORE setContentView
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.setCancelable(true);
        alertDialog.setContentView(view);
        alertDialog.show();
        Window window = alertDialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }


    //TODO takes URI of the image and returns bitmap
    private Bitmap uriToBitmap(Uri selectedFileUri) {
        try {
            ParcelFileDescriptor parcelFileDescriptor =
                    getContentResolver().openFileDescriptor(selectedFileUri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);

            parcelFileDescriptor.close();
            return image;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  null;
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }


    public String convertImageToBase64(Bitmap resizedBitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 40, baos);
        try {
            byte[] imageBytes = baos.toByteArray();
            long lengthbmp = imageBytes.length;
            Log.e("Hello",lengthbmp+"");
            encodedTaxImage = Base64.encodeToString(imageBytes, Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encodedTaxImage;

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            if (requestCode == 1) {
                Uri uri = this.image_uri;
                Log.e("Uri",uri+"");
                Bitmap bitmap = uriToBitmap(uri);
                resizedBitmap = getResizedBitmap(bitmap, 500);
                binding.mProfileImg.setImageBitmap(resizedBitmap);

                convertImageToBase64(resizedBitmap);



            } else if (requestCode == 2) {
                Uri selectedImage = data.getData();

                Bitmap bitmap = uriToBitmap(selectedImage);
                resizedBitmap = getResizedBitmap(bitmap, 500);
                binding.mProfileImg.setImageBitmap(resizedBitmap);

                convertImageToBase64(resizedBitmap);
            }
            else if (requestCode == 3) {
                String lat = data.getStringExtra("lat");
                String lng = data.getStringExtra("lng");
                binding.addressEt.setText(Utils.getCompleteAddressString(RegisterActivity.this,Double.parseDouble(lat),Double.parseDouble(lng)));
            }

        }
    }

    private boolean validateFields() {
        Utils.hideKeyboard(RegisterActivity.this);
        if (binding.mMobileNumberEt.getText().toString().trim().isEmpty()) {
            Toast.makeText(RegisterActivity.this, getString(R.string.enter_phone), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (binding.mMobileNumberEt.getText().toString().length() < 10) {
            Toast.makeText(RegisterActivity.this, getString(R.string.enter_valid_phone), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (binding.mNameEt.getText().toString().trim().isEmpty()) {
            Toast.makeText(RegisterActivity.this, getString(R.string.enter_name), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (binding.mPasswordEt.getText().toString().trim().isEmpty()) {
            Toast.makeText(RegisterActivity.this, getString(R.string.enter_password), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (binding.mPasswordEt.getText().toString().length() < 6) {
            Toast.makeText(RegisterActivity.this, getString(R.string.password_not_less_than_6), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (encodedTaxImage.equalsIgnoreCase("")) {
            Toast.makeText(RegisterActivity.this, getString(R.string.add_image), Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;

    }
}