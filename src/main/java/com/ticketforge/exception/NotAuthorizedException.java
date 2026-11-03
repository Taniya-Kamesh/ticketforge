package com.ticketforge.exception;

public class NotAuthorizedException extends Exception{
    public NotAuthorizedException(String message) {
        super(message);
    }
}
