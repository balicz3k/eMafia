package com.mafia.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "users",
       uniqueConstraints = { @UniqueConstraint(columnNames = "email")
                             , @UniqueConstraint(columnNames = "username") })
@Getter
@Setter
public class User
{

    @Id @GeneratedValue @UuidGenerator(style = UuidGenerator.Style.TIME) private UUID id;

    @Column(nullable = false, length = 20) private String username;

    @Column(nullable = false, length = 50) private String email;

    @Column(nullable = false) private String password;

    @CreationTimestamp @Column(name = "created_at") private LocalDateTime createdAt;

    @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role_name")
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "host", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<GameRoom> hostedRooms = new ArrayList<>();
}