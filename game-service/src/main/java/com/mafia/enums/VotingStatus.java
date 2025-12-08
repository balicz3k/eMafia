package com.mafia.enums;

/**
 * Status sesji głosowania
 */
public enum VotingStatus {
  /** Głosowanie jest aktywne - gracze mogą oddawać głosy */
  ACTIVE,
  
  /** Głosowanie zakończone normalnie - wszyscy zagłosowali lub wynik został określony */
  COMPLETED,
  
  /** Głosowanie wygasło - upłynął czas */
  EXPIRED
}
