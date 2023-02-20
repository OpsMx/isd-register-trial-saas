package com.opsmx.isd.register.enums;

public enum CDType {

    isdSpinnaker("ISD-Spinnaker"),
    isdArgo("ISD-Argo");

    private String description;

    CDType(String description) {
        this.description = description;
    }
}
