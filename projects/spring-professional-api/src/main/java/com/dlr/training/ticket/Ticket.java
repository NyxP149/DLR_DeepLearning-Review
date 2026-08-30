package com.dlr.training.ticket;

public record Ticket(long id, String title, Status status) {
    public enum Status { OPEN, DONE }
}
