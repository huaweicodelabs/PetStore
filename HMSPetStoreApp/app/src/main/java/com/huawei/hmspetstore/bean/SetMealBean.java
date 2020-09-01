package com.huawei.hmspetstore.bean;

import com.huawei.hms.iap.entity.ProductInfo;

public class SetMealBean {

    private String name;
    private String desc;
    private String money;
    private ProductInfo productInfo;

    public SetMealBean(ProductInfo productInfo) {
        this.name = productInfo.getProductName();
        this.desc = productInfo.getProductDesc();
        this.money = productInfo.getPrice();
        this.productInfo = productInfo;
    }

    public ProductInfo getProductInfo() {
        return productInfo;
    }

    public void setProductInfo(ProductInfo productInfo) {
        this.productInfo = productInfo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getMoney() {
        return money;
    }

    public void setMoney(String money) {
        this.money = money;
    }
}