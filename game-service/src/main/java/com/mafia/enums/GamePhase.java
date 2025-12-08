package com.mafia.enums;

/**
 * Fazy gry Mafia
 */
public enum GamePhase {
  /** Dyskusja w ciągu dnia (opcjonalna - future feature) */
  DAY_DISCUSSION,
  
  /** Głosowanie dzienne - wszyscy żywi gracze głosują publicznie */
  DAY_VOTE,
  
  /** Głosowanie nocne - mafia wybiera ofiarę */
  NIGHT_VOTE,
  
  /** Gra zakończona */
  GAME_OVER
}
