package com.mafia.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

@Entity @Table(name = "game_rooms") @Getter @Setter public class GameRoom
{

    @Id @GeneratedValue @UuidGenerator private UUID id;

    @Column(unique = true, nullable = false, length = 8) private String roomCode; // Unikalny, krótki kod pokoju

    @Column(nullable = false, length = 100) private String name; // Nazwa pokoju nadana przez użytkownika

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "host_id", nullable = false) private User host;

    @Column(nullable = false) private int maxPlayers;

    @Enumerated(EnumType.STRING) @Column(nullable = false) private GameRoomStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Relacja do graczy w pokoju (OneToMany lub ManyToMany) zostanie dodana później
    // np. @OneToMany(mappedBy = "gameRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    // private Set<PlayerInRoom> players = new HashSet<>();

    public GameRoom() { this.status = GameRoomStatus.WAITING_FOR_PLAYERS; }
}