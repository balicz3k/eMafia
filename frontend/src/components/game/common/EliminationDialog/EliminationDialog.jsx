import React from 'react';
import styles from './EliminationDialog.module.css';

/**
 * Dialog wyświetlający informację o eliminacji gracza.
 * Nie blokuje rozgrywki - timer i gra działają pod spodem.
 * Użytkownik może zamknąć przyciskiem X lub OK.
 */
const EliminationDialog = ({ 
  isOpen, 
  onClose, 
  eliminatedUsername, 
  phase, // 'NIGHT_VOTE' lub 'DAY_VOTE'
  isTie = false,
  resultType = 'ELIMINATION'
}) => {
  if (!isOpen) return null;

  const isNight = phase === 'NIGHT_VOTE';
  const hasElimination = eliminatedUsername && resultType !== 'NO_ELIMINATION' && resultType !== 'EXPIRED_NO_VOTES';

  const getTitle = () => {
    if (resultType === 'EXPIRED_NO_VOTES') {
      return isNight ? '🌙 Mafia Eliminated' : '☀️ Town Voted';
    }
    if (isTie && !hasElimination) {
      return '⚖️ Tie Vote!';
    }
    if (!hasElimination) {
      return isNight ? '🌙 Mafia Eliminated' : '☀️ Town Voted';
    }
    return isNight ? '🌙 Mafia Eliminated' : '☀️ Town Voted';
  };

  const getMessage = () => {
    if (resultType === 'EXPIRED_NO_VOTES') {
      return isNight 
        ? 'No one was killed tonight.'
        : 'No one was eliminated.';
    }
    if (isTie && !hasElimination) {
      return 'The vote ended in a tie. No one was eliminated.';
    }
    if (!hasElimination) {
      return isNight 
        ? 'No one was killed tonight.'
        : 'No one was eliminated.';
    }
    return null; // Message will be displayed in the elimination card
  };

  const getIcon = () => {
    if (!hasElimination) {
      return isNight ? '🌙' : '☀️';
    }
    return '💀';
  };

  const getEliminationLabel = () => {
    return isNight ? 'Mafia eliminated:' : 'Town voted out:';
  };

  return (
    <div className={styles.overlay}>
      <div className={`${styles.dialog} ${isNight ? styles.nightDialog : styles.dayDialog}`}>
        {/* Przycisk X do zamknięcia */}
        <button className={styles.closeButton} onClick={onClose} aria-label="Close">
          ✕
        </button>
        
        <div className={styles.iconContainer}>
          <span className={styles.icon}>{getIcon()}</span>
        </div>
        
        <h2 className={styles.title}>{getTitle()}</h2>
        
        {hasElimination ? (
          <div className={styles.eliminationInfo}>
            <p className={styles.eliminationLabel}>{getEliminationLabel()}</p>
            <div className={styles.eliminatedPlayer}>
              <span className={styles.skull}>💀</span>
              <span className={styles.playerName}>{eliminatedUsername}</span>
            </div>
          </div>
        ) : (
          <p className={styles.message}>{getMessage()}</p>
        )}
        
        {isTie && hasElimination && (
          <p className={styles.tieNote}>
            (Tie was resolved randomly)
          </p>
        )}
        
        <button className={styles.okButton} onClick={onClose}>
          OK
        </button>
      </div>
    </div>
  );
};

export default EliminationDialog;
