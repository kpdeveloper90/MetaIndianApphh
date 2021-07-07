package com.ecosense.app.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;


@JsonIgnoreProperties(ignoreUnknown = true)
public class Event implements Serializable {

    @JsonProperty("name")
    private String event_id;

    @JsonProperty("evnbroadcast_date")
    private String evnbroadcast_date;

    @JsonProperty("evnlatitude")
    private String evnlatitude;

    @JsonProperty("evnlongitude")
    private String evnlongitude;

    @JsonProperty("evnorgnr")
    private String evnorgnr;

    @JsonProperty("evnurl")
    private String evnurl;

    @JsonProperty("evntitle")
    private String evntitle;

    @JsonProperty("evndate")
    private String evndate;

    @JsonProperty("evnphoto")
    private String evnphoto;

    @JsonProperty("evndetail")
    private String evndetail;

    @JsonProperty("evnstatus")
    private String evnstatus;

    @JsonProperty("evnloc")
    private String evnloc;
    @JsonProperty("evndislike")
    private String evndislike;
    @JsonProperty("evnlike")
    private String evnlike;

    public String getEvndislike() {
        return evndislike;
    }

    public void setEvndislike(String evndislike) {
        this.evndislike = evndislike;
    }

    public String getEvnlike() {
        return evnlike;
    }

    public void setEvnlike(String evnlike) {
        this.evnlike = evnlike;
    }

    public String getEvnurl() {
        return evnurl;
    }

    public void setEvnurl(String evnurl) {
        this.evnurl = evnurl;
    }

    public String getEvent_id() {
        return event_id;
    }

    public void setEvent_id(String event_id) {
        this.event_id = event_id;
    }

    public String getEvnbroadcast_date() {
        return evnbroadcast_date;
    }

    public void setEvnbroadcast_date(String evnbroadcast_date) {
        this.evnbroadcast_date = evnbroadcast_date;
    }

    public String getEvnlatitude() {
        return evnlatitude;
    }

    public void setEvnlatitude(String evnlatitude) {
        this.evnlatitude = evnlatitude;
    }

    public String getEvnlongitude() {
        return evnlongitude;
    }

    public void setEvnlongitude(String evnlongitude) {
        this.evnlongitude = evnlongitude;
    }

    public String getEvnorgnr() {
        return evnorgnr;
    }

    public void setEvnorgnr(String evnorgnr) {
        this.evnorgnr = evnorgnr;
    }

    public String getEvntitle() {
        return evntitle;
    }

    public void setEvntitle(String evntitle) {
        this.evntitle = evntitle;
    }

    public String getEvndate() {
        return evndate;
    }

    public void setEvndate(String evndate) {
        this.evndate = evndate;
    }

    public String getEvnphoto() {
        return evnphoto;
    }

    public void setEvnphoto(String evnphoto) {
        this.evnphoto = evnphoto;
    }

    public String getEvndetail() {
        return evndetail;
    }

    public void setEvndetail(String evndetail) {
        this.evndetail = evndetail;
    }

    public String getEvnstatus() {
        return evnstatus;
    }

    public void setEvnstatus(String evnstatus) {
        this.evnstatus = evnstatus;
    }

    public String getEvnloc() {
        return evnloc;
    }

    public void setEvnloc(String evnloc) {
        this.evnloc = evnloc;
    }
}
