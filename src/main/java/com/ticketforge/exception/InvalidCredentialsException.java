package com.ticketforge.exception;

public class InvalidCredentialsException extends Exception{
    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}
