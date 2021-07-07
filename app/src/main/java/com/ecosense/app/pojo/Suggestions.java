package com.ecosense.app.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Suggestions implements Serializable {

    @JsonProperty("name")
    private String sugId;

    @JsonProperty("sugdate")
    private String sugdate;

    @JsonProperty("sugcategory")
    private String sugcategory;

    @JsonProperty("suguname")
    private String suguname;

    @JsonProperty("sugmobile")
    private String sugmobile;

    @JsonProperty("sugemail")
    private String sugemail;

    @JsonProperty("sugphoto")
    private String sugphoto;

    @JsonProperty("suggestion")
    private String suggestion;

    public String getSugId() {
        return sugId;
    }

    public void setSugId(String sugId) {
        this.sugId = sugId;
    }

    public String getSugdate() {
        return sugdate;
    }

    public void setSugdate(String sugdate) {
        this.sugdate = sugdate;
    }

    public String getSugcategory() {
        return sugcategory;
    }

    public void setSugcategory(String sugcategory) {
        this.sugcategory = sugcategory;
    }

    public String getSuguname() {
        return suguname;
    }

    public void setSuguname(String suguname) {
        this.suguname = suguname;
    }

    public String getSugmobile() {
        return sugmobile;
    }

    public void setSugmobile(String sugmobile) {
        this.sugmobile = sugmobile;
    }

    public String getSugemail() {
        return sugemail;
    }

    public void setSugemail(String sugemail) {
        this.sugemail = sugemail;
    }

    public String getSugphoto() {
        return sugphoto;
    }

    public void setSugphoto(String sugphoto) {
        this.sugphoto = sugphoto;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }
}
