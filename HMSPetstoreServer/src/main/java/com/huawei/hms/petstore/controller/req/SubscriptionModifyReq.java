package com.huawei.hms.petstore.controller.req;

public class SubscriptionModifyReq {
    private String subscriptionId;

    private String purchaseToken;

    private Long currentExpirationTime;

    private Long desiredExpirationTime;

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getPurchaseToken() {
        return purchaseToken;
    }

    public void setPurchaseToken(String purchaseToken) {
        this.purchaseToken = purchaseToken;
    }

    public Long getCurrentExpirationTime() {
        return currentExpirationTime;
    }

    public void setCurrentExpirationTime(Long currentExpirationTime) {
        this.currentExpirationTime = currentExpirationTime;
    }

    public Long getDesiredExpirationTime() {
        return desiredExpirationTime;
    }

    public void setDesiredExpirationTime(Long desiredExpirationTime) {
        this.desiredExpirationTime = desiredExpirationTime;
    }
}
