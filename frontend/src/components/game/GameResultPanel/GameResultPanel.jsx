import React from 'react';
import { useNavigate } from 'react-router-dom';
import styles from './GameResultPanel.module.css';

/**
 * Panel wyświetlający wyniki gry po jej zakończeniu
 * Pokazuje zwycięzcę, statystyki i listę graczy z ich rolami
 */
const GameResultPanel = ({ gameResult, roomCode }) => {
  const navigate = useNavigate();

  if (!gameResult) {
    return null;
  }

  const isMafiaWin = gameResult.winner === 'MAFIA';
  const winnerTeam = isMafiaWin ? 'Mafia' : 'Citizens';

  const handleBackToLobby = () => {
    if (roomCode) {
      navigate(`/room/${roomCode}`);
    } else {
      navigate('/dashboard');
    }
  };

  // Grupuj graczy według ról
  const mafiaPlayers = gameResult.players?.filter(p => p.role === 'MAFIA') || [];
  const citizenPlayers = gameResult.players?.filter(p => p.role === 'CITIZEN') || [];

  return (
    <div className={styles.gameResultPanel}>
      {/* Header z wynikiem */}
      <div className={`${styles.header} ${isMafiaWin ? styles.mafiaWin : styles.citizensWin}`}>
        <div className={styles.winnerIcon}>
          {isMafiaWin ? '🎭' : '👥'}
        </div>
        <h1 className={styles.winnerTitle}>
          {winnerTeam} Win!
        </h1>
        <p className={styles.winnerSubtitle}>
          {isMafiaWin 
            ? 'The Mafia has taken over the town!' 
            : 'The Citizens have eliminated all Mafia members!'}
        </p>
      </div>

      {/* Statystyki gry */}
      <div className={styles.statsSection}>
        <h2 className={styles.sectionTitle}>Game Statistics</h2>
        <div className={styles.statsGrid}>
          <div className={styles.statCard}>
            <div className={styles.statIcon}>📅</div>
            <div className={styles.statValue}>{gameResult.totalDays}</div>
            <div className={styles.statLabel}>Days Survived</div>
          </div>
          <div className={styles.statCard}>
            <div className={styles.statIcon}>👥</div>
            <div className={styles.statValue}>{gameResult.players?.length || 0}</div>
            <div className={styles.statLabel}>Total Players</div>
          </div>
          <div className={styles.statCard}>
            <div className={styles.statIcon}>🎭</div>
            <div className={styles.statValue}>{mafiaPlayers.length}</div>
            <div className={styles.statLabel}>Mafia Members</div>
          </div>
          <div className={styles.statCard}>
            <div className={styles.statIcon}>
              {isMafiaWin ? '💀' : '✓'}
            </div>
            <div className={styles.statValue}>
              {gameResult.players?.filter(p => p.isAlive).length || 0}
            </div>
            <div className={styles.statLabel}>Survivors</div>
          </div>
        </div>
      </div>

      {/* Lista graczy według ról */}
      <div className={styles.playersSection}>
        <h2 className={styles.sectionTitle}>Players & Roles</h2>

        {/* Mafia */}
        <div className={styles.teamSection}>
          <h3 className={styles.teamTitle}>
            <span className={styles.teamIcon}>🎭</span>
            Mafia Team
          </h3>
          <div className={styles.playersList}>
            {mafiaPlayers.map(player => (
              <div 
                key={player.userId} 
                className={`${styles.playerCard} ${styles.mafiaCard} ${!player.isAlive ? styles.eliminated : ''}`}
              >
                <div className={styles.playerInfo}>
                  <span className={styles.playerName}>{player.username}</span>
                  <span className={styles.playerRole}>Mafia</span>
                </div>
                <div className={styles.playerStatus}>
                  {player.isAlive ? (
                    <span className={styles.survived}>✓ Survived</span>
                  ) : (
                    <span className={styles.dead}>💀 Eliminated</span>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Citizens */}
        <div className={styles.teamSection}>
          <h3 className={styles.teamTitle}>
            <span className={styles.teamIcon}>👥</span>
            Citizens Team
          </h3>
          <div className={styles.playersList}>
            {citizenPlayers.map(player => (
              <div 
                key={player.userId} 
                className={`${styles.playerCard} ${styles.citizenCard} ${!player.isAlive ? styles.eliminated : ''}`}
              >
                <div className={styles.playerInfo}>
                  <span className={styles.playerName}>{player.username}</span>
                  <span className={styles.playerRole}>Citizen</span>
                </div>
                <div className={styles.playerStatus}>
                  {player.isAlive ? (
                    <span className={styles.survived}>✓ Survived</span>
                  ) : (
                    <span className={styles.dead}>💀 Eliminated</span>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Przyciski akcji */}
      <div className={styles.actionsSection}>
        <button 
          className={styles.primaryButton}
          onClick={handleBackToLobby}
        >
          Back to Lobby
        </button>
        <button 
          className={styles.secondaryButton}
          onClick={() => navigate('/dashboard')}
        >
          Dashboard
        </button>
      </div>
    </div>
  );
};

export default GameResultPanel;
