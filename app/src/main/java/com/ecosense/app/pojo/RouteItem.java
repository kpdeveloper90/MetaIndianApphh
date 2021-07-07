package com.ecosense.app.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RouteItem implements Serializable {


    @JsonProperty("name")
    private String name;

    @JsonProperty("creation")
    private String creation;
    @JsonProperty("modified")
    private String modified;

    @JsonProperty("r_name")
    private String r_name;

    @JsonProperty("r_status")
    private String r_status;

    @JsonProperty("fawardno")
    private String fawardno;

    @JsonProperty("r_info")
    private ArrayList<RouteInfo> r_info;

    @JsonProperty("da_routeinfo_act")
    private ArrayList<RouteInfo> da_routeinfo_act;

    @JsonProperty("da_vehicleno")
    private String da_vehicleno;

    @JsonProperty("da_routeid")
    private String da_routeid;

    @JsonProperty("da_date")
    private String da_date;

    @JsonProperty("vehicle_type")
    private String vehicle_type;

    @JsonProperty("da_drivername")
    private String da_drivername;

    @JsonProperty("da_mobno")
    private String da_mobno;

    @JsonProperty("da_dlno")
    private String da_dlno;

    @JsonProperty("vehicle_registration_no")
    private String vehicle_registration_no;

    public String getFawardno() {
        return fawardno;
    }

    public void setFawardno(String fawardno) {
        this.fawardno = fawardno;
    }

    public String getVehicle_type() {
        return vehicle_type;
    }

    public String getCreation() {
        return creation;
    }

    public void setCreation(String creation) {
        this.creation = creation;
    }

    public void setVehicle_type(String vehicle_type) {
        this.vehicle_type = vehicle_type;
    }

    public String getVehicle_registration_no() {
        return vehicle_registration_no;
    }

    public void setVehicle_registration_no(String vehicle_registration_no) {
        this.vehicle_registration_no = vehicle_registration_no;
    }

    public String getDa_vehicleno() {
        return da_vehicleno;
    }

    public void setDa_vehicleno(String da_vehicleno) {
        this.da_vehicleno = da_vehicleno;
    }

    public String getDa_routeid() {
        return da_routeid;
    }

    public void setDa_routeid(String da_routeid) {
        this.da_routeid = da_routeid;
    }

    public String getDa_date() {
        return da_date;
    }

    public void setDa_date(String da_date) {
        this.da_date = da_date;
    }

    public String getDa_drivername() {
        return da_drivername;
    }

    public void setDa_drivername(String da_drivername) {
        this.da_drivername = da_drivername;
    }

    public String getDa_mobno() {
        return da_mobno;
    }

    public void setDa_mobno(String da_mobno) {
        this.da_mobno = da_mobno;
    }

    public String getDa_dlno() {
        return da_dlno;
    }

    public void setDa_dlno(String da_dlno) {
        this.da_dlno = da_dlno;
    }

    public ArrayList<RouteInfo> getDa_routeinfo_act() {
        return da_routeinfo_act;
    }

    public void setDa_routeinfo_act(ArrayList<RouteInfo> da_routeinfo_act) {
        this.da_routeinfo_act = da_routeinfo_act;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModified() {
        return modified;
    }

    public void setModified(String modified) {
        this.modified = modified;
    }

    public String getR_name() {
        return r_name;
    }

    public void setR_name(String r_name) {
        this.r_name = r_name;
    }

    public String getR_status() {
        return r_status;
    }

    public void setR_status(String r_status) {
        this.r_status = r_status;
    }

    public ArrayList<RouteInfo> getR_info() {
        return r_info;
    }

    public void setR_info(ArrayList<RouteInfo> r_info) {
        this.r_info = r_info;
    }
}
