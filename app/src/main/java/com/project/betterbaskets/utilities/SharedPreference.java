package com.project.betterbaskets.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.google.gson.Gson;
import com.project.betterbaskets.interfaces.Constants;
import com.project.betterbaskets.models.Users;


public class SharedPreference {
    public static String My_Prefrences = "better_basket_Preferences";
    static SharedPreferences mPref;
    Editor editor;

    public SharedPreference(Context mContext) {
        mPref = mContext.getSharedPreferences(My_Prefrences, Context.MODE_PRIVATE);
        editor = mPref.edit();
    }

   /* public static UserModel getUserObj() {
        Gson gson = new Gson();
        String json = mPref.getString(Constants.USER_OBJ, "");
        UserModel obj = gson.fromJson(json, UserModel.class);
        return obj;
    }

    public static void setUserObj(UserModel userModel) {

        Editor editor2 = mPref.edit();
        Gson gson = new Gson();
        String json = gson.toJson(userModel);
        editor2.putString(Constants.USER_OBJ, json);
        editor2.apply();
    }*/


    public static String getLat() {
        return mPref.getString(Constants.LAT, "");
    }

    public static void setLat(String lat) {
        Editor editor2 = mPref.edit();
        editor2.putString(Constants.LAT, lat);
        editor2.apply();
    }


    public static String getLng() {
        return mPref.getString(Constants.LNG, "");
    }

    public static void setLng(String lng) {
        Editor editor2 = mPref.edit();
        editor2.putString(Constants.LNG, lng);
        editor2.apply();
    }





    public static void setSalonLogin() {
        Editor editor = mPref.edit();
        editor.putBoolean(Constants.SALON_LOGIN, true);
        editor.apply();
    }

    public static boolean isSalonLogin() {
        return mPref.getBoolean(Constants.SALON_LOGIN,false);
    }

    public static void resetSalonLogin() {
        Editor editor = mPref.edit();
        editor.putBoolean(Constants.SALON_LOGIN, false);
        editor.apply();
    }



    public static void setUserLogin() {
        Editor editor = mPref.edit();
        editor.putBoolean(Constants.USER_LOGIN, true);
        editor.apply();
    }

    public static boolean isUserLogin() {
        return mPref.getBoolean(Constants.USER_LOGIN,false);
    }

    public static void resetUserLogin() {
        Editor editor = mPref.edit();
        editor.putBoolean(Constants.USER_LOGIN, false);
        editor.apply();
    }


    public static Users getLoggedStore() {
        Gson gson = new Gson();
        String json = mPref.getString(Constants.STORE_OBJ, "");
        Users obj = gson.fromJson(json, Users.class);
        return obj;
    }

    public static void setLoggedStore(Users userModel) {

        Editor editor2 = mPref.edit();
        Gson gson = new Gson();
        String json = gson.toJson(userModel);
        editor2.putString(Constants.STORE_OBJ, json);
        editor2.apply();
    }


    public static void removeAllData(){
        Editor editor = mPref.edit();
        editor.remove(Constants.USER_OBJ);
        resetSalonLogin();
        resetUserLogin();
        editor.apply();
        mPref.edit().clear().apply();
    }




}
