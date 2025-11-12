import React from 'react';
import styles from './VoteProgressBar.module.css';

/**
 * Komponent wyświetlający progress bar głosowania
 * Pokazuje ile osób już zagłosowało
 */
const VoteProgressBar = ({ votesReceived, totalVoters }) => {
  const percentage = totalVoters > 0 ? (votesReceived / totalVoters) * 100 : 0;
  const isComplete = votesReceived >= totalVoters;

  return (
    <div className={styles.voteProgressBar}>
      <div className={styles.progressHeader}>
        <span className={styles.progressLabel}>Votes Cast</span>
        <span className={styles.progressCount}>
          <strong>{votesReceived}</strong> / {totalVoters}
        </span>
      </div>

      <div className={styles.progressBarContainer}>
        <div 
          className={`${styles.progressBarFill} ${isComplete ? styles.complete : ''}`}
          style={{ width: `${percentage}%` }}
        >
          {percentage > 10 && (
            <span className={styles.progressPercentage}>
              {Math.round(percentage)}%
            </span>
          )}
        </div>
      </div>

      {isComplete && (
        <div className={styles.completeMessage}>
          ✓ All players have voted
        </div>
      )}
    </div>
  );
};

export default VoteProgressBar;
