package com.mafia.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateGameRoomRequest
{

    @NotBlank(message = "Room name is required")
    @Size(min = 3, max = 100, message = "Room name must be between 3 and 100 characters")
    private String name;

    @Min(value = 2, message = "Minimum number of players is 2")
    @Max(value = 20, message = "Maximum number of players is 20")
    private int maxPlayers;

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public int getMaxPlayers() { return maxPlayers; }

    public void setMaxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; }
}