package com.ecosense.app.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Fuel implements Serializable {

    @JsonProperty("name")
    private String name;

    @JsonProperty("pump_location_lat")
    private String pump_location_lat;

    @JsonProperty("pump_address")
    private String pump_address;

    @JsonProperty("pump_location_lang")
    private String pump_location_lang;

    @JsonProperty("pump_name")
    private String pump_name;
    @JsonProperty("oem_name")
    private String oem_name;

    @JsonProperty("rate")
    private String rate;

    @JsonProperty("quantity")
    private String quantity;

    @JsonProperty("total")
    private String total;

    @JsonProperty("meter_reading")
    private String meter_reading;

    @JsonProperty("supervisor_id")
    private String supervisor_id;

    @JsonProperty("supervisor_name")
    private String supervisor_name;

    @JsonProperty("vehicle_number")
    private String vehicle_number;

    @JsonProperty("date")
    private String date;

    @JsonProperty("creation")
    private String creation;

    boolean fixedQty;

    public String getOem_name() {
        return oem_name;
    }

    public void setOem_name(String oem_name) {
        this.oem_name = oem_name;
    }

    public boolean isFixedQty() {
        return fixedQty;
    }

    public void setFixedQty(boolean fixedQty) {
        this.fixedQty = fixedQty;
    }

    public String getCreation() {
        return creation;
    }

    public void setCreation(String creation) {
        this.creation = creation;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPump_location_lat() {
        return pump_location_lat;
    }

    public void setPump_location_lat(String pump_location_lat) {
        this.pump_location_lat = pump_location_lat;
    }

    public String getPump_address() {
        return pump_address;
    }

    public void setPump_address(String pump_address) {
        this.pump_address = pump_address;
    }

    public String getPump_location_lang() {
        return pump_location_lang;
    }

    public void setPump_location_lang(String pump_location_lang) {
        this.pump_location_lang = pump_location_lang;
    }

    public String getPump_name() {
        return pump_name;
    }

    public void setPump_name(String pump_name) {
        this.pump_name = pump_name;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getMeter_reading() {
        return meter_reading;
    }

    public void setMeter_reading(String meter_reading) {
        this.meter_reading = meter_reading;
    }

    public String getSupervisor_id() {
        return supervisor_id;
    }

    public void setSupervisor_id(String supervisor_id) {
        this.supervisor_id = supervisor_id;
    }

    public String getSupervisor_name() {
        return supervisor_name;
    }

    public void setSupervisor_name(String supervisor_name) {
        this.supervisor_name = supervisor_name;
    }

    public String getVehicle_number() {
        return vehicle_number;
    }

    public void setVehicle_number(String vehicle_number) {
        this.vehicle_number = vehicle_number;
    }
}
