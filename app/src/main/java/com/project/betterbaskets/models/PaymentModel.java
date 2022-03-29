package com.project.betterbaskets.models;

public class PaymentModel {
    String uid;
    String paymentId;
    SaleModel saleModel;

    public PaymentModel() {
    }

    public PaymentModel(String uid, String paymentId, SaleModel saleModel) {
        this.uid = uid;
        this.paymentId = paymentId;
        this.saleModel = saleModel;
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
}
