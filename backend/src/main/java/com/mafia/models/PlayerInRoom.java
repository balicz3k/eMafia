package com.mafia.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

@Entity @Table(name = "players_in_rooms") @Getter @Setter @NoArgsConstructor public class PlayerInRoom
{

    @Id @GeneratedValue @UuidGenerator private UUID id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false) private User user;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "game_room_id", nullable = false) private GameRoom gameRoom;

    @Column(nullable = false, length = 50) private String nicknameInRoom;

    @Column(nullable = false) private boolean isAlive = true;

    @CreationTimestamp @Column(name = "joined_at", nullable = false, updatable = false) private LocalDateTime joinedAt;

    // TODO: W przyszłości można dodać pole roli w grze (np. MAFIA, CITIZEN)
    // @Enumerated(EnumType.STRING)
    // private GameRole gameRole;

    public PlayerInRoom(User user, GameRoom gameRoom, String nicknameInRoom)
    {
        this.user = user;
        this.gameRoom = gameRoom;
        this.nicknameInRoom = nicknameInRoom;
    }
}