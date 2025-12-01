import React, { useState } from 'react';
import styles from './PlayerVotingList.module.css';

/**
 * Komponent wyświetlający listę graczy z możliwością głosowania
 * Dwuetapowy proces: wybór gracza → submit vote
 */
const PlayerVotingList = ({ 
  players, 
  onVote, 
  hasVoted, 
  canVote, 
  currentUserId 
}) => {
  const [selectedPlayerId, setSelectedPlayerId] = useState(null);

  if (!players || players.length === 0) {
    return (
      <div className={styles.emptyState}>
        <p>No players available to vote for</p>
      </div>
    );
  }

  const handleSelectPlayer = (playerId) => {
    if (!canVote || hasVoted) return;
    setSelectedPlayerId(playerId);
  };

  const handleSubmitVote = () => {
    if (!selectedPlayerId || !canVote || hasVoted) return;
    onVote(selectedPlayerId);
    setSelectedPlayerId(null);
  };

  return (
    <div className={styles.playerVotingList}>
      <h3 className={styles.listTitle}>Select a player</h3>
      
      <ul className={styles.playerList}>
        {players.map((player) => {
          const isSelected = selectedPlayerId === player.userId;
          const isCurrentUser = String(player.userId) === String(currentUserId);
          const isAlive = player.isAlive !== false;
          const isDisabled = !canVote || hasVoted || isCurrentUser || !isAlive;

          return (
            <li 
              key={player.userId} 
              className={`
                ${styles.playerItem} 
                ${isSelected ? styles.selected : ''}
                ${isDisabled ? styles.disabled : ''}
              `}
              onClick={() => handleSelectPlayer(player.userId)}
            >
              <div className={styles.playerInfo}>
                <span className={styles.playerName}>
                  {player.username || player.displayName || 'Unknown Player'}
                </span>
                
                {isCurrentUser && (
                  <span className={styles.badge}>You</span>
                )}
                
                {!isAlive && (
                  <span className={styles.badge}>💀</span>
                )}
              </div>

              {isSelected && <span className={styles.selectedIcon}>✓</span>}
            </li>
          );
        })}
      </ul>

      {!hasVoted && selectedPlayerId && (
        <button
          className={styles.submitButton}
          onClick={handleSubmitVote}
          disabled={!canVote}
        >
          Submit Vote
        </button>
      )}

      {hasVoted && (
        <div className={styles.successMessage}>
          ✓ Vote submitted successfully
        </div>
      )}
    </div>
  );
};

export default PlayerVotingList;
