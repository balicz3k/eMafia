import React from 'react';
import styles from './SpectatorView.module.css';

/**
 * Widok dla martwych graczy - tryb obserwatora
 * Gracz widzi że jest martwy, ale może śledzić rozgrywkę
 * W przyszłości może zostać ożywiony
 */
const SpectatorView = ({ 
  currentPlayer,
  players,
  currentPhase,
  dayNumber,
  lastElimination
}) => {
  // Policz żywych graczy
  const alivePlayers = players?.filter(p => {
    const alive = p.isAlive !== undefined ? p.isAlive : p.alive;
    return alive === true;
  }) || [];

  const isNight = currentPhase === 'NIGHT_VOTE';

  return (
    <div className={styles.spectatorView}>
      {/* Header */}
      <div className={`${styles.header} ${isNight ? styles.nightHeader : styles.dayHeader}`}>
        <div className={styles.ghostIcon}>👻</div>
        <h1 className={styles.title}>You Are Eliminated</h1>
        <p className={styles.subtitle}>But not all hope is lost...</p>
      </div>

      {/* Main message */}
      <div className={styles.messageCard}>
        <div className={styles.cardIcon}>💀</div>
        <h2 className={styles.cardTitle}>Spectator Mode</h2>
        <p className={styles.cardText}>
          You have been eliminated from the game, but you can still watch how the story unfolds.
          Keep track of who's winning - the Mafia or the Citizens!
        </p>
        <div className={styles.hopeMessage}>
          <span className={styles.hopeIcon}>✨</span>
          <p>In some games, eliminated players can be revived. Stay tuned!</p>
        </div>
      </div>

      {/* Current game state */}
      <div className={styles.gameStateCard}>
        <h3 className={styles.sectionTitle}>
          {isNight ? '🌙' : '☀️'} Current Phase
        </h3>
        <div className={styles.phaseInfo}>
          <span className={styles.phaseLabel}>
            {isNight ? `Night ${dayNumber}` : `Day ${dayNumber}`}
          </span>
          <span className={styles.phaseDescription}>
            {isNight 
              ? 'The Mafia is choosing their target...' 
              : 'The town is voting...'}
          </span>
        </div>
      </div>

      {/* Players status */}
      <div className={styles.playersCard}>
        <h3 className={styles.sectionTitle}>👥 Players Status</h3>
        <div className={styles.playersStats}>
          <div className={styles.statItem}>
            <span className={styles.statNumber}>{alivePlayers.length}</span>
            <span className={styles.statLabel}>Alive</span>
          </div>
          <div className={styles.statItem}>
            <span className={styles.statNumber}>{(players?.length || 0) - alivePlayers.length}</span>
            <span className={styles.statLabel}>Eliminated</span>
          </div>
        </div>
        
        <div className={styles.playersList}>
          {players?.map(player => {
            const isAlive = player.isAlive !== undefined ? player.isAlive : player.alive;
            const isCurrentPlayer = String(player.userId) === String(currentPlayer?.userId);
            
            return (
              <div 
                key={player.userId}
                className={`${styles.playerItem} ${!isAlive ? styles.deadPlayer : ''} ${isCurrentPlayer ? styles.currentPlayer : ''}`}
              >
                <span className={styles.playerStatus}>
                  {isAlive ? '🟢' : '💀'}
                </span>
                <span className={styles.playerName}>
                  {player.username || player.gameNick}
                  {isCurrentPlayer && ' (You)'}
                </span>
              </div>
            );
          })}
        </div>
      </div>

      {/* Last elimination info */}
      {lastElimination && (
        <div className={styles.eliminationCard}>
          <h3 className={styles.sectionTitle}>📢 Last Elimination</h3>
          <p className={styles.eliminationText}>
            <span className={styles.eliminatedName}>{lastElimination.username}</span>
            {lastElimination.phase === 'NIGHT_VOTE' 
              ? ' was killed by the Mafia'
              : ' was voted out by the town'}
          </p>
        </div>
      )}

      {/* Footer message */}
      <div className={styles.footer}>
        <p className={styles.footerText}>
          🎮 The game will notify you when it ends
        </p>
      </div>
    </div>
  );
};

export default SpectatorView;
