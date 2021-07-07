package com.ecosense.app.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserActivity implements Serializable {

    @JsonProperty("name")
    private String userActionId;

    @JsonProperty("uautype")
    private String uautype;

   @JsonProperty("uauname")
    private String uauname;

   @JsonProperty("uaarticalid")
    private String uaarticalid;


   @JsonProperty("uaplatform")
    private String uaplatform;

   @JsonProperty("uaacttype")
    private String uaacttype;


   @JsonProperty("uaartcltype")
    private String uaartcltype;

   @JsonProperty("uauserid")
    private String uauserid;

   @JsonProperty("uaadate")
    private String uaadate;

    public String getUaplatform() {
        return uaplatform;
    }

    public void setUaplatform(String uaplatform) {
        this.uaplatform = uaplatform;
    }

    public String getUserActionId() {
        return userActionId;
    }

    public void setUserActionId(String userActionId) {
        this.userActionId = userActionId;
    }

    public String getUautype() {
        return uautype;
    }

    public void setUautype(String uautype) {
        this.uautype = uautype;
    }

    public String getUauname() {
        return uauname;
    }

    public void setUauname(String uauname) {
        this.uauname = uauname;
    }

    public String getUaarticalid() {
        return uaarticalid;
    }

    public void setUaarticalid(String uaarticalid) {
        this.uaarticalid = uaarticalid;
    }

    public String getUaacttype() {
        return uaacttype;
    }

    public void setUaacttype(String uaacttype) {
        this.uaacttype = uaacttype;
    }

    public String getUaartcltype() {
        return uaartcltype;
    }

    public void setUaartcltype(String uaartcltype) {
        this.uaartcltype = uaartcltype;
    }

    public String getUauserid() {
        return uauserid;
    }

    public void setUauserid(String uauserid) {
        this.uauserid = uauserid;
    }

    public String getUaadate() {
        return uaadate;
    }

    public void setUaadate(String uaadate) {
        this.uaadate = uaadate;
    }
}
