package com.ecosense.app.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Complaints implements Serializable {

    @JsonProperty("name")
    private String comId;

    @JsonProperty("cptuserid")
    private String cptuserid;

    @JsonProperty("cptid")
    private String cptid;

    @JsonProperty("cpttype")
    private String cpttype;

    @JsonProperty("cpttitle")
    private String cpttitle;

    @JsonProperty("cptdate")
    private String cptdate;

    @JsonProperty("cptlatitude")
    private String cptlatitude;

    @JsonProperty("cptlongitude")
    private String cptlongitude;

    @JsonProperty("cptloc")
    private String cptloc;

    @JsonProperty("cptloctype")
    private String cptloctype;

    @JsonProperty("cptdescription")
    private String cptdescription;


    @JsonProperty("cptphoto")
    private String cptphoto;

   @JsonProperty("cptresphoto")
    private String cptresphoto;


    @JsonProperty("cptfeedbk_date")
    private String cptfeedbk_date;

    @JsonProperty("cptfeedback")
    private String cptfeedback;


    @JsonProperty("cptstatus")
    private String cptstatus;


    @JsonProperty("cptmobileno")
    private String cptmobileno;

    @JsonProperty("cptpname")
    private String cptpname;

    @JsonProperty("cptassignto")
    private String com_assignTO;

    @JsonProperty("cptemail")
    private String cptemail;

    @JsonProperty("cptresdate")
    private String cptresdate;

    @JsonProperty("cptward_no")
    private String cptward_no;

    @JsonProperty("cptactstatuts")
    private String cptactstatuts;

    @JsonProperty("driver_id")
    private String driver_id;

    @JsonProperty("driver_name")
    private String driver_name;

    @JsonProperty("vehicle_no")
    private String vehicle_no;

    @JsonProperty("user_type")
    private String user_type;

    @JsonProperty("assign_date")
    private String assign_date;

    @JsonProperty("assignfrom")
    private String assignfrom;

    @JsonProperty("esta_name")
    private String esta_name;

    @JsonProperty("modified")
    private String modified;

    @JsonProperty("new")
    private String com_dash_new;

    @JsonProperty("pending")
    private String com_dash_pending;

    @JsonProperty("wip")
    private String com_dash_wip;

    @JsonProperty("closed")
    private String com_dash_closed;

    public String getCom_dash_new() {
        return com_dash_new;
    }

    public void setCom_dash_new(String com_dash_new) {
        this.com_dash_new = com_dash_new;
    }

    public String getCom_dash_pending() {
        return com_dash_pending;
    }

    public void setCom_dash_pending(String com_dash_pending) {
        this.com_dash_pending = com_dash_pending;
    }

    public String getCom_dash_wip() {
        return com_dash_wip;
    }

    public void setCom_dash_wip(String com_dash_wip) {
        this.com_dash_wip = com_dash_wip;
    }

    public String getCom_dash_closed() {
        return com_dash_closed;
    }

    public void setCom_dash_closed(String com_dash_closed) {
        this.com_dash_closed = com_dash_closed;
    }

    public String getModified() {
        return modified;
    }

    public void setModified(String modified) {
        this.modified = modified;
    }

    public String getEsta_name() {
        return esta_name;
    }

    public void setEsta_name(String esta_name) {
        this.esta_name = esta_name;
    }

    private String cptRemCount;

    public String getCptresphoto() {
        return cptresphoto;
    }

    public void setCptresphoto(String cptresphoto) {
        this.cptresphoto = cptresphoto;
    }

    public String getAssignfrom() {
        return assignfrom;
    }

    public void setAssignfrom(String assignfrom) {
        this.assignfrom = assignfrom;
    }

    public String getDriver_id() {
        return driver_id;
    }

    public void setDriver_id(String driver_id) {
        this.driver_id = driver_id;
    }

    public String getDriver_name() {
        return driver_name;
    }

    public void setDriver_name(String driver_name) {
        this.driver_name = driver_name;
    }

    public String getVehicle_no() {
        return vehicle_no;
    }

    public void setVehicle_no(String vehicle_no) {
        this.vehicle_no = vehicle_no;
    }

    public String getUser_type() {
        return user_type;
    }

    public void setUser_type(String user_type) {
        this.user_type = user_type;
    }

    public String getAssign_date() {
        return assign_date;
    }

    public void setAssign_date(String assign_date) {
        this.assign_date = assign_date;
    }

    public String getCptRemCount() {
        return cptRemCount;
    }

    public void setCptRemCount(String cptRemCount) {
        this.cptRemCount = cptRemCount;
    }

    public String getCptactstatuts() {
        return cptactstatuts;
    }

    public void setCptactstatuts(String cptactstatuts) {
        this.cptactstatuts = cptactstatuts;
    }

    public String getCptward_no() {
        return cptward_no;
    }

    public void setCptward_no(String cptward_no) {
        this.cptward_no = cptward_no;
    }

    public String getCptresdate() {
        return cptresdate;
    }

    public void setCptresdate(String cptresdate) {
        this.cptresdate = cptresdate;
    }

    public String getCptuserid() {
        return cptuserid;
    }

    public void setCptuserid(String cptuserid) {
        this.cptuserid = cptuserid;
    }

    public String getCptfeedbk_date() {
        return cptfeedbk_date;
    }

    public void setCptfeedbk_date(String cptfeedbk_date) {
        this.cptfeedbk_date = cptfeedbk_date;
    }

    public String getCptemail() {
        return cptemail;
    }

    public void setCptemail(String cptemail) {
        this.cptemail = cptemail;
    }

    public String getComId() {
        return comId;
    }

    public void setComId(String comId) {
        this.comId = comId;
    }

    public String getCptid() {
        return cptid;
    }

    public void setCptid(String cptid) {
        this.cptid = cptid;
    }

    public String getCpttype() {
        return cpttype;
    }

    public void setCpttype(String cpttype) {
        this.cpttype = cpttype;
    }

    public String getCpttitle() {
        return cpttitle;
    }

    public void setCpttitle(String  cpttitle) {
        this.cpttitle = cpttitle;
    }

    public  String getCptdate() {
        return  cptdate;
    }

    public void setCptdate(String cptdate) {
        this.cptdate = cptdate;
    }

    public String getCptlatitude() {
        return cptlatitude;
    }

    public void setCptlatitude(String cptlatitude) {
        this.cptlatitude = cptlatitude;
    }

    public String getCptlongitude() {
        return cptlongitude;
    }

    public void setCptlongitude(String cptlongitude) {
        this.cptlongitude = cptlongitude;
    }

    public String getCptloc() {
        return cptloc;
    }

    public void setCptloc(String cptloc) {
        this.cptloc = cptloc;
    }

    public String getCptloctype() {
        return cptloctype;
    }

    public void setCptloctype(String cptloctype) {
        this.cptloctype = cptloctype;
    }

    public String getCptdescription() {
        return cptdescription;
    }

    public void setCptdescription(String cptdescription) {
        this.cptdescription = cptdescription;
    }

    public String getCptphoto() {
        return cptphoto;
    }

    public void setCptphoto(String cptphoto) {
        this.cptphoto = cptphoto;
    }

    public String getCptfeedback() {
        return cptfeedback;
    }

    public void setCptfeedback(String cptfeedback) {
        this.cptfeedback = cptfeedback;
    }

    public String getCptstatus() {
        return cptstatus;
    }

    public void setCptstatus(String cptstatus) {
        this.cptstatus = cptstatus;
    }

    public String getCptmobileno() {
        return cptmobileno;
    }

    public void setCptmobileno(String cptmobileno) {
        this.cptmobileno = cptmobileno;
    }

    public String getCptpname() {
        return cptpname;
    }

    public void setCptpname(String cptpname) {
        this.cptpname = cptpname;
    }

    public String getCom_assignTO() {
        return com_assignTO;
    }

    public void setCom_assignTO(String com_assignTO) {
        this.com_assignTO = com_assignTO;
    }
}
