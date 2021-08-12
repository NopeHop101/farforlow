package com.main.farforlow.exception;

import com.main.farforlow.view.ServiceMessages;

public class DurationException extends Exception {
    public DurationException() {
        super(ServiceMessages.DURATION_EXCEPTION.text);
    }
}
