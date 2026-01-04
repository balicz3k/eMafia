package com.mafia.exceptions;

public class VotingSessionNotFoundException extends RuntimeException {
    public VotingSessionNotFoundException(String message) {
        super(message);
    }
}
