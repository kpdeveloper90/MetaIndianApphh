package com.ecosense.app.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Voucher implements Serializable {


    @JsonProperty("name")
    private String name;

    @JsonProperty("rate")
    private String rate;

    @JsonProperty("quantity")
    private String quantity;

    @JsonProperty("total")
    private String total;

    @JsonProperty("supervisor_id")
    private String supervisor_id;

    @JsonProperty("supervisor_name")
    private String supervisor_name;

    @JsonProperty("vehicle_number")
    private String vehicle_number;
    @JsonProperty("status")
    private String status;

    @JsonProperty("generated_by")
    private String generated_by;


    @JsonProperty("voucher_amount")
    private String voucher_amount;

    @JsonProperty("voucher_reason")
    private String voucher_reason;

    @JsonProperty("driver_name")
    private String driver_name;

    @JsonProperty("driver_id")
    private String driver_id;

    @JsonProperty("voucher_use_date")
    private String voucher_use_date;

    @JsonProperty("voucher_status")
    private String voucher_status;

    @JsonProperty("creation")
    private String creation;

    private Vehicle vehicle;

    public String getGenerated_by() {
        return generated_by;
    }

    public void setGenerated_by(String generated_by) {
        this.generated_by = generated_by;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getVoucher_amount() {
        return voucher_amount;
    }

    public void setVoucher_amount(String voucher_amount) {
        this.voucher_amount = voucher_amount;
    }

    public String getVoucher_reason() {
        return voucher_reason;
    }

    public void setVoucher_reason(String voucher_reason) {
        this.voucher_reason = voucher_reason;
    }

    public String getDriver_name() {
        return driver_name;
    }

    public void setDriver_name(String driver_name) {
        this.driver_name = driver_name;
    }

    public String getDriver_id() {
        return driver_id;
    }

    public void setDriver_id(String driver_id) {
        this.driver_id = driver_id;
    }

    public String getVoucher_use_date() {
        return voucher_use_date;
    }

    public void setVoucher_use_date(String voucher_use_date) {
        this.voucher_use_date = voucher_use_date;
    }

    public String getVoucher_status() {
        return voucher_status;
    }

    public void setVoucher_status(String voucher_status) {
        this.voucher_status = voucher_status;
    }

    public String getCreation() {
        return creation;
    }

    public void setCreation(String creation) {
        this.creation = creation;
    }
}
