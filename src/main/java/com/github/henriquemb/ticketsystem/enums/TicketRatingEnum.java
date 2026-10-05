package com.github.henriquemb.ticketsystem.enums;

import com.github.henriquemb.ticketsystem.TicketSystem;

public enum TicketRatingEnum {
    CANCELED(-1),
    UNAVAILABLE(0),
    TERRIBLE(1),
    BAD(2),
    REGULAR(3),
    GOOD(4),
    GREAT(5);

    private final int rate;

    TicketRatingEnum(int rate) {
        this.rate = rate;
    }

    public int getRate() {
        return rate;
    }

    public String getColor() {
        return TicketSystem.getMessages().getString(String.format("status.ticket.%s.color", this).toLowerCase());
    }

    public String getName() {
        return TicketSystem.getMessages().getString(String.format("status.ticket.%s.name", this).toLowerCase());
    }

    public String format() {
        return getColor() + getName();
    }

    public static TicketRatingEnum fromName(String name) {
        for (TicketRatingEnum value : values()) {
            if (value.name().equalsIgnoreCase(name)) return value;
        }
        return null;
    }
}
