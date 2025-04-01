package com.mafia.exceptions;

public class EmailAlreadyExistsException extends RuntimeException
{
    public EmailAlreadyExistsException(String message) { super(message); }
}
