package com.mafia.exceptions;

public class RoomFullException extends RuntimeException
{
    public RoomFullException(String message) { super(message); }
}