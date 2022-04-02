package com.invicibles.betterbaskets.models;

import java.util.List;


public class SaleModel {

    String uid;
    String storeId;
    String storeName;
    String saleTitle;
    String description;
    String stockAvailable;
    String saleStartDate;
    String saleEndDate;
    List<SearchProductsModel> productsList;
    String downloadUrl;

    public SaleModel() {
    }

    public SaleModel(String uid, String storeId,String storeName,String saleTitle, String description,String stockAvailable, String saleStartDate, String saleEndDate,List<SearchProductsModel> productsList,String downloadUrl) {
        this.uid = uid;
        this.storeId = storeId;
        this.storeName = storeName;
        this.saleTitle = saleTitle;
        this.description = description;
        this.stockAvailable = stockAvailable;
        this.saleStartDate = saleStartDate;
        this.saleEndDate = saleEndDate;
        this.productsList = productsList;
        this.downloadUrl = downloadUrl;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSaleStartDate() {
        return saleStartDate;
    }

    public void setSaleStartDate(String saleStartDate) {
        this.saleStartDate = saleStartDate;
    }

    public String getSaleEndDate() {
        return saleEndDate;
    }

    public void setSaleEndDate(String saleEndDate) {
        this.saleEndDate = saleEndDate;
    }

    public List<SearchProductsModel> getProductsList() {
        return productsList;
    }

    public void setProductsList(List<SearchProductsModel> productsList) {
        this.productsList = productsList;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getSaleTitle() {
        return saleTitle;
    }

    public void setSaleTitle(String saleTitle) {
        this.saleTitle = saleTitle;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getStockAvailable() {
        return stockAvailable;
    }

    public void setStockAvailable(String stockAvailable) {
        this.stockAvailable = stockAvailable;
    }
}