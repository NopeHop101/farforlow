package com.main.farforlow.exception;

public class RequestsQuantityException extends Exception {
    public RequestsQuantityException(String message) {
//        super("Request is too loose :(. %d options need to be checked and current limit is %d options per request. Try more precise trip duration and/or search period.");
        super(message);
    }
}
