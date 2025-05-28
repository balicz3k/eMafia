package com.mafia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Request to create a new game room")
public class CreateGameRoomRequest {

    @Schema(description = "Name of the game room", example = "Friday Night Mafia", minLength = 1, maxLength = 50, required = true)
    @NotBlank(message = "Room name is required")
    @Size(max = 50, message = "Room name cannot exceed 50 characters")
    private String name;

    @Schema(description = "Maximum number of players allowed in the room", example = "8", minimum = "4", maximum = "20", required = true)
    @NotNull(message = "Max players is required")
    @Min(value = 4, message = "Minimum 4 players required")
    @Max(value = 20, message = "Maximum 20 players allowed")
    private Integer maxPlayers;

    // Konstruktory, getters i setters...
    public CreateGameRoomRequest() {
    }

    public CreateGameRoomRequest(String name, Integer maxPlayers) {
        this.name = name;
        this.maxPlayers = maxPlayers;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(Integer maxPlayers) {
        this.maxPlayers = maxPlayers;
    }
}