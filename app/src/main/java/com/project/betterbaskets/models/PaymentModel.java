package com.project.betterbaskets.models;

public class PaymentModel {
    String uid;
    String paymentId;
    SaleModel saleModel;
    String userId;
    String totalAmount;

    public PaymentModel() {
    }

    public PaymentModel(String uid, String paymentId, SaleModel saleModel,String userId,String totalAmount) {
        this.uid = uid;
        this.paymentId = paymentId;
        this.saleModel = saleModel;
        this.userId = userId;
        this.totalAmount = totalAmount;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public SaleModel getSaleModel() {
        return saleModel;
    }

    public void setSaleModel(SaleModel saleModel) {
        this.saleModel = saleModel;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }
}
