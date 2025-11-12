import React from 'react';
import VotingTimer from '../VotingTimer/VotingTimer';
import VoteProgressBar from '../VoteProgressBar/VoteProgressBar';
import VoteVisualization from '../VoteVisualization/VoteVisualization';
import PlayerVotingList from '../PlayerVotingList/PlayerVotingList';
import styles from './DayVotingPanel.module.css';

/**
 * Panel głosowania dziennego
 * - Wszyscy żywi gracze mogą głosować
 * - Głosy są publiczne (widoczne dla wszystkich)
 * - Wykres słupkowy pokazuje wyniki w czasie rzeczywistym
 * - Remis = nikt nie odpada
 */
const DayVotingPanel = ({ 
  session, 
  onVote, 
  hasVoted, 
  currentUser, 
  players,
  remainingTime 
}) => {
  // Znajdź aktualnego gracza
  const currentPlayer = players?.find(
    p => String(p.userId) === String(currentUser?.id)
  );
  
  const isPlayerAlive = currentPlayer?.isAlive !== false;
  const canVote = isPlayerAlive && !hasVoted && session?.status === 'ACTIVE';

  return (
    <div className={styles.dayVotingPanel}>
      {/* Header */}
      <div className={styles.header}>
        <div className={styles.phaseIcon}>☀️</div>
        <div className={styles.headerContent}>
          <h2 className={styles.title}>Day {session.dayNumber}</h2>
          <p className={styles.subtitle}>Town Vote - Eliminate a Suspect</p>
        </div>
      </div>

      {/* Instrukcje */}
      <div className={styles.instructions}>
        <p>
          <strong>🗳️ Voting Phase:</strong> Discuss and vote to eliminate a player you suspect is Mafia.
          {session.totalEligibleVoters > 0 && (
            <> All {session.totalEligibleVoters} players must vote.</>
          )}
        </p>
        {isPlayerAlive ? (
          <p className={styles.instructionHighlight}>
            ✓ You can vote. Choose wisely!
          </p>
        ) : (
          <p className={styles.deadNotice}>
            💀 You are dead. You can observe but cannot vote.
          </p>
        )}
      </div>

      {/* Timer */}
      <VotingTimer remainingSeconds={remainingTime} />

      {/* Progress Bar */}
      <VoteProgressBar
        votesReceived={session.votesReceived}
        totalVoters={session.totalEligibleVoters}
      />

      {/* Wizualizacja głosów - wykres słupkowy */}
      {session.currentResults && session.currentResults.length > 0 && (
        <VoteVisualization
          results={session.currentResults}
          showVoters={true}
          phase={session.phase}
        />
      )}

      {/* Lista graczy do głosowania */}
      <PlayerVotingList
        players={players}
        onVote={onVote}
        hasVoted={hasVoted}
        canVote={canVote}
        currentUserId={currentUser?.id}
      />

      {/* Informacje o remisie */}
      <div className={styles.rulesInfo}>
        <h4>📋 Day Voting Rules:</h4>
        <ul>
          <li>All alive players can vote</li>
          <li>Votes are public - everyone can see who voted for whom</li>
          <li>Player with most votes is eliminated</li>
          <li><strong>In case of a tie, nobody is eliminated</strong></li>
        </ul>
      </div>
    </div>
  );
};

export default DayVotingPanel;
