package com.ecosense.app.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Vehicle implements Serializable {
    //New Field   ========================
    @JsonProperty("name")
    private String name;


    @JsonProperty("capacity")
    private String vehicle_capacity;


    @JsonProperty("chassis_number")
    private String chassis_number;
    @JsonProperty("puc_validity_start_1")
    private String puc_validity_start_1;

    @JsonProperty("puc_validity_end")
    private String puc_validity_end;

    @JsonProperty("permit_number")
    private String license_number;

    @JsonProperty("registration_number")
    private String vehicle_registration_no;


    @JsonProperty("size_of_tyre")
    private String size_of_tyres;


    @JsonProperty("vendor_name")
    private String vehicle_owner_name;

    @JsonProperty("vender_registration_number")
    private String vender_registration_number;

    @JsonProperty("vendor_number")
    private String vendor_moblie_no;

    @JsonProperty("vendor_address")
    private String vendor_address;


    @JsonProperty("vehicle_type")
    private String vehicle_type;

    @JsonProperty("barcode")
    private String barcode;

    @JsonProperty("route_name")
    private String vm_routename;


    @JsonProperty("route_last_modified")
    private String route_lastmodified;

    @JsonProperty("route_info")
    private String route_info;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVehicle_capacity() {
        return vehicle_capacity;
    }

    public void setVehicle_capacity(String vehicle_capacity) {
        this.vehicle_capacity = vehicle_capacity;
    }

    public String getChassis_number() {
        return chassis_number;
    }

    public void setChassis_number(String chassis_number) {
        this.chassis_number = chassis_number;
    }

    public String getPuc_validity_start_1() {
        return puc_validity_start_1;
    }

    public void setPuc_validity_start_1(String puc_validity_start_1) {
        this.puc_validity_start_1 = puc_validity_start_1;
    }

    public String getPuc_validity_end() {
        return puc_validity_end;
    }

    public void setPuc_validity_end(String puc_validity_end) {
        this.puc_validity_end = puc_validity_end;
    }

    public String getLicense_number() {
        return license_number;
    }

    public void setLicense_number(String license_number) {
        this.license_number = license_number;
    }

    public String getVehicle_registration_no() {
        return vehicle_registration_no;
    }

    public void setVehicle_registration_no(String vehicle_registration_no) {
        this.vehicle_registration_no = vehicle_registration_no;
    }

    public String getSize_of_tyres() {
        return size_of_tyres;
    }

    public void setSize_of_tyres(String size_of_tyres) {
        this.size_of_tyres = size_of_tyres;
    }

    public String getVehicle_owner_name() {
        return vehicle_owner_name;
    }

    public void setVehicle_owner_name(String vehicle_owner_name) {
        this.vehicle_owner_name = vehicle_owner_name;
    }

    public String getVender_registration_number() {
        return vender_registration_number;
    }

    public void setVender_registration_number(String vender_registration_number) {
        this.vender_registration_number = vender_registration_number;
    }

    public String getVendor_moblie_no() {
        return vendor_moblie_no;
    }

    public void setVendor_moblie_no(String vendor_moblie_no) {
        this.vendor_moblie_no = vendor_moblie_no;
    }

    public String getVendor_address() {
        return vendor_address;
    }

    public void setVendor_address(String vendor_address) {
        this.vendor_address = vendor_address;
    }

    public String getVehicle_type() {
        return vehicle_type;
    }

    public void setVehicle_type(String vehicle_type) {
        this.vehicle_type = vehicle_type;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getVm_routename() {
        return vm_routename;
    }

    public void setVm_routename(String vm_routename) {
        this.vm_routename = vm_routename;
    }

    public String getRoute_lastmodified() {
        return route_lastmodified;
    }

    public void setRoute_lastmodified(String route_lastmodified) {
        this.route_lastmodified = route_lastmodified;
    }

    public String getRoute_info() {
        return route_info;
    }

    public void setRoute_info(String route_info) {
        this.route_info = route_info;
    }
}
