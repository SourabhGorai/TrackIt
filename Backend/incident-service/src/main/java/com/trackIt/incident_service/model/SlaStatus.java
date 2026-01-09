package com.trackIt.incident_service.model;

public enum SlaStatus {

    ALL,
    ACTIVE,
    CLOSED;

    public static SlaStatus from(String value) {
        return SlaStatus.valueOf(value.toUpperCase());
    }

}
