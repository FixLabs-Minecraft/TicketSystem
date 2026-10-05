package com.github.henriquemb.ticketsystem.enums;

import com.github.henriquemb.ticketsystem.TicketSystem;

public enum ReportStatusEnum {
    WAITING(0),
    ACCEPTED(1),
    REVIEW(2),
    REJECT(3);

    private final int id;

    ReportStatusEnum(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getColor() {
        return TicketSystem.getMessages().getString(String.format("status.report.%s.color", this).toLowerCase());
    }

    public String getName() {
        return TicketSystem.getMessages().getString(String.format("status.report.%s.name", this).toLowerCase());
    }

    public String format() {
        return getColor() + getName();
    }

    public static ReportStatusEnum fromName(String name) {
        for (ReportStatusEnum value : values()) {
            if (value.name().equalsIgnoreCase(name)) return value;
        }
        return null;
    }
}
