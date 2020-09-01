package com.huawei.hmspetstore.bean;

import java.io.Serializable;

public class UserBean implements Serializable {

    private static final long serialVersionUID = 3944082161898269797L;
    private String name;
    private String password;
    private String uuid;

    public UserBean() {
    }

    public UserBean(String name, String password, String uuid) {
        this.name = name;
        this.password = password;
        this.uuid = uuid;
}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}