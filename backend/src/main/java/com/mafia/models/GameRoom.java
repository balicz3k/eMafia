package com.mafia.models;

import java.util.UUID;

public class GameRoom
{
    private UUID id;
    private String name;
    private int maxPlayers;

    public GameRoom(String name, int maxPlayers)
    {
        this.name = name;
        this.maxPlayers = maxPlayers;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getMaxPlayers() { return maxPlayers; }
    public void setMaxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; }
}