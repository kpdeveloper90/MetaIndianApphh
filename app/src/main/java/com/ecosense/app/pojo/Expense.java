package com.ecosense.app.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Expense implements Serializable {

    @JsonProperty("name")
    private String name;
    @JsonProperty("modified")
    private String modified;

    @JsonProperty("creation")
    private String creation;

    @JsonProperty("approval_status")
    private String approval_status;

    @JsonProperty("status")
    private String status;

    @JsonProperty("title")
    private String title;

    @JsonProperty("total_claimed_amount")
    private String total_claimed_amount;

    @JsonProperty("cost_center")
    private String cost_center;
    @JsonProperty("employee")
    private String employee;

    @JsonProperty("employee_name")
    private String employee_name;

    @JsonProperty("expense_type")
    private String expense_type;

    @JsonProperty("expense_date")
    private String expense_date;

    @JsonProperty("claim_amount")
    private String claim_amount;
    @JsonProperty("sanctioned_amount")
    private String sanctioned_amount;

    @JsonProperty("mobile_no")
    private String mobile_no;

    @JsonProperty("description")
    private String description;

    @JsonProperty("expenses")
    private List<Expense> expenses;

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

    public String getModified() {
        return modified;
    }

    public void setModified(String modified) {
        this.modified = modified;
    }

    public String getCreation() {
        return creation;
    }

    public void setCreation(String creation) {
        this.creation = creation;
    }

    public String getApproval_status() {
        return approval_status;
    }

    public void setApproval_status(String approval_status) {
        this.approval_status = approval_status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTotal_claimed_amount() {
        return total_claimed_amount;
    }

    public void setTotal_claimed_amount(String total_claimed_amount) {
        this.total_claimed_amount = total_claimed_amount;
    }

    public String getCost_center() {
        return cost_center;
    }

    public void setCost_center(String cost_center) {
        this.cost_center = cost_center;
    }

    public String getEmployee() {
        return employee;
    }

    public void setEmployee(String employee) {
        this.employee = employee;
    }

    public String getEmployee_name() {
        return employee_name;
    }

    public void setEmployee_name(String employee_name) {
        this.employee_name = employee_name;
    }

    public String getExpense_type() {
        return expense_type;
    }

    public void setExpense_type(String expense_type) {
        this.expense_type = expense_type;
    }

    public String getExpense_date() {
        return expense_date;
    }

    public void setExpense_date(String expense_date) {
        this.expense_date = expense_date;
    }

    public String getClaim_amount() {
        return claim_amount;
    }

    public void setClaim_amount(String claim_amount) {
        this.claim_amount = claim_amount;
    }

    public String getSanctioned_amount() {
        return sanctioned_amount;
    }

    public void setSanctioned_amount(String sanctioned_amount) {
        this.sanctioned_amount = sanctioned_amount;
    }

    public String getMobile_no() {
        return mobile_no;
    }

    public void setMobile_no(String mobile_no) {
        this.mobile_no = mobile_no;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Expense> getExpenses() {
        return expenses;
    }

    public void setExpenses(List<Expense> expenses) {
        this.expenses = expenses;
    }
}
