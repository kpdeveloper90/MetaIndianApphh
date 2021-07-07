package com.ecosense.app.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Sasurvey_Answer implements Serializable {


    @JsonProperty("name")
    private String suransID;

    @JsonProperty("surandatetime")
    private String surandatetime;

    @JsonProperty("surananstype")
    private String surananstype;

    @JsonProperty("surananswer")
    private String surananswer;

    @JsonProperty("suranquestionid")
    private String suranquestionid;

    public String getSuransID() {
        return suransID;
    }

    public void setSuransID(String suransID) {
        this.suransID = suransID;
    }

    public String getSurandatetime() {
        return surandatetime;
    }

    public void setSurandatetime(String surandatetime) {
        this.surandatetime = surandatetime;
    }

    public String getSurananstype() {
        return surananstype;
    }

    public void setSurananstype(String surananstype) {
        this.surananstype = surananstype;
    }

    public String getSurananswer() {
        return surananswer;
    }

    public void setSurananswer(String surananswer) {
        this.surananswer = surananswer;
    }

    public String getSuranquestionid() {
        return suranquestionid;
    }

    public void setSuranquestionid(String suranquestionid) {
        this.suranquestionid = suranquestionid;
    }
}
