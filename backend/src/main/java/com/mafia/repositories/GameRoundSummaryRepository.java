package com.mafia.repositories;

import com.mafia.databaseModels.GameRoundSummary;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRoundSummaryRepository extends JpaRepository<GameRoundSummary, UUID> {}
