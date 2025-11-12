import React, { useState } from 'react';
import styles from './GameRoomSettings.module.css';

/**
 * Komponent ustawień gry dla hosta
 * Pozwala skonfigurować liczbę mafii i czas dyskusji przed rozpoczęciem gry
 */
const GameRoomSettings = ({ onStartGame, isStarting, minPlayers = 3 }) => {
  const [mafiaCount, setMafiaCount] = useState(1);
  const [discussionTime, setDiscussionTime] = useState(120);
  const [showAdvanced, setShowAdvanced] = useState(false);

  const handleStartGame = () => {
    onStartGame({
      mafiaCount,
      discussionTimeSeconds: discussionTime
    });
  };

  // Opcje liczby mafii (1-5)
  const mafiaOptions = [1, 2, 3, 4, 5];

  // Opcje czasu dyskusji
  const timeOptions = [
    { value: 30, label: '30 seconds' },
    { value: 60, label: '1 minute' },
    { value: 90, label: '1.5 minutes' },
    { value: 120, label: '2 minutes' },
    { value: 180, label: '3 minutes' },
    { value: 240, label: '4 minutes' },
    { value: 300, label: '5 minutes' },
    { value: 420, label: '7 minutes' },
    { value: 600, label: '10 minutes' }
  ];

  return (
    <div className={styles.gameRoomSettings}>
      <div className={styles.header}>
        <h3 className={styles.title}>⚙️ Game Settings</h3>
        <p className={styles.subtitle}>Configure your game before starting</p>
      </div>

      <div className={styles.settingsGrid}>
        {/* Liczba Mafii */}
        <div className={styles.settingCard}>
          <div className={styles.settingHeader}>
            <label className={styles.settingLabel}>
              <span className={styles.icon}>🎭</span>
              Number of Mafia
            </label>
            <span className={styles.settingValue}>{mafiaCount}</span>
          </div>
          
          <div className={styles.settingDescription}>
            How many Mafia members will be in the game
          </div>

          <div className={styles.buttonGroup}>
            {mafiaOptions.map(count => (
              <button
                key={count}
                className={`${styles.optionButton} ${mafiaCount === count ? styles.active : ''}`}
                onClick={() => setMafiaCount(count)}
                disabled={isStarting}
              >
                {count}
              </button>
            ))}
          </div>

          <div className={styles.settingHint}>
            💡 Recommended: 1 Mafia per 3-4 players
          </div>
        </div>

        {/* Czas dyskusji */}
        <div className={styles.settingCard}>
          <div className={styles.settingHeader}>
            <label className={styles.settingLabel}>
              <span className={styles.icon}>⏱️</span>
              Discussion Time
            </label>
            <span className={styles.settingValue}>
              {discussionTime < 60 
                ? `${discussionTime}s` 
                : `${Math.floor(discussionTime / 60)}m ${discussionTime % 60 > 0 ? `${discussionTime % 60}s` : ''}`
              }
            </span>
          </div>

          <div className={styles.settingDescription}>
            Time limit for each voting phase
          </div>

          <select
            className={styles.selectInput}
            value={discussionTime}
            onChange={(e) => setDiscussionTime(Number(e.target.value))}
            disabled={isStarting}
          >
            {timeOptions.map(option => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>

          <div className={styles.settingHint}>
            💡 Recommended: 2-3 minutes for balanced gameplay
          </div>
        </div>
      </div>

      {/* Zaawansowane ustawienia (opcjonalne - na przyszłość) */}
      <div className={styles.advancedSection}>
        <button
          className={styles.advancedToggle}
          onClick={() => setShowAdvanced(!showAdvanced)}
          disabled={isStarting}
        >
          {showAdvanced ? '▼' : '▶'} Advanced Settings
        </button>

        {showAdvanced && (
          <div className={styles.advancedContent}>
            <p className={styles.comingSoon}>
              🚧 Additional roles and settings coming soon!
            </p>
            <ul className={styles.futureFeatures}>
              <li>🕵️ Detective role</li>
              <li>👨‍⚕️ Doctor role</li>
              <li>💬 Discussion phase before voting</li>
              <li>🎲 Random role distribution</li>
            </ul>
          </div>
        )}
      </div>

      {/* Przycisk Start Game */}
      <div className={styles.startSection}>
        <button
          className={styles.startButton}
          onClick={handleStartGame}
          disabled={isStarting}
        >
          {isStarting ? (
            <>
              <span className={styles.spinner}></span>
              Starting Game...
            </>
          ) : (
            <>
              <span className={styles.startIcon}>🎮</span>
              Start Game
            </>
          )}
        </button>

        <div className={styles.startHint}>
          Minimum {minPlayers} players required to start
        </div>
      </div>

      {/* Podsumowanie */}
      <div className={styles.summary}>
        <h4 className={styles.summaryTitle}>Game Summary</h4>
        <div className={styles.summaryGrid}>
          <div className={styles.summaryItem}>
            <span className={styles.summaryLabel}>Mafia:</span>
            <span className={styles.summaryValue}>{mafiaCount}</span>
          </div>
          <div className={styles.summaryItem}>
            <span className={styles.summaryLabel}>Discussion Time:</span>
            <span className={styles.summaryValue}>
              {discussionTime < 60 
                ? `${discussionTime}s` 
                : `${Math.floor(discussionTime / 60)} min`
              }
            </span>
          </div>
          <div className={styles.summaryItem}>
            <span className={styles.summaryLabel}>Game Mode:</span>
            <span className={styles.summaryValue}>Classic</span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default GameRoomSettings;
