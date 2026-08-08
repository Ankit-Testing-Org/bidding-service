package com.evatech.bidplatform.dashboard.dto;

public enum Priority {

    CRITICAL("Critical", "#dc2626"),
    HIGH("High", "#dc2626"),
    MEDIUM("Medium", "#ea580c"),
    LOW("Low", "#0284c7");

    private final String label;
    private final String color;

    Priority(String label, String color) {
        this.label = label;
        this.color = color;
    }

    public String getLabel() {
        return label;
    }

    public String getColor() {
        return color;
    }
}