package com.mafia.services;

import com.mafia.dto.VoteRequest;
import com.mafia.dto.VoteResponse;
import com.mafia.enums.GamePhase;
import com.mafia.enums.GameRole;
import com.mafia.repositories.GameVoteRepository;
import com.mafia.repositories.PlayerGameStateRepository;
import com.mafia.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("GameEngineService unit tests")
class GameEngineServiceTest {

    private PlayerGameStateRepository players;
    private GameVoteRepository votes;
    private UserRepository users;
    private SimpMessagingTemplate messaging;
    private GameRoomService gameRoomService;

    private GameEngineService service;

    @BeforeEach
    void setUp() {
        players = mock(PlayerGameStateRepository.class);
        votes = mock(GameVoteRepository.class);
        users = mock(UserRepository.class);
        messaging = mock(SimpMessagingTemplate.class);
        gameRoomService = mock(GameRoomService.class);
        service = new GameEngineService(players, votes, users, messaging, gameRoomService);
    }

    // Helper stubs for domain objects that are not present in source tree listing
    static class PlayerGameState {
        private String roomCode;
        private UUID userId;
        private GameRole role;
        private boolean alive = true;
        private boolean suspended = false;
        public String getRoomCode() { return roomCode; }
        public void setRoomCode(String roomCode) { this.roomCode = roomCode; }
        public UUID getUserId() { return userId; }
        public void setUserId(UUID userId) { this.userId = userId; }
        public GameRole getRole() { return role; }
        public void setGameRole(GameRole role) { this.role = role; }
        public boolean isAlive() { return alive; }
        public void setAlive(boolean alive) { this.alive = alive; }
        public boolean isSuspended() { return suspended; }
        public void setSuspended(boolean suspended) { this.suspended = suspended; }
    }

    static class Vote {
        private final String roomCode;
        private final GamePhase phase;
        private final Integer dayNumber;
        private final UUID voterId;
        private UUID targetUserId;
        private LocalDateTime votedAt;
        Vote(String roomCode, GamePhase phase, Integer dayNumber, UUID voterId, UUID targetUserId) {
            this.roomCode = roomCode; this.phase = phase; this.dayNumber = dayNumber; this.voterId = voterId; this.targetUserId = targetUserId;
        }
        public UUID getTargetUserId() { return targetUserId; }
        public void setTargetUserId(UUID id) { this.targetUserId = id; }
        public UUID getVoterId() { return voterId; }
        public void setVotedAt(LocalDateTime t) { this.votedAt = t; }
    }

    static class User { private UUID id; private String username; public User(UUID id, String username){this.id=id;this.username=username;} public UUID getId(){return id;} public String getUsername(){return username;} }

    // Mocks for repository methods used in service
    interface GameVoteRepository {
        void deleteByRoomCodeAndPhase(String roomCode, GamePhase phase);
        Optional<Vote> findByRoomCodeAndPhaseAndDayNumberAndVoterId(String roomCode, GamePhase phase, Integer dayNumber, UUID voterId);
        void save(Vote vote);
        long countByRoomCodeAndPhaseAndDayNumber(String roomCode, GamePhase phase, Integer dayNumber);
        List<Vote> findByRoomCodeAndPhaseAndDayNumber(String roomCode, GamePhase phase, Integer dayNumber);
        List<Vote> findByRoomCodeAndPhase(String roomCode, GamePhase phase);
        long countByRoomCodeAndPhase(String roomCode, GamePhase phase);
        Optional<Vote> findByRoomCodeAndPhaseAndVoterId(String roomCode, GamePhase phase, UUID voterId);
    }
    interface PlayerGameStateRepository {
        PlayerGameState findByRoomCodeAndUserId(String roomCode, UUID userId);
        long countByRoomCodeAndAliveTrue(String roomCode);
        List<PlayerGameState> findByRoomCodeAndAliveTrue(String roomCode);
        List<PlayerGameState> findByRoomCodeAndAliveTrueAndRole(String roomCode, GameRole role);
        PlayerGameState save(PlayerGameState p);
    }
    interface UserRepository {
        Optional<User> findByUsername(String username);
        Optional<User> findById(UUID id);
    }

