package com.ecosense.app.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Reminder implements Serializable {


    @JsonProperty("name")
    private String remId;
    @JsonProperty("ruserid")
    private String ruserid;

    @JsonProperty("rdatetime")
    private String rdatetime;

    @JsonProperty("rusernmae")
    private String rusernmae;

    @JsonProperty("ecomplaintsno")
    private String ecomplaintsno;

    //    @JsonProperty("ecomplaintsno")
    @JsonProperty("rdesc")
    private String rdesc;

    public String getRemId() {
        return remId;
    }

    public void setRemId(String remId) {
        this.remId = remId;
    }

    public String getRuserid() {
        return ruserid;
    }

    public void setRuserid(String ruserid) {
        this.ruserid = ruserid;
    }

    public String getRdatetime() {
        return rdatetime;
    }

    public void setRdatetime(String rdatetime) {
        this.rdatetime = rdatetime;
    }

    public String getRusernmae() {
        return rusernmae;
    }

    public void setRusernmae(String rusernmae) {
        this.rusernmae = rusernmae;
    }

    public String getEcomplaintsno() {
        return ecomplaintsno;
    }

    public void setEcomplaintsno(String ecomplaintsno) {
        this.ecomplaintsno = ecomplaintsno;
    }

    public String getRdesc() {
        return rdesc;
    }

    public void setRdesc(String rdesc) {
        this.rdesc = rdesc;
    }
}
