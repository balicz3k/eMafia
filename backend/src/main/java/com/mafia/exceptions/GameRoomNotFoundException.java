package com.mafia.exceptions;

public class GameRoomNotFoundException extends RuntimeException
{
    public GameRoomNotFoundException(String message) { super(message); }
}