package com.project.betterbaskets.models;

import com.project.betterbaskets.interfaces.Constants;

public class Users {
    String id;
    String name;
    String phone;
    String password;
    String type;
    String profilePic;
    String address;
    String lat;
    String lng;

    public Users(){

    }

    public Users(String id,String name, String phone, String password, String type, String profilePic, String address, String lat, String lng) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.password = password;
        this.type = type;
        this.profilePic = profilePic;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
