import React from 'react';
import styles from './PlayerVotingList.module.css';

/**
 * Komponent wyświetlający listę graczy z możliwością głosowania
 */
const PlayerVotingList = ({ 
  players, 
  onVote, 
  hasVoted, 
  canVote, 
  currentUserId 
}) => {
  if (!players || players.length === 0) {
    return (
      <div className={styles.emptyState}>
        <p>No players available to vote for</p>
      </div>
    );
  }

  const handleVote = (targetUserId) => {
    if (!canVote || hasVoted) return;
    onVote(targetUserId);
  };

  return (
    <div className={styles.playerVotingList}>
      <h3 className={styles.listTitle}>Select a player to vote</h3>
      
      <ul className={styles.playerList}>
        {players.map((player) => {
          const isCurrentUser = String(player.userId) === String(currentUserId);
          const isAlive = player.isAlive !== false; // Default to true if not specified
          const isDisabled = !canVote || hasVoted || isCurrentUser || !isAlive;

          return (
            <li 
              key={player.userId} 
              className={`${styles.playerItem} ${isDisabled ? styles.disabled : ''}`}
            >
              <div className={styles.playerInfo}>
                <span className={styles.playerName}>
                  {player.username || player.displayName || 'Unknown Player'}
                </span>
                
                {isCurrentUser && (
                  <span className={styles.badge} style={{ background: '#2196F3' }}>
                    You
                  </span>
                )}
                
                {!isAlive && (
                  <span className={styles.badge} style={{ background: '#666' }}>
                    💀 Dead
                  </span>
                )}
                
                {player.isHost && (
                  <span className={styles.badge} style={{ background: '#FF9800' }}>
                    👑 Host
                  </span>
                )}
              </div>

              <button
                className={styles.voteButton}
                onClick={() => handleVote(player.userId)}
                disabled={isDisabled}
              >
                {hasVoted ? '✓ Voted' : isCurrentUser ? 'You' : 'Vote'}
              </button>
            </li>
          );
        })}
      </ul>

      {hasVoted && (
        <div className={styles.votedMessage}>
          ✓ You have cast your vote. Waiting for other players...
        </div>
      )}

      {!canVote && !hasVoted && (
        <div className={styles.infoMessage}>
          You cannot vote at this time
        </div>
      )}
    </div>
  );
};

export default PlayerVotingList;
