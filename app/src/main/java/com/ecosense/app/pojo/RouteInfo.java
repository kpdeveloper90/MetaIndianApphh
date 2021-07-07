package com.ecosense.app.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;


@JsonIgnoreProperties(ignoreUnknown = true)
public class RouteInfo implements Serializable {

    @JsonProperty("da_date")
    private String da_date;
    @JsonProperty("da_mobno")
    private String da_mobno;
    @JsonProperty("da_routeid")
    private String da_routeid;
    @JsonProperty("da_dlno")
    private String da_dlno;
    @JsonProperty("da_vehicleno")
    private String da_vehicleno;
    @JsonProperty("da_drivername")
    private String da_drivername;

    @JsonProperty("da_code")
    private String da_code;

    @JsonProperty("barcode")
    private String barcode;

    @JsonProperty("r_name")
    private String r_name;

    @JsonProperty("name")
    private String name;

    @JsonProperty("bin_reason")
    private String bin_reason;

    @JsonProperty("route_qrcode")
    private String route_qrcode;

    @JsonProperty("route_photo")
    private String route_photo;

    @JsonProperty("route_icon")
    private String route_icon;

    @JsonProperty("route_binphoto")
    private String route_binphoto;

    @JsonProperty("clean_status")
    private String clean_status;

    @JsonProperty("route_long")
    private String route_long;

    @JsonProperty("route_lat")
    private String route_lat;

    @JsonProperty("route_assetloc")
    private String route_assetloc;

    @JsonProperty("route_assetname")
    private String route_assetname;

    @JsonProperty("route_location_name")
    private String route_location_name;

    @JsonProperty("route_assetcap")
    private String route_assetcap;

    @JsonProperty("type")
    private String type;

    @JsonProperty("route_poi")
    private String route_poi;

    @JsonProperty("modified")
    private String modified;

    @JsonProperty("fawardno")
    private String fawardno;

    @JsonProperty("status")
    private String status;

    @JsonProperty("vehicle_type")
    private String vehicle_type;

    @JsonProperty("reason_for_breakdown")
    private String reason_for_breakdown;
    @JsonProperty("poi")
    private String poi;

    public String getRoute_photo() {
        return route_photo;
    }

    public void setRoute_photo(String route_photo) {
        this.route_photo = route_photo;
    }

    public String getPoi() {
        return poi;
    }

    public void setPoi(String poi) {
        this.poi = poi;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVehicle_type() {
        return vehicle_type;
    }

    public void setVehicle_type(String vehicle_type) {
        this.vehicle_type = vehicle_type;
    }

    public String getReason_for_breakdown() {
        return reason_for_breakdown;
    }

    public void setReason_for_breakdown(String reason_for_breakdown) {
        this.reason_for_breakdown = reason_for_breakdown;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getBin_reason() {
        return bin_reason;
    }

    public void setBin_reason(String bin_reason) {
        this.bin_reason = bin_reason;
    }

    public String getFawardno() {
        return fawardno;
    }

    public void setFawardno(String fawardno) {
        this.fawardno = fawardno;
    }

    public String getDa_date() {
        return da_date;
    }

    public void setDa_date(String da_date) {
        this.da_date = da_date;
    }

    public String getDa_mobno() {
        return da_mobno;
    }

    public void setDa_mobno(String da_mobno) {
        this.da_mobno = da_mobno;
    }

    public String getDa_routeid() {
        return da_routeid;
    }

    public void setDa_routeid(String da_routeid) {
        this.da_routeid = da_routeid;
    }

    public String getDa_dlno() {
        return da_dlno;
    }

    public void setDa_dlno(String da_dlno) {
        this.da_dlno = da_dlno;
    }

    public String getDa_vehicleno() {
        return da_vehicleno;
    }

    public void setDa_vehicleno(String da_vehicleno) {
        this.da_vehicleno = da_vehicleno;
    }

    public String getDa_drivername() {
        return da_drivername;
    }

    public void setDa_drivername(String da_drivername) {
        this.da_drivername = da_drivername;
    }

    public String getDa_code() {
        return da_code;
    }

    public void setDa_code(String da_code) {
        this.da_code = da_code;
    }

    public String getR_name() {
        return r_name;
    }

    public void setR_name(String r_name) {
        this.r_name = r_name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRoute_qrcode() {
        return route_qrcode;
    }

    public void setRoute_qrcode(String route_qrcode) {
        this.route_qrcode = route_qrcode;
    }

    public String getRoute_icon() {
        return route_icon;
    }

    public void setRoute_icon(String route_icon) {
        this.route_icon = route_icon;
    }

    public String getRoute_binphoto() {
        return route_binphoto;
    }

    public void setRoute_binphoto(String route_binphoto) {
        this.route_binphoto = route_binphoto;
    }

    public String getClean_status() {
        return clean_status;
    }

    public void setClean_status(String clean_status) {
        this.clean_status = clean_status;
    }

    public String getRoute_long() {
        return route_long;
    }

    public void setRoute_long(String route_long) {
        this.route_long = route_long;
    }

    public String getRoute_lat() {
        return route_lat;
    }

    public void setRoute_lat(String route_lat) {
        this.route_lat = route_lat;
    }

    public String getRoute_assetloc() {
        return route_assetloc;
    }

    public void setRoute_assetloc(String route_assetloc) {
        this.route_assetloc = route_assetloc;
    }

    public String getRoute_assetname() {
        return route_assetname;
    }

    public void setRoute_assetname(String route_assetname) {
        this.route_assetname = route_assetname;
    }

    public String getRoute_location_name() {
        return route_location_name;
    }

    public void setRoute_location_name(String route_location_name) {
        this.route_location_name = route_location_name;
    }

    public String getRoute_assetcap() {
        return route_assetcap;
    }

    public void setRoute_assetcap(String route_assetcap) {
        this.route_assetcap = route_assetcap;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRoute_poi() {
        return route_poi;
    }

    public void setRoute_poi(String route_poi) {
        this.route_poi = route_poi;
    }

    public String getModified() {
        return modified;
    }

    public void setModified(String modified) {
        this.modified = modified;
    }
}
