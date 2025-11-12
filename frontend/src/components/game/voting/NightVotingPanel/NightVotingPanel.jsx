import React from 'react';
import VotingTimer from '../VotingTimer/VotingTimer';
import VoteProgressBar from '../VoteProgressBar/VoteProgressBar';
import PlayerVotingList from '../PlayerVotingList/PlayerVotingList';
import styles from './NightVotingPanel.module.css';

/**
 * Panel głosowania nocnego (Mafia)
 * - Wszyscy widzą timer, ale tylko Mafia może głosować
 * - Głosy są tajne (nie widać kto na kogo głosował)
 * - Liczą się tylko głosy Mafii
 * - Remis = losowy wybór
 */
const NightVotingPanel = ({ 
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
  
  const isMafia = currentPlayer?.role === 'MAFIA' || currentPlayer?.assignedRole === 'MAFIA';
  const isAlive = currentPlayer?.isAlive !== false;
  const canVote = isMafia && isAlive && !hasVoted && session?.status === 'ACTIVE';

  // Filtruj graczy - Mafia nie może głosować na siebie nawzajem
  const votablePlayers = players?.filter(p => {
    const playerRole = p.role || p.assignedRole;
    return playerRole !== 'MAFIA';
  }) || [];

  return (
    <div className={styles.nightVotingPanel}>
      {/* Header */}
      <div className={styles.header}>
        <div className={styles.phaseIcon}>🌙</div>
        <div className={styles.headerContent}>
          <h2 className={styles.title}>Night {session.dayNumber}</h2>
          <p className={styles.subtitle}>Mafia Vote - Choose Your Target</p>
        </div>
      </div>

      {/* Instrukcje - różne dla Mafii i Obywateli */}
      {isMafia && isAlive ? (
        <div className={styles.mafiaInstructions}>
          <p className={styles.mafiaTitle}>
            <strong>🎭 You are Mafia!</strong>
          </p>
          <p>
            Choose a citizen to eliminate. Your vote counts towards the Mafia's decision.
            {session.totalEligibleVoters > 1 && (
              <> Coordinate with your fellow Mafia members.</>
            )}
          </p>
          <p className={styles.instructionHighlight}>
            ✓ You can vote. Choose your target wisely!
          </p>
        </div>
      ) : (
        <div className={styles.citizenInstructions}>
          <p className={styles.citizenTitle}>
            <strong>🌙 The town is asleep...</strong>
          </p>
          <p>
            While the citizens sleep, the Mafia is choosing their next victim.
            Wait for the night to pass and the results to be revealed.
          </p>
          {!isAlive && (
            <p className={styles.deadNotice}>
              💀 You are dead. You can observe but cannot participate.
            </p>
          )}
        </div>
      )}

      {/* Timer */}
      <VotingTimer remainingSeconds={remainingTime} />

      {/* Progress Bar */}
      <VoteProgressBar
        votesReceived={session.votesReceived}
        totalVoters={session.totalEligibleVoters}
      />

      {/* Zawartość - różna dla Mafii i Obywateli */}
      {isMafia && isAlive ? (
        <>
          {/* Lista graczy do głosowania - tylko dla Mafii */}
          <PlayerVotingList
            players={votablePlayers}
            onVote={onVote}
            hasVoted={hasVoted}
            canVote={canVote}
            currentUserId={currentUser?.id}
          />

          {/* Informacje o zasadach */}
          <div className={styles.rulesInfo}>
            <h4>📋 Night Voting Rules:</h4>
            <ul>
              <li>Only Mafia members can vote</li>
              <li>Votes are secret - nobody sees who voted for whom</li>
              <li>Only Mafia votes count towards elimination</li>
              <li><strong>In case of a tie, one player is randomly selected</strong></li>
            </ul>
          </div>
        </>
      ) : (
        <div className={styles.waitingArea}>
          <div className={styles.sleepingIcon}>😴</div>
          <p className={styles.waitingText}>
            The citizens are sleeping peacefully...
          </p>
          <p className={styles.waitingSubtext}>
            Waiting for the night to pass
          </p>
          <div className={styles.stars}>
            <span className={styles.star}>⭐</span>
            <span className={styles.star}>✨</span>
            <span className={styles.star}>⭐</span>
            <span className={styles.star}>✨</span>
            <span className={styles.star}>⭐</span>
          </div>
        </div>
      )}
    </div>
  );
};

export default NightVotingPanel;
