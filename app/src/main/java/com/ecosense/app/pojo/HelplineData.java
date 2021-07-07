package com.ecosense.app.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HelplineData implements Serializable {

    @JsonProperty("name")
    private String name;


    @JsonProperty("cl_icon")
    private String helplineIcond;

    @JsonProperty("hl_name")
    private String helplineName;

    @JsonProperty("hl_contactno")
    private String helplineNumber;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHelplineIcond() {
        return helplineIcond;
    }

    public void setHelplineIcond(String helplineIcond) {
        this.helplineIcond = helplineIcond;
    }

    public String getHelplineName() {
        return helplineName;
    }

    public void setHelplineName(String helplineName) {
        this.helplineName = helplineName;
    }

    public String getHelplineNumber() {
        return helplineNumber;
    }

    public void setHelplineNumber(String helplineNumber) {
        this.helplineNumber = helplineNumber;
    }
}
