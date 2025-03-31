package com.mafia.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

@Entity @Table(name = "invitations") @Getter @Setter public class Invitation
{

    public enum InvitationStatus
    {
        PENDING,
        ACCEPTED,
        DECLINED
    }

    @Id @GeneratedValue @UuidGenerator private UUID id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "room_id", nullable = false) private GameRoom room;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "sender_id", nullable = false) private User sender;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "receiver_id", nullable = false) private User receiver;

    @Enumerated(EnumType.STRING) @Column(nullable = false) private InvitationStatus status = InvitationStatus.PENDING;

    @CreationTimestamp @Column(name = "created_at") private LocalDateTime createdAt;
}