package com.klaudia.transactionsimporter.exceptions.statistics;

public class IbanNotFoundException extends RuntimeException {
    public IbanNotFoundException(String message) {
        super(message);
    }
}
