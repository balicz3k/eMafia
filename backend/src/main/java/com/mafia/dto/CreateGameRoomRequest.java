package com.mafia.dto;
import com.mafia.models.User;

public class CreateGameRoomRequest
{
    private String name;
    private int maxPlayers;
    private User host;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getMaxPlayers() { return maxPlayers; }
    public void setMaxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; }

    public User getHost() { return host; }
    public void setHost(User host) { this.host = host; }
}