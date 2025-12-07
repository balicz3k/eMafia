import React from 'react';
import VotingTimer from '../VotingTimer/VotingTimer';
import VoteProgressBar from '../VoteProgressBar/VoteProgressBar';
import PlayerVotingList from '../PlayerVotingList/PlayerVotingList';
import RolePanel from '../../panel/rolePanel/RolePanel';
import styles from './NightVotingPanel.module.css';

/**
 * Panel głosowania nocnego (Mafia)
 * - WSZYSCY widzą tę samą listę graczy
 * - WSZYSCY mogą głosować (backend liczy tylko głosy mafii)
 * - Rola ukryta za przyciskiem "Check My Role"
 * - Głosy są tajne
 */
const NightVotingPanel = ({ 
  session, 
  onVote, 
  hasVoted, 
  currentUser, 
  players,
  remainingTime 
}) => {
  // Helper funkcja sprawdzająca czy gracz żyje (obsługuje oba formaty z backendu)
  const checkIsAlive = (player) => {
    if (player === null || player === undefined) return false;
    // Backend może wysyłać 'isAlive' lub 'alive' w zależności od konfiguracji Jackson
    const alive = player.isAlive !== undefined ? player.isAlive : player.alive;
    return alive === true;
  };

  // Znajdź aktualnego gracza
  const currentPlayer = players?.find(
    p => String(p.userId) === String(currentUser?.id)
  );
  
  const isAlive = checkIsAlive(currentPlayer);
  const canVote = isAlive && !hasVoted && session?.status === 'ACTIVE';

  // WSZYSCY widzą tylko żywych graczy (oprócz siebie)
  const votablePlayers = players?.filter(p => {
    return checkIsAlive(p) && String(p.userId) !== String(currentUser?.id);
  }) || [];

  return (
    <div className={styles.nightVotingPanel}>
      {/* Header - jednolity dla wszystkich */}
      <div className={styles.header}>
        <div className={styles.phaseIcon}>🌙</div>
        <h2 className={styles.title}>Night {session.dayNumber}</h2>
        <p className={styles.subtitle}>Cast Your Vote</p>
      </div>

      {/* Instrukcje - jednolite */}
      <div className={styles.instructions}>
        <p>
          The night has fallen. Each player must cast their vote. Choose wisely - your decision matters.
        </p>
        {!isAlive && (
          <p className={styles.deadNotice}>
            💀 You are eliminated. You can observe but cannot vote.
          </p>
        )}
      </div>

      {/* RolePanel - ukryta za przyciskiem */}
      <RolePanel roomCode={session.roomCode} />

      {/* Timer */}
      <VotingTimer remainingSeconds={remainingTime} />

      {/* Progress Bar */}
      <VoteProgressBar
        votesReceived={session.votesReceived}
        totalVoters={session.totalEligibleVoters}
      />

      {/* Lista graczy - WSZYSCY widzą tę samą listę */}
      <PlayerVotingList
        players={votablePlayers}
        onVote={onVote}
        hasVoted={hasVoted}
        canVote={canVote}
        currentUserId={currentUser?.id}
      />

      {/* Zasady - uproszczone */}
      <div className={styles.rulesInfo}>
        <h4>📋 Night Voting Rules:</h4>
        <ul>
          <li>All players cast their votes</li>
          <li>Votes are secret</li>
          <li>Results revealed at dawn</li>
        </ul>
      </div>
    </div>
  );
};

export default NightVotingPanel;
