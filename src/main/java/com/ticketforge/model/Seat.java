package com.ticketforge.model;

import java.time.LocalDateTime;

public class Seat {
    private long id;
    private long event_id;
    private String seat_label;
    private SeatStatus status;
    private Long held_by;
    private LocalDateTime holdExpiresAt;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getEvent_id() {
        return event_id;
    }

    public void setEvent_id(long event_id) {
        this.event_id = event_id;
    }

    public String getSeat_label() {
        return seat_label;
    }

    public void setSeat_label(String seat_label) {
        this.seat_label = seat_label;
    }

    public SeatStatus getStatus() {
        return status;
    }

    public void setStatus(SeatStatus status) {
        this.status = status;
    }

    public Long getHeld_by(Long i) {
        return held_by;
    }

    public void setHeld_by(Long held_by) {
        this.held_by = held_by;
    }

    public LocalDateTime getHoldExpiresAt(LocalDateTime localDateTime) {
        return holdExpiresAt;
    }

    public void setHoldExpiresAt(LocalDateTime holdExpiresAt) {
        this.holdExpiresAt = holdExpiresAt;
    }

    @Override
    public String toString() {
        return "Seat{" +
                "id=" + id +
                ", event_id=" + event_id +
                ", seat_label='" + seat_label + '\'' +
                ", status=" + status +
                ", held_by=" + held_by +
                ", holdExpiresAt=" + holdExpiresAt +
                '}';
    }
}