    private VoteRequest buildVoteRequest(String voter, String target, GamePhase phase, GameRole voterRole, int day) {
        VoteRequest req = new VoteRequest();
        req.setVoterUsername(voter);
        req.setTargetUsername(target);
        req.setGamePhase(phase);
        req.setVoterRole(voterRole);
        req.setDayNumber(day);
        return req;
    }

    @Test
    @DisplayName("Should reject night vote by non-mafia")
    void shouldRejectNightVoteByNonMafia() {
        String room = "R1"; UUID voterId = UUID.randomUUID(); UUID targetId = UUID.randomUUID();
        when(users.findByUsername("alice")).thenReturn(Optional.of(new User(voterId, "alice")));
        when(users.findByUsername("bob")).thenReturn(Optional.of(new User(targetId, "bob")));
        PlayerGameState voter = new PlayerGameState(); voter.setRoomCode(room); voter.setUserId(voterId); voter.setGameRole(GameRole.CITIZEN); voter.setAlive(true);
        PlayerGameState target = new PlayerGameState(); target.setRoomCode(room); target.setUserId(targetId); target.setGameRole(GameRole.MAFIA); target.setAlive(true);
        when(players.findByRoomCodeAndUserId(room, voterId)).thenReturn(voter);
        when(players.findByRoomCodeAndUserId(room, targetId)).thenReturn(target);

        VoteRequest req = buildVoteRequest("alice","bob", GamePhase.NIGHT_VOTE, GameRole.CITIZEN, 1);
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.doVote(room, req));
        assertTrue(ex.getMessage().toLowerCase().contains("only mafia"));
        verifyNoInteractions(votes);
    }

    @Test
    @DisplayName("Should accept day vote and broadcast progress")
    void shouldAcceptDayVoteAndBroadcast() {
        String room = "R1"; UUID voterId = UUID.randomUUID(); UUID targetId = UUID.randomUUID();
        when(users.findByUsername("alice")).thenReturn(Optional.of(new User(voterId, "alice")));
        when(users.findByUsername("bob")).thenReturn(Optional.of(new User(targetId, "bob")));
        PlayerGameState voter = new PlayerGameState(); voter.setRoomCode(room); voter.setUserId(voterId); voter.setGameRole(GameRole.CITIZEN); voter.setAlive(true);
        PlayerGameState target = new PlayerGameState(); target.setRoomCode(room); target.setUserId(targetId); target.setGameRole(GameRole.CITIZEN); target.setAlive(true);
        when(players.findByRoomCodeAndUserId(room, voterId)).thenReturn(voter);
        when(players.findByRoomCodeAndUserId(room, targetId)).thenReturn(target);

        when(votes.findByRoomCodeAndPhaseAndDayNumberAndVoterId(room, GamePhase.DAY_ELIMINATION, 1, voterId)).thenReturn(Optional.empty());
        when(players.countByRoomCodeAndAliveTrue(room)).thenReturn(3L);
        when(votes.countByRoomCodeAndPhaseAndDayNumber(room, GamePhase.DAY_ELIMINATION, 1)).thenReturn(1L);

        VoteRequest req = buildVoteRequest("alice","bob", GamePhase.DAY_ELIMINATION, GameRole.CITIZEN, 1);
        VoteResponse resp = service.doVote(room, req);

        assertEquals("VOTE_RECEIVED", resp.getStatus());
        assertEquals(1L, resp.getVotesReceived());
        assertFalse(resp.isVotingComplete());
        verify(votes, times(1)).save(any());
        verify(messaging, atLeastOnce()).convertAndSend(startsWith("/topic/rooms/"+room), any());
    }

    @Test
    @DisplayName("Should complete day voting with clear majority and eliminate target without phase transition tie case")
    void shouldCompleteDayVotingAndEliminate() {
        String room = "R1"; int day = 1;
        UUID voterId = UUID.randomUUID(); UUID targetId = UUID.randomUUID();
        when(users.findByUsername("alice")).thenReturn(Optional.of(new User(voterId, "alice")));
        when(users.findByUsername("bob")).thenReturn(Optional.of(new User(targetId, "bob")));
        PlayerGameState voter = new PlayerGameState(); voter.setRoomCode(room); voter.setUserId(voterId); voter.setGameRole(GameRole.CITIZEN); voter.setAlive(true);
        PlayerGameState target = new PlayerGameState(); target.setRoomCode(room); target.setUserId(targetId); target.setGameRole(GameRole.CITIZEN); target.setAlive(true);
        when(players.findByRoomCodeAndUserId(room, voterId)).thenReturn(voter);
        when(players.findByRoomCodeAndUserId(room, targetId)).thenReturn(target);

        when(votes.findByRoomCodeAndPhaseAndDayNumberAndVoterId(room, GamePhase.DAY_ELIMINATION, day, voterId)).thenReturn(Optional.empty());
        when(players.countByRoomCodeAndAliveTrue(room)).thenReturn(1L);
        when(votes.countByRoomCodeAndPhaseAndDayNumber(room, GamePhase.DAY_ELIMINATION, day)).thenReturn(1L);

        // resolveVoting support: 1 vote for target
        Vote cast = new Vote(room, GamePhase.DAY_ELIMINATION, day, voterId, targetId);
        when(votes.findByRoomCodeAndPhaseAndDayNumber(room, GamePhase.DAY_ELIMINATION, day)).thenReturn(List.of(cast));
        when(users.findById(targetId)).thenReturn(Optional.of(new User(targetId, "bob")));

        VoteRequest req = buildVoteRequest("alice","bob", GamePhase.DAY_ELIMINATION, GameRole.CITIZEN, day);
        VoteResponse resp = service.doVote(room, req);

        assertEquals("VOTING_COMPLETE", resp.getStatus());
        assertEquals(targetId, resp.getEliminatedPlayerId());
        verify(players).save(argThat(p -> p.getUserId().equals(targetId) && !p.isAlive()));
        verify(messaging, atLeastOnce()).convertAndSend(startsWith("/topic/rooms/"+room), any());
    }

    @Test
    @DisplayName("Night tie should randomly eliminate among top targets")
    void nightTieRandomElimination() {
        String room = "R1"; int day = 1;
        UUID voterId = UUID.randomUUID(); UUID t1 = UUID.randomUUID(); UUID t2 = UUID.randomUUID();
        when(users.findByUsername("alice")).thenReturn(Optional.of(new User(voterId, "alice")));
        when(users.findByUsername("bob")).thenReturn(Optional.of(new User(t1, "bob")));
        PlayerGameState voter = new PlayerGameState(); voter.setRoomCode(room); voter.setUserId(voterId); voter.setGameRole(GameRole.MAFIA); voter.setAlive(true);
        PlayerGameState target1 = new PlayerGameState(); target1.setRoomCode(room); target1.setUserId(t1); target1.setGameRole(GameRole.CITIZEN); target1.setAlive(true);
        PlayerGameState target2 = new PlayerGameState(); target2.setRoomCode(room); target2.setUserId(t2); target2.setGameRole(GameRole.CITIZEN); target2.setAlive(true);
        when(players.findByRoomCodeAndUserId(room, voterId)).thenReturn(voter);
        when(players.findByRoomCodeAndUserId(room, t1)).thenReturn(target1);
        when(players.findByRoomCodeAndUserId(room, t2)).thenReturn(target2);

        when(votes.findByRoomCodeAndPhaseAndDayNumberAndVoterId(room, GamePhase.NIGHT_VOTE, day, voterId)).thenReturn(Optional.empty());
        when(players.countByRoomCodeAndAliveTrue(room)).thenReturn(1L);
        when(votes.countByRoomCodeAndPhaseAndDayNumber(room, GamePhase.NIGHT_VOTE, day)).thenReturn(1L);

        // Two targets tied
        Vote v1 = new Vote(room, GamePhase.NIGHT_VOTE, day, UUID.randomUUID(), t1);
        Vote v2 = new Vote(room, GamePhase.NIGHT_VOTE, day, UUID.randomUUID(), t2);
        when(votes.findByRoomCodeAndPhaseAndDayNumber(room, GamePhase.NIGHT_VOTE, day)).thenReturn(List.of(v1, v2));
        when(users.findById(any())).thenAnswer(inv -> Optional.of(new User((UUID) inv.getArgument(0), "user")));

        VoteRequest req = buildVoteRequest("alice","bob", GamePhase.NIGHT_VOTE, GameRole.MAFIA, day);
        VoteResponse resp = service.doVote(room, req);

        assertEquals("VOTING_COMPLETE", resp.getStatus());
        assertNotNull(resp.getEliminatedPlayerId());
        verify(players).save(argThat(p -> p.getUserId().equals(resp.getEliminatedPlayerId()) && !p.isAlive()));
    }

    @Test
    @DisplayName("Day tie should result in no elimination")
    void dayTieNoElimination() {
        String room = "R1"; int day = 1;
        UUID voterId = UUID.randomUUID(); UUID t1 = UUID.randomUUID(); UUID t2 = UUID.randomUUID();
        when(users.findByUsername("alice")).thenReturn(Optional.of(new User(voterId, "alice")));
        when(users.findByUsername("bob")).thenReturn(Optional.of(new User(t1, "bob")));
        PlayerGameState voter = new PlayerGameState(); voter.setRoomCode(room); voter.setUserId(voterId); voter.setGameRole(GameRole.CITIZEN); voter.setAlive(true);
        PlayerGameState target1 = new PlayerGameState(); target1.setRoomCode(room); target1.setUserId(t1); target1.setGameRole(GameRole.CITIZEN); target1.setAlive(true);
        PlayerGameState target2 = new PlayerGameState(); target2.setRoomCode(room); target2.setUserId(t2); target2.setGameRole(GameRole.CITIZEN); target2.setAlive(true);
        when(players.findByRoomCodeAndUserId(room, voterId)).thenReturn(voter);
        when(players.findByRoomCodeAndUserId(room, t1)).thenReturn(target1);
        when(players.findByRoomCodeAndUserId(room, t2)).thenReturn(target2);

        when(votes.findByRoomCodeAndPhaseAndDayNumberAndVoterId(room, GamePhase.DAY_ELIMINATION, day, voterId)).thenReturn(Optional.empty());
        when(players.countByRoomCodeAndAliveTrue(room)).thenReturn(1L);
        when(votes.countByRoomCodeAndPhaseAndDayNumber(room, GamePhase.DAY_ELIMINATION, day)).thenReturn(1L);

        Vote v1 = new Vote(room, GamePhase.DAY_ELIMINATION, day, UUID.randomUUID(), t1);
        Vote v2 = new Vote(room, GamePhase.DAY_ELIMINATION, day, UUID.randomUUID(), t2);
        when(votes.findByRoomCodeAndPhaseAndDayNumber(room, GamePhase.DAY_ELIMINATION, day)).thenReturn(List.of(v1, v2));

        VoteRequest req = buildVoteRequest("alice","bob", GamePhase.DAY_ELIMINATION, GameRole.CITIZEN, day);
        VoteResponse resp = service.doVote(room, req);

        assertEquals("VOTE_RECEIVED", resp.getStatus()); // eliminationResult == null -> status remains VOTE_RECEIVED
        assertNull(resp.getEliminatedPlayerId());
        verify(players, never()).save(any());
    }
}
