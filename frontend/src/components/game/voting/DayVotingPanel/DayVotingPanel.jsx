import React from 'react';
import VotingTimer from '../VotingTimer/VotingTimer';
import VoteProgressBar from '../VoteProgressBar/VoteProgressBar';
import PlayerVotingList from '../PlayerVotingList/PlayerVotingList';
import RolePanel from '../../panel/rolePanel/RolePanel';
import styles from './DayVotingPanel.module.css';

/**
 * Panel głosowania dziennego
 * - Wszyscy żywi gracze mogą głosować
 * - Struktura analogiczna do NightVotingPanel
 * - Minimalistyczny design
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
  
  const isAlive = currentPlayer?.isAlive !== false;
  const canVote = isAlive && !hasVoted && session?.status === 'ACTIVE';

  // Wszyscy widzą wszystkich żywych graczy (oprócz siebie)
  const votablePlayers = players?.filter(p => {
    return p.isAlive !== false && String(p.userId) !== String(currentUser?.id);
  }) || [];

  return (
    <div className={styles.dayVotingPanel}>
      {/* Header */}
      <div className={styles.header}>
        <div className={styles.phaseIcon}>☀️</div>
        <h2 className={styles.title}>Day {session.dayNumber}</h2>
        <p className={styles.subtitle}>Town Vote</p>
      </div>

      {/* Instrukcje */}
      <div className={styles.instructions}>
        <p>
          Discuss and vote to eliminate a player you suspect is Mafia. Choose carefully - your vote matters.
        </p>
        {!isAlive && (
          <p className={styles.deadNotice}>
            💀 You are eliminated. You can observe but cannot vote.
          </p>
        )}
      </div>

      {/* RolePanel */}
      <RolePanel roomCode={session.roomCode} />

      {/* Timer */}
      <VotingTimer remainingSeconds={remainingTime} />

      {/* Progress Bar */}
      <VoteProgressBar
        votesReceived={session.votesReceived}
        totalVoters={session.totalEligibleVoters}
      />

      {/* Lista graczy */}
      <PlayerVotingList
        players={votablePlayers}
        onVote={onVote}
        hasVoted={hasVoted}
        canVote={canVote}
        currentUserId={currentUser?.id}
      />

      {/* Zasady */}
      <div className={styles.rulesInfo}>
        <h4>📋 Day Voting Rules:</h4>
        <ul>
          <li>All alive players can vote</li>
          <li>Player with most votes is eliminated</li>
          <li>In case of a tie, nobody is eliminated</li>
        </ul>
      </div>
    </div>
  );
};

export default DayVotingPanel;
