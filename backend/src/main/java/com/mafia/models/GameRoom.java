package com.mafia.models;

import java.util.UUID;

public class GameRoom
{
    private final UUID id;
    private String name;
    private int maxPlayers;

    public GameRoom(String name, int maxPlayers)
    {
        this.id = UUID.randomUUID();
        this.name = name;
        this.maxPlayers = maxPlayers;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public int getMaxPlayers() { return maxPlayers; }

    public void setName(String name) { this.name = name; }
    public void setMaxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; }
}