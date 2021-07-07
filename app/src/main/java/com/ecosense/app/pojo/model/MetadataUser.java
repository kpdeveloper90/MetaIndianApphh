package com.ecosense.app.pojo.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * <h1>POJO class of a metadata user</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class MetadataUser  extends User{
    private String imageUrl;
    @SerializedName("role")
    private String type;
    private String subType;
    private String costCenter;
    private List<Contact> contactList;

    public MetadataUser(String userName, String password) {
        super(userName, password);
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public String getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(String costCenter) {
        this.costCenter = costCenter;
    }

    public List<Contact> getContactList() {
        return contactList;
    }

    public void setContactList(List<Contact> contactList) {
        this.contactList = contactList;
    }
}
