package com.main.farforlow.exception;

public class SearchPeriodException extends Exception {
    public SearchPeriodException() {
        super("Please check dates. Use exact format: 20.03.2030-20.04.2030. Plus make sure search period is longer than max trip duration, both dates are in the future and start date followed by end date.");
    }
}
