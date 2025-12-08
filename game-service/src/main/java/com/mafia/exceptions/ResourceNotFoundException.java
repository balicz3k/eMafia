package com.mafia.exceptions;

public class ResourceNotFoundException extends RuntimeException
{
    public ResourceNotFoundException(String message) { super(message); }
}