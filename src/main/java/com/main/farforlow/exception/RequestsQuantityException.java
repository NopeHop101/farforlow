package com.main.farforlow.exception;

public class RequestsQuantityException extends Exception {
    public RequestsQuantityException() {
        super("Request is too loose :(. Try more precise trip duration and/or search period.");
    }
}
