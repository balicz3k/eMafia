package com.mafia.repositiories;

import com.mafia.models.GameRoom;
import com.mafia.models.Invitation;
import com.mafia.models.User;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvitationRepository extends JpaRepository<Invitation, UUID>
{
    List<Invitation> findByReceiverAndStatus(User receiver, Invitation.InvitationStatus status);
    List<Invitation> findByRoom(GameRoom room);
}