package com.ecosense.app.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginDetail implements Serializable {
    private int success;//0 is error and 1 is success

    @JsonProperty("name")
    private String userId;

    @JsonProperty("reglatitude")
    private String reglatitude;

    @JsonProperty("reglongitude")
    private String reglongitude;

    @JsonProperty("cost_center")
    private String cost_center;
    @JsonProperty("reglocation")
    private String reglocation;

    @JsonProperty("regmobile")
    private String regmobile;

    @JsonProperty("regfname")
    private String regfname;

    @JsonProperty("message")
    private String message;

    @JsonProperty("full_name")
    private String full_name;

    @JsonProperty("regusertype")
    private String regusertype;

    @JsonProperty("regsubusertype")
    private String regsubusertype;

    @JsonProperty("regteam")
    private String regteam;

    @JsonProperty("regemail")
    private String regemail;

    @JsonProperty("status")
    private String status;

    @JsonProperty("regdate")
    private String regdate;

    @JsonProperty("type")
    private String otp_type;

    @JsonProperty("reg_photo")
    private String reg_photo;

    @JsonProperty("reg_teamphoto")
    private String reg_teamphoto;

//    @JsonProperty("reg_license_number") after go  kainext name update
    @JsonProperty("driving_license_number")
    private String reg_license_number;

    @JsonProperty("reg_wardno")
    private String reg_wardno;

    @JsonProperty("reg_impcno")
    private ArrayList<Profile> reg_impcno;

/////// For Employee Login Field

    @JsonProperty("designation")
    private String designation;

   @JsonProperty("employee_name")
    private String employee_name;

    @JsonProperty("personal_email")
    private String personal_email;

    @JsonProperty("image")
    private String image;

    public String getOtp_type() {
        return otp_type;
    }

    public void setOtp_type(String otp_type) {
        this.otp_type = otp_type;
    }

    public String getCost_center() {
        return cost_center;
    }

    public void setCost_center(String cost_center) {
        this.cost_center = cost_center;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getEmployee_name() {
        return employee_name;
    }

    public void setEmployee_name(String employee_name) {
        this.employee_name = employee_name;
    }

    public String getPersonal_email() {
        return personal_email;
    }

    public void setPersonal_email(String personal_email) {
        this.personal_email = personal_email;
    }

    public String getReg_teamphoto() {
        return reg_teamphoto;
    }

    public void setReg_teamphoto(String reg_teamphoto) {
        this.reg_teamphoto = reg_teamphoto;
    }

    public String getReg_wardno() {
        return reg_wardno;
    }

    public void setReg_wardno(String reg_wardno) {
        this.reg_wardno = reg_wardno;
    }

    public String getReg_photo() {
        return reg_photo;
    }

    public void setReg_photo(String reg_photo) {
        this.reg_photo = reg_photo;
    }

    public ArrayList<Profile> getReg_impcno() {
        return reg_impcno;
    }

    public void setReg_impcno(ArrayList<Profile> reg_impcno) {
        this.reg_impcno = reg_impcno;
    }

    public String getReg_license_number() {
        return reg_license_number;
    }

    public void setReg_license_number(String reg_license_number) {
        this.reg_license_number = reg_license_number;
    }

    public String getRegsubusertype() {
        return regsubusertype;
    }

    public void setRegsubusertype(String regsubusertype) {
        this.regsubusertype = regsubusertype;
    }

    public String getRegteam() {
        return regteam;
    }

    public void setRegteam(String regteam) {
        this.regteam = regteam;
    }

    public String getRegdate() {
        return regdate;
    }

    public void setRegdate(String regdate) {
        this.regdate = regdate;
    }

    public String getRegusertype() {
        return regusertype;
    }

    public void setRegusertype(String regusertype) {
        this.regusertype = regusertype;
    }

    public String getRegemail() {
        return regemail;
    }

    public void setRegemail(String regemail) {
        this.regemail = regemail;
    }

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getReglatitude() {
        return reglatitude;
    }

    public void setReglatitude(String reglatitude) {
        this.reglatitude = reglatitude;
    }

    public String getReglongitude() {
        return reglongitude;
    }

    public void setReglongitude(String reglongitude) {
        this.reglongitude = reglongitude;
    }

    public String getReglocation() {
        return reglocation;
    }

    public void setReglocation(String reglocation) {
        this.reglocation = reglocation;
    }

    public String getRegmobile() {
        return regmobile;
    }

    public void setRegmobile(String regmobile) {
        this.regmobile = regmobile;
    }

    public String getRegfname() {
        return regfname;
    }

    public void setRegfname(String regfname) {
        this.regfname = regfname;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }
}
