package com.ecosense.app.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HowTos implements Serializable {

    @JsonProperty("name")
    private String howTos_id;


    @JsonProperty("httitle")
    private String httitle;

    @JsonProperty("htartical")
    private String htartical;

    @JsonProperty("htdate")
    private String htdate;

    @JsonProperty("htimage")
    private String htimage;


    @JsonProperty("htque_1")
    private String htque_1;
    @JsonProperty("htans_1")
    private String htans_1;

    @JsonProperty("htque_2")
    private String htque_2;
    @JsonProperty("htans_2")
    private String htans_2;


    @JsonProperty("htque_3")
    private String htque_3;
    @JsonProperty("htans_3")
    private String htans_3;


    @JsonProperty("htque_4")
    private String htque_4;
    @JsonProperty("htans_4")
    private String htans_4;

    @JsonProperty("htvideo")
    private String htvideo;

    @JsonProperty("hturl")
    private String hturl;
    @JsonProperty("htlike")
    private String htlike;
    @JsonProperty("htdislike")
    private String htdislike;

    public String getHtlike() {
        return htlike;
    }

    public void setHtlike(String htlike) {
        this.htlike = htlike;
    }

    public String getHtdislike() {
        return htdislike;
    }

    public void setHtdislike(String htdislike) {
        this.htdislike = htdislike;
    }

    public String getHturl() {
        return hturl;
    }

    public void setHturl(String hturl) {
        this.hturl = hturl;
    }

    public String getHtvideo() {
        return htvideo;
    }

    public void setHtvideo(String htvideo) {
        this.htvideo = htvideo;
    }

    public String getHowTos_id() {
        return howTos_id;
    }

    public void setHowTos_id(String howTos_id) {
        this.howTos_id = howTos_id;
    }

    public String getHttitle() {
        return httitle;
    }

    public void setHttitle(String httitle) {
        this.httitle = httitle;
    }

    public String getHtartical() {
        return htartical;
    }

    public void setHtartical(String htartical) {
        this.htartical = htartical;
    }

    public String getHtdate() {
        return htdate;
    }

    public void setHtdate(String htdate) {
        this.htdate = htdate;
    }

    public String getHtimage() {
        return htimage;
    }

    public void setHtimage(String htimage) {
        this.htimage = htimage;
    }

    public String getHtque_1() {
        return htque_1;
    }

    public void setHtque_1(String htque_1) {
        this.htque_1 = htque_1;
    }

    public String getHtans_1() {
        return htans_1;
    }

    public void setHtans_1(String htans_1) {
        this.htans_1 = htans_1;
    }

    public String getHtque_2() {
        return htque_2;
    }

    public void setHtque_2(String htque_2) {
        this.htque_2 = htque_2;
    }

    public String getHtans_2() {
        return htans_2;
    }

    public void setHtans_2(String htans_2) {
        this.htans_2 = htans_2;
    }

    public String getHtque_3() {
        return htque_3;
    }

    public void setHtque_3(String htque_3) {
        this.htque_3 = htque_3;
    }

    public String getHtans_3() {
        return htans_3;
    }

    public void setHtans_3(String htans_3) {
        this.htans_3 = htans_3;
    }

    public String getHtque_4() {
        return htque_4;
    }

    public void setHtque_4(String htque_4) {
        this.htque_4 = htque_4;
    }

    public String getHtans_4() {
        return htans_4;
    }

    public void setHtans_4(String htans_4) {
        this.htans_4 = htans_4;
    }
}
