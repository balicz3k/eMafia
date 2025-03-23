package com.mafia.dto;

public class CreateRoomRequest
{
    private String name;
    private int maxPlayers;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getMaxPlayers() { return maxPlayers; }
    public void setMaxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; }
}