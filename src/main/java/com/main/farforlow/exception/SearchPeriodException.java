package com.main.farforlow.exception;

import com.main.farforlow.view.ServiceMessages;

public class SearchPeriodException extends Exception {
    public SearchPeriodException() {
        super(ServiceMessages.SEARCH_PERIOD_EXCEPTION.text);
    }
}
