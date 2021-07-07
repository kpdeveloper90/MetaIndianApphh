package com.ecosense.app.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;


@JsonIgnoreProperties(ignoreUnknown = true)
public class News implements Serializable {

    @JsonProperty("name")
    private String news_id;

    @JsonProperty("nwtitle")
    private String nwtitle;

    @JsonProperty("nwpftime")
    private String nwpftime;

    @JsonProperty("nwphoto")
    private String nwphoto;

    @JsonProperty("nwdescription")
    private String nwdescription;

    @JsonProperty("nwurl")
    private String nwurl;

    @JsonProperty("nwnewsstatus")
    private String nwnewsstatus;

    @JsonProperty("nwdislike")
    private String nwdislike;
    @JsonProperty("nwlike")
    private String nwlike;

    public String getNwnewsstatus() {
        return nwnewsstatus;
    }

    public void setNwnewsstatus(String nwnewsstatus) {
        this.nwnewsstatus = nwnewsstatus;
    }

    public String getNwdislike() {
        return nwdislike;
    }

    public void setNwdislike(String nwdislike) {
        this.nwdislike = nwdislike;
    }

    public String getNwlike() {
        return nwlike;
    }

    public void setNwlike(String nwlike) {
        this.nwlike = nwlike;
    }

    public String getNwurl() {
        return nwurl;
    }

    public void setNwurl(String nwurl) {
        this.nwurl = nwurl;
    }

    public String getNews_id() {
        return news_id;
    }

    public void setNews_id(String news_id) {
        this.news_id = news_id;
    }

    public String getNwtitle() {
        return nwtitle;
    }

    public void setNwtitle(String nwtitle) {
        this.nwtitle = nwtitle;
    }

    public String getNwpftime() {
        return nwpftime;
    }

    public void setNwpftime(String nwpftime) {
        this.nwpftime = nwpftime;
    }

    public String getNwphoto() {
        return nwphoto;
    }

    public void setNwphoto(String nwphoto) {
        this.nwphoto = nwphoto;
    }

    public String getNwdescription() {
        return nwdescription;
    }

    public void setNwdescription(String nwdescription) {
        this.nwdescription = nwdescription;
    }
}
