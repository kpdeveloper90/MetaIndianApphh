package com.ecosense.app.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Survey implements Serializable {

    @JsonProperty("name")
    private String surID;
    @JsonProperty("surphoto")
    private String surphoto;
    @JsonProperty("surpttime")
    private String surpttime;
    @JsonProperty("surpftime")
    private String surpftime;
    @JsonProperty("surtitle")
    private String surtitle;
    @JsonProperty("surauthor")
    private String surauthor;

    @JsonProperty("surtable")
    private ArrayList<SurveyQA> surtable;

    public ArrayList<SurveyQA> getSurtable() {
        return surtable;
    }

    public void setSurtable(ArrayList<SurveyQA> surtable) {
        this.surtable = surtable;
    }

    public String getSurID() {
        return surID;
    }

    public void setSurID(String surID) {
        this.surID = surID;
    }

    public String getSurphoto() {
        return surphoto;
    }

    public void setSurphoto(String surphoto) {
        this.surphoto = surphoto;
    }

    public String getSurpttime() {
        return surpttime;
    }

    public void setSurpttime(String surpttime) {
        this.surpttime = surpttime;
    }

    public String getSurpftime() {
        return surpftime;
    }

    public void setSurpftime(String surpftime) {
        this.surpftime = surpftime;
    }

    public String getSurtitle() {
        return surtitle;
    }

    public void setSurtitle(String surtitle) {
        this.surtitle = surtitle;
    }

    public String getSurauthor() {
        return surauthor;
    }

    public void setSurauthor(String surauthor) {
        this.surauthor = surauthor;
    }
}
