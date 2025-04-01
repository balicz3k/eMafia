package com.mafia.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

@Entity @Table(name = "game_rooms") @Getter @Setter public class GameRoom
{
    public GameRoom(String name, User host, int maxPlayers)
    {
        this.name = name;
        this.host = host;
        this.maxPlayers = maxPlayers;
        this.players = new ArrayList<>();
        this.players.add(host.getId());
        this.status = GameRoomStatus.CREATED;
        this.createdAt = LocalDateTime.now();
        this.invitations = new ArrayList<>();
        this.id = UUID.randomUUID();
    }
    public enum GameRoomStatus
    {
        CREATED,
        IN_PROGRESS,
        FINISHED
    }

    @Id @GeneratedValue @UuidGenerator private UUID id;

    @Column(nullable = false, length = 30) private String name;

    @Column(name = "max_players", nullable = false) private int maxPlayers;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "host_id", nullable = false) private User host;

    @Enumerated(EnumType.STRING) @Column(nullable = false) private GameRoomStatus status = GameRoomStatus.CREATED;

    @CreationTimestamp @Column(name = "created_at") private LocalDateTime createdAt;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Invitation> invitations = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "game_players", joinColumns = @JoinColumn(name = "room_id"))
    @Column(name = "player_id")
    private List<UUID> players = new ArrayList<>();
}