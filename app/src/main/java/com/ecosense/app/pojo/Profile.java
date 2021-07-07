package com.ecosense.app.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Profile implements Serializable {


    @JsonProperty("impc_desig")
    private String impc_desig ;

    @JsonProperty("impc_no")
    private String impc_no;

    @JsonProperty("impc_name")
    private String impc_name;

    @JsonProperty("proabout")
    private String proabout;

    @JsonProperty("proterms")
    private String proterms;

    @JsonProperty("name")
    private String name;

    @JsonProperty("prodwns")
    private String prodwns;

    @JsonProperty("proprivacy")
    private String proprivacy;

    @JsonProperty("profaq")
    private String profaq;

    public String getProfaq() {
        return profaq;
    }

    public void setProfaq(String profaq) {
        this.profaq = profaq;
    }

    public String getImpc_desig() {
        return impc_desig;
    }

    public void setImpc_desig(String impc_desig) {
        this.impc_desig = impc_desig;
    }

    public String getImpc_no() {
        return impc_no;
    }

    public void setImpc_no(String impc_no) {
        this.impc_no = impc_no;
    }

    public String getImpc_name() {
        return impc_name;
    }

    public void setImpc_name(String impc_name) {
        this.impc_name = impc_name;
    }

    public String getProabout() {
        return proabout;
    }

    public void setProabout(String proabout) {
        this.proabout = proabout;
    }

    public String getProterms() {
        return proterms;
    }

    public void setProterms(String proterms) {
        this.proterms = proterms;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProdwns() {
        return prodwns;
    }

    public void setProdwns(String prodwns) {
        this.prodwns = prodwns;
    }

    public String getProprivacy() {
        return proprivacy;
    }

    public void setProprivacy(String proprivacy) {
        this.proprivacy = proprivacy;
    }
}
