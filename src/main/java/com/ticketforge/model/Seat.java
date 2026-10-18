package com.ticketforge.model;

import java.time.LocalDateTime;

public class Seat {
    private long id;
    private long eventId;
    private String seatLabel;
    private SeatStatus status;
    private Long heldBy;
    private LocalDateTime holdExpiresAt;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getEventId() {
        return eventId;
    }

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }

    public String getSeatLabel() {
        return seatLabel;
    }

    public void setSeatLabel(String seatLabel) {
        this.seatLabel = seatLabel;
    }

    public SeatStatus getStatus() {
        return status;
    }

    public void setStatus(SeatStatus status) {
        this.status = status;
    }

    public Long getHeldBy() {
        return heldBy;
    }

    public void setHeldBy(Long heldBy) {
        this.heldBy = heldBy;
    }

    public LocalDateTime getHoldExpiresAt() {
        return holdExpiresAt;
    }

    public void setHoldExpiresAt(LocalDateTime holdExpiresAt) {
        this.holdExpiresAt = holdExpiresAt;
    }

    @Override
    public String toString() {
        return "Seat{" +
                "id=" + id +
                ", eventId=" + eventId +
                ", seatLabel='" + seatLabel + '\'' +
                ", status=" + status +
                ", heldBy=" + heldBy +
                ", holdExpiresAt=" + holdExpiresAt +
                '}';
    }
}
