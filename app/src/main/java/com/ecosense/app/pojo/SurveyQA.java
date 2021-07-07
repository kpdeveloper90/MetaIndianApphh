package com.ecosense.app.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SurveyQA implements Serializable {

    @JsonProperty("name")
    private String QAID;


    @JsonProperty("idx")
    private String idx;
    @JsonProperty("surquestion")
    private String surquestion;

    @JsonProperty("suranswer")
    private String suranswer;

    @JsonProperty("suranstype")
    private String suranstype;


    @JsonProperty("surdatetime")
    private String surdatetime;

    @JsonProperty("aasurveyid")
    private String aasurveyid;

    @JsonProperty("sauserid")
    private String sauserid;

    @JsonProperty("surstatus")
    private String surstatus;

    @JsonProperty("sauname")
    private String sauname;

    @JsonProperty("saplatform")
    private String saplatform;

    @JsonProperty("sasurvey_answer")
    private List<Sasurvey_Answer> sasurvey_answer;

    public String getSaplatform() {
        return saplatform;
    }

    public void setSaplatform(String saplatform) {
        this.saplatform = saplatform;
    }

    public String getSurstatus() {
        return surstatus;
    }

    public void setSurstatus(String surstatus) {
        this.surstatus = surstatus;
    }

    public List<Sasurvey_Answer> getSasurvey_answer() {
        return sasurvey_answer;
    }

    public void setSasurvey_answer(List<Sasurvey_Answer> sasurvey_answer) {
        this.sasurvey_answer = sasurvey_answer;
    }

    public String getAasurveyid() {
        return aasurveyid;
    }

    public void setAasurveyid(String aasurveyid) {
        this.aasurveyid = aasurveyid;
    }

    public String getSauserid() {
        return sauserid;
    }

    public void setSauserid(String sauserid) {
        this.sauserid = sauserid;
    }

    public String getSauname() {
        return sauname;
    }

    public void setSauname(String sauname) {
        this.sauname = sauname;
    }

    public String getQAID() {
        return QAID;
    }

    public void setQAID(String QAID) {
        this.QAID = QAID;
    }

    public String getIdx() {
        return idx;
    }

    public void setIdx(String idx) {
        this.idx = idx;
    }

    public String getSurquestion() {
        return surquestion;
    }

    public void setSurquestion(String surquestion) {
        this.surquestion = surquestion;
    }

    public String getSuranswer() {
        return suranswer;
    }

    public void setSuranswer(String suranswer) {
        this.suranswer = suranswer;
    }

    public String getSuranstype() {
        return suranstype;
    }

    public void setSuranstype(String suranstype) {
        this.suranstype = suranstype;
    }

    public String getSurdatetime() {
        return surdatetime;
    }

    public void setSurdatetime(String surdatetime) {
        this.surdatetime = surdatetime;
    }
}
