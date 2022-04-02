package com.invicibles.betterbaskets.models;

// made by pawan and rishabh bhangu

public class Products {

    String uid;
    String name;
    String storeId;
    String downloadUrl;

    public Products() {
    }

    public Products(String uid, String name,String storeId,String downloadUrl) {
        this.uid = uid;
        this.name = name;
        this.storeId = storeId;
        this.downloadUrl = downloadUrl;
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

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
}
