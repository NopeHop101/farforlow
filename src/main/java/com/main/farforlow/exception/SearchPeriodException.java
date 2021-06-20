package com.main.farforlow.exception;

public class SearchPeriodException extends Exception {
    public SearchPeriodException() {
        super("Please use the format: 20.03.2030-20.05.2030. Plus make sure search period is longer than trip duration");
    }
}
