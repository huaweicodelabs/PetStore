package com.huawei.hms.petstore.controller.req;

public class OrderVerifyReq {
    private String purchaseToken;

    private String productId;

    public String getPurchaseToken() {
        return purchaseToken;
    }

    public void setPurchaseToken(String purchaseToken) {
        this.purchaseToken = purchaseToken;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }
}
