package com.mafia.services;

import com.mafia.models.*;
import com.mafia.repositories.GameVoteRepository;
import com.mafia.repositories.PlayerGameStateRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GameEngineService {

    private final PlayerGameStateRepository players;
    private final GameVoteRepository votes;
    private final Random random = new Random();

    public GameEngineService(PlayerGameStateRepository players, GameVoteRepository votes) {
        this.players = players;
        this.votes = votes;
    }

    @Transactional
    public void startGame(String roomCode, List<UUID> userIds) {
        int mafiaCount = Math.max(1, Math.round(userIds.size() * 0.25f));
        var shuffled = new ArrayList<>(userIds);
        Collections.shuffle(shuffled, random);
        var mafiaSet = new HashSet<>(shuffled.subList(0, mafiaCount));

        votes.deleteByRoomCodeAndPhase(roomCode, GamePhase.DAY_ELIMINATION);
        votes.deleteByRoomCodeAndPhase(roomCode, GamePhase.NIGHT_VOTE);

        for (UUID uid : userIds) {
            var existing = players.findByRoomCodeAndUserId(roomCode, uid);
            if (existing == null)
                existing = new PlayerGameState();
            existing.setRoomCode(roomCode);
            existing.setUserId(uid);
            existing.setGameRole(mafiaSet.contains(uid) ? GameRole.MAFIA : GameRole.CITIZEN);
            existing.setAlive(true);
            existing.setSuspended(false);
            players.save(existing);
        }
    }

    @Transactional
    public void submitVoteRequireAll(String roomCode, UUID voterId, GamePhase phase, UUID targetUserId) {
        var voter = players.findByRoomCodeAndUserId(roomCode, voterId);
        if (voter == null || !voter.isAlive())
            throw new IllegalStateException("Voter not alive/in room");

        var target = players.findByRoomCodeAndUserId(roomCode, targetUserId);
        if (target == null || !target.isAlive())
            throw new IllegalStateException("Target not alive/in room");

        var existing = votes.findByRoomCodeAndPhaseAndVoterId(roomCode, phase, voterId);
        GameVote v = existing.orElseGet(GameVote::new);
        v.setRoomCode(roomCode);
        v.setPhase(phase);
        v.setVoterId(voterId);
        v.setTargetUserId(targetUserId);
        votes.save(v);

        long totalAlive = players.countByRoomCodeAndAliveTrue(roomCode);
        long cast = votes.countByRoomCodeAndPhase(roomCode, phase);
        if (cast >= totalAlive) {
            if (phase == GamePhase.DAY_ELIMINATION) {
                resolveDayElimination(roomCode);
            } else if (phase == GamePhase.NIGHT_VOTE) {
                resolveNightKill(roomCode);
            }
        }
    }

    @Transactional
    public Optional<UUID> resolveDayElimination(String roomCode) {
        var list = votes.findByRoomCodeAndPhase(roomCode, GamePhase.DAY_ELIMINATION);
        if (list.isEmpty())
            return Optional.empty();

        Map<UUID, Long> counts = list.stream()
                .collect(Collectors.groupingBy(GameVote::getTargetUserId, Collectors.counting()));

        long max = counts.values().stream().mapToLong(Long::longValue).max().orElse(0);
        var top = counts.entrySet().stream()
                .filter(e -> e.getValue() == max)
                .map(Map.Entry::getKey)
                .toList();

        if (top.size() == 1) {
            eliminate(roomCode, top.get(0));
            return Optional.of(top.get(0));
        }
        return Optional.empty();
    }

    @Transactional
    public Optional<UUID> resolveNightKill(String roomCode) {
        var list = votes.findByRoomCodeAndPhase(roomCode, GamePhase.NIGHT_VOTE);
        if (list.isEmpty())
            return Optional.empty();

        var mafiaIds = players.findByRoomCodeAndAliveTrueAndRole(roomCode, GameRole.MAFIA)
                .stream().map(PlayerGameState::getUserId).collect(Collectors.toSet());

        Map<UUID, Long> counts = list.stream()
                .filter(v -> mafiaIds.contains(v.getVoterId()))
                .collect(Collectors.groupingBy(GameVote::getTargetUserId, Collectors.counting()));

        if (counts.isEmpty())
            return Optional.empty();

        long max = counts.values().stream().mapToLong(Long::longValue).max().orElse(0);
        var top = counts.entrySet().stream()
                .filter(e -> e.getValue() == max)
                .map(Map.Entry::getKey)
                .toList();

        UUID victim = top.size() == 1 ? top.get(0) : top.get(random.nextInt(top.size()));
        eliminate(roomCode, victim);
        return Optional.of(victim);
    }

    @Transactional
    protected void eliminate(String roomCode, UUID userId) {
        var p = players.findByRoomCodeAndUserId(roomCode, userId);
        if (p != null && p.isAlive()) {
            p.setAlive(false);
            players.save(p);
        }
    }
}