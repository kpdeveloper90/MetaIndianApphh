package com.ecosense.app.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationItem implements Serializable {


    @JsonProperty("name")
    private String notificationID;

    @JsonProperty("nftdoctypename")
    private String nftdoctypename;

    @JsonProperty("nftdesc")
    private String nftdesc;

    @JsonProperty("nftstatus")
    private String nftstatus;

    @JsonProperty("nftdatetime")
    private String nftdatetime;

    public String getNotificationID() {
        return notificationID;
    }

    public void setNotificationID(String notificationID) {
        this.notificationID = notificationID;
    }

    public String getNftdoctypename() {
        return nftdoctypename;
    }

    public void setNftdoctypename(String nftdoctypename) {
        this.nftdoctypename = nftdoctypename;
    }

    public String getNftdesc() {
        return nftdesc;
    }

    public void setNftdesc(String nftdesc) {
        this.nftdesc = nftdesc;
    }

    public String getNftstatus() {
        return nftstatus;
    }

    public void setNftstatus(String nftstatus) {
        this.nftstatus = nftstatus;
    }

    public String getNftdatetime() {
        return nftdatetime;
    }

    public void setNftdatetime(String nftdatetime) {
        this.nftdatetime = nftdatetime;
    }
}
