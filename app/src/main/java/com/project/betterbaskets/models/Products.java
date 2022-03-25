package com.project.betterbaskets.models;

public class Products {

    String uid;
    String name;
    String storeId;

    public Products() {
    }

    public Products(String uid, String name,String storeId) {
        this.uid = uid;
        this.name = name;
        this.storeId = storeId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }
}
