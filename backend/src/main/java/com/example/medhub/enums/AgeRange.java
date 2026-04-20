package com.example.medhub.enums;

public enum AgeRange {
    UNDER_18("under 18"),
    AGE_18_30("18-30"),
    AGE_31_50("31-50"),
    AGE_51_70("51-70"),
    OVER_70("over 70");

    private final String label;

    AgeRange(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
