import React, { useEffect, useState } from 'react';
import styles from './VotingTimer.module.css';

/**
 * Komponent wyświetlający timer głosowania
 * Zmienia kolor w zależności od pozostałego czasu
 */
const VotingTimer = ({ remainingSeconds }) => {
  const [timeLeft, setTimeLeft] = useState(remainingSeconds || 0);

  useEffect(() => {
    if (remainingSeconds !== null && remainingSeconds !== undefined) {
      setTimeLeft(remainingSeconds);
    }
  }, [remainingSeconds]);

  const minutes = Math.floor(timeLeft / 60);
  const seconds = timeLeft % 60;

  const getTimerColor = () => {
    if (timeLeft > 60) return '#4CAF50'; // Zielony - dużo czasu
    if (timeLeft > 30) return '#FF9800'; // Pomarańczowy - średnio czasu
    return '#F44336'; // Czerwony - mało czasu
  };

  const getTimerClass = () => {
    if (timeLeft > 60) return styles.timerNormal;
    if (timeLeft > 30) return styles.timerWarning;
    return styles.timerDanger;
  };

  return (
    <div className={styles.votingTimer}>
      <div 
        className={`${styles.timerDisplay} ${getTimerClass()}`}
        style={{ color: getTimerColor() }}
      >
        <span className={styles.timerIcon}>⏱️</span>
        <span className={styles.timerText}>
          {String(minutes).padStart(2, '0')}:{String(seconds).padStart(2, '0')}
        </span>
      </div>
      <div className={styles.timerLabel}>Time remaining</div>
    </div>
  );
};

export default VotingTimer;
