package com.huawei.hms.petstore.beans.push;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Recepit {
    @JsonProperty("errno")
    private String errno;

    @JsonProperty("errmsg")
    private String errmsg;

    public void setErrno(String errno) {
        this.errno = errno;
    }

    public String getErrno() {
        return errno;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

    public String getErrmsg() {
        return errmsg;
    }
}
