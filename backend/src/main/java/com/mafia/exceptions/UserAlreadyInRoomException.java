package com.mafia.exceptions;

public class UserAlreadyInRoomException extends RuntimeException
{
    public UserAlreadyInRoomException(String message) { super(message); }
}