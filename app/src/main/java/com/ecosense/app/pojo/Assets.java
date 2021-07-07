package com.ecosense.app.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Assets implements Serializable {


//   new Field Name ========================

    @JsonProperty("name")
    private String asset_Id;

    @JsonProperty("da_routeid")
    private String da_routeid;

    @JsonProperty("asset_name")
    private String asset_name;

    @JsonProperty("item_name")
    private String item_name;

    @JsonProperty("ward_no")
    private String fawardno;

    @JsonProperty("ward_name")
    private String fawardname;

    @JsonProperty("longitude")
    private String falongitude;

    @JsonProperty("latitude")
    private String falatitude;

    @JsonProperty("geo_location")
    private String falocation;

    @JsonProperty("for_date")
    private String for_date;

    @JsonProperty("bin_color")
    private String facolor;

    @JsonProperty("r_name")
    private String r_name;

    @JsonProperty("route_info")
    private String farouteno;

    @JsonProperty("image_string")
    private String fabinimage;

    @JsonProperty("image")
    private String fabin_icon;

    @JsonProperty("address")
    private String falatlonglocation;

    @JsonProperty("capacity")
    private String facapcty;

    @JsonProperty("age_of_bin")
    private String age_of_bin;

    @JsonProperty("type_of_establishments")
    private String type_of_establishments;

    @JsonProperty("barcode")
    private String faqr_code_no;

    @JsonProperty("modified")
    private String modified;

    @JsonProperty("type_of_bin")
    private String type_of_bin;
    @JsonProperty("employee_id")
    private String employee_id;

    // for metadata use id
    @JsonProperty("item_code")
    private String item_code;

//   old Field Name ========================


    public String getAsset_name() {
        return asset_name;
    }

    public void setAsset_name(String asset_name) {
        this.asset_name = asset_name;
    }

    public String getFor_date() {
        return for_date;
    }

    public void setFor_date(String for_date) {
        this.for_date = for_date;
    }

    private int mul_issue_count;
    private String mul_issue_tag;
//
//    @JsonProperty("fadate")
//    private String fadate;


//    @JsonProperty("fatype")
//    private String fatype;

    public String getDa_routeid() {
        return da_routeid;
    }

    public void setDa_routeid(String da_routeid) {
        this.da_routeid = da_routeid;
    }

    public String getFabin_icon() {
        return fabin_icon;
    }

    public void setFabin_icon(String fabin_icon) {
        this.fabin_icon = fabin_icon;
    }

    public String getEmployee_id() {
        return employee_id;
    }

    public void setEmployee_id(String employee_id) {
        this.employee_id = employee_id;
    }

    @JsonProperty("fastatus")
    private String fastatus;

    public String getType_of_bin() {
        return type_of_bin;
    }

    public void setType_of_bin(String type_of_bin) {
        this.type_of_bin = type_of_bin;
    }

    public String getItem_code() {
        return item_code;
    }

    public void setItem_code(String item_code) {
        this.item_code = item_code;
    }

    public String getItem_name() {
        return item_name;
    }

    public void setItem_name(String item_name) {
        this.item_name = item_name;
    }

    public String getR_name() {
        return r_name;
    }

    public void setR_name(String r_name) {
        this.r_name = r_name;
    }

    public String getAge_of_bin() {
        return age_of_bin;
    }

    public void setAge_of_bin(String age_of_bin) {
        this.age_of_bin = age_of_bin;
    }



    public String getFalatlonglocation() {
        return falatlonglocation;
    }

    public void setFalatlonglocation(String falatlonglocation) {
        this.falatlonglocation = falatlonglocation;
    }

    public String getType_of_establishments() {
        return type_of_establishments;
    }

    public void setType_of_establishments(String type_of_establishments) {
        this.type_of_establishments = type_of_establishments;
    }

    public String getFaqr_code_no() {
        return faqr_code_no;
    }

    public void setFaqr_code_no(String faqr_code_no) {
        this.faqr_code_no = faqr_code_no;
    }

    public String getFarouteno() {
        return farouteno;
    }

    public void setFarouteno(String farouteno) {
        this.farouteno = farouteno;
    }

    public String getModified() {
        return modified;
    }

    public void setModified(String modified) {
        this.modified = modified;
    }


    public String getFawardname() {
        return fawardname;
    }

    public void setFawardname(String fawardname) {
        this.fawardname = fawardname;
    }

    public String getFawardno() {
        return fawardno;
    }

    public void setFawardno(String fawardno) {
        this.fawardno = fawardno;
    }

    public String getFabinimage() {
        return fabinimage;
    }

    public void setFabinimage(String fabinimage) {
        this.fabinimage = fabinimage;
    }

    public String getAsset_Id() {
        return asset_Id;
    }

    public void setAsset_Id(String asset_Id) {
        this.asset_Id = asset_Id;
    }

    public String getFalongitude() {
        return falongitude;
    }

    public void setFalongitude(String falongitude) {
        this.falongitude = falongitude;
    }

    public String getFalatitude() {
        return falatitude;
    }

    public void setFalatitude(String falatitude) {
        this.falatitude = falatitude;
    }

//    public String getFadate() {
//        return fadate;
//    }
//
//    public void setFadate(String fadate) {
//        this.fadate = fadate;
//    }

    public String getFalocation() {
        return falocation;
    }

    public void setFalocation(String falocation) {
        this.falocation = falocation;
    }

//
//    public String getFatype() {
//        return fatype;
//    }
//
//    public void setFatype(String fatype) {
//        this.fatype = fatype;
//    }

    public String getFastatus() {
        return fastatus;
    }

    public void setFastatus(String fastatus) {
        this.fastatus = fastatus;
    }


    public String getFacolor() {
        return facolor;
    }

    public void setFacolor(String facolor) {
        this.facolor = facolor;
    }

    public String getFacapcty() {
        return facapcty;
    }

    public void setFacapcty(String facapcty) {
        this.facapcty = facapcty;
    }

    public int getMul_issue_count() {
        return mul_issue_count;
    }

    public void setMul_issue_count(int mul_issue_count) {
        this.mul_issue_count = mul_issue_count;
    }

    public String getMul_issue_tag() {
        return mul_issue_tag;
    }

    public void setMul_issue_tag(String mul_issue_tag) {
        this.mul_issue_tag = mul_issue_tag;
    }
}
