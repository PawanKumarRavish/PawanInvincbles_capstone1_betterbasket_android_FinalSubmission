package com.project.betterbaskets.models;


// made by Rishabh Bhangu Up wala
public class SearchProductsModel {

    String uid;
    String name;
    String storeId;
    boolean isDataAdded;
    String unitSale ;
    String salePrice ;

    public SearchProductsModel() {
    }

    public SearchProductsModel(String uid, String name,String storeId,boolean isDataAdded,String unitSale,String salePrice) {
        this.uid = uid;
        this.name = name;
        this.storeId = storeId;
        this.isDataAdded = isDataAdded;
        this.unitSale = unitSale;
        this.salePrice = salePrice;
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

    public boolean isDataAdded() {
        return isDataAdded;
    }

    public void setDataAdded(boolean dataAdded) {
        isDataAdded = dataAdded;
    }


    public String getUnitSale() {
        return unitSale;
    }

    public void setUnitSale(String unitSale) {
        this.unitSale = unitSale;
    }

    public String getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(String salePrice) {
        this.salePrice = salePrice;
    }
}
