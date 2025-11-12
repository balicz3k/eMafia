import React from 'react';
import styles from './VoteVisualization.module.css';

/**
 * Komponent wizualizacji głosów - wykres słupkowy
 * Pokazuje wyniki głosowania w czasie rzeczywistym
 */
const VoteVisualization = ({ results, showVoters = true, phase }) => {
  if (!results || results.length === 0) {
    return (
      <div className={styles.emptyState}>
        <p>No votes cast yet</p>
      </div>
    );
  }

  // Znajdź maksymalną liczbę głosów dla skalowania
  const maxVotes = Math.max(...results.map(r => r.voteCount || 0), 1);

  // Sortuj wyniki według liczby głosów (malejąco)
  const sortedResults = [...results].sort((a, b) => (b.voteCount || 0) - (a.voteCount || 0));

  /**
   * Zwraca kolor słupka w zależności od liczby głosów
   */
  const getBarColor = (voteCount) => {
    const percentage = (voteCount / maxVotes) * 100;
    
    if (percentage >= 75) {
      return 'linear-gradient(135deg, #F44336, #E53935)'; // Czerwony - najwięcej głosów
    } else if (percentage >= 50) {
      return 'linear-gradient(135deg, #FF9800, #FB8C00)'; // Pomarańczowy
    } else if (percentage >= 25) {
      return 'linear-gradient(135deg, #FFC107, #FFB300)'; // Żółty
    } else {
      return 'linear-gradient(135deg, #4CAF50, #66BB6A)'; // Zielony - mało głosów
    }
  };

  /**
   * Zwraca emoji w zależności od pozycji
   */
  const getPositionEmoji = (index) => {
    if (index === 0 && sortedResults[0].voteCount > 0) return '🎯';
    return '';
  };

  return (
    <div className={styles.voteVisualization}>
      <h3 className={styles.chartTitle}>
        {phase === 'NIGHT_VOTE' ? '🌙 Voting Results (Hidden)' : '☀️ Current Votes'}
      </h3>

      <div className={styles.chartContainer}>
        {sortedResults.map((result, index) => {
          const percentage = maxVotes > 0 ? (result.voteCount / maxVotes) * 100 : 0;
          const hasVotes = result.voteCount > 0;

          return (
            <div key={result.targetUserId} className={styles.chartRow}>
              <div className={styles.playerLabel}>
                <span className={styles.position}>{getPositionEmoji(index)}</span>
                <span className={styles.playerName}>{result.targetUsername}</span>
              </div>

              <div className={styles.barWrapper}>
                <div className={styles.barContainer}>
                  <div
                    className={`${styles.barFill} ${hasVotes ? styles.animated : ''}`}
                    style={{
                      width: `${percentage}%`,
                      background: getBarColor(result.voteCount)
                    }}
                  >
                    {hasVotes && (
                      <span className={styles.voteCount}>
                        {result.voteCount} {result.voteCount === 1 ? 'vote' : 'votes'}
                      </span>
                    )}
                  </div>
                </div>

                {!hasVotes && (
                  <span className={styles.noVotes}>No votes</span>
                )}
              </div>

              {/* Lista głosujących - tylko dla dziennego głosowania */}
              {showVoters && result.voters && result.voters.length > 0 && (
                <div className={styles.votersList}>
                  <span className={styles.votersLabel}>Voted by:</span>
                  <div className={styles.voterBadges}>
                    {result.voters.map(voter => (
                      <span key={voter.voterId} className={styles.voterBadge}>
                        {voter.voterUsername}
                      </span>
                    ))}
                  </div>
                </div>
              )}
            </div>
          );
        })}
      </div>

      {/* Legenda */}
      <div className={styles.legend}>
        <div className={styles.legendItem}>
          <div className={styles.legendColor} style={{ background: 'linear-gradient(135deg, #F44336, #E53935)' }}></div>
          <span>Most votes</span>
        </div>
        <div className={styles.legendItem}>
          <div className={styles.legendColor} style={{ background: 'linear-gradient(135deg, #4CAF50, #66BB6A)' }}></div>
          <span>Least votes</span>
        </div>
      </div>
    </div>
  );
};

export default VoteVisualization;
