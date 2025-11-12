import React, { useEffect, useState, useRef } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import toast from '../../../../utils/notifications';
import httpClient from '../../../../utils/httpClient';
import DayVotingPanel from '../DayVotingPanel/DayVotingPanel';
import NightVotingPanel from '../NightVotingPanel/NightVotingPanel';
import styles from './VotingPhaseContainer.module.css';

/**
 * Główny kontener dla fazy głosowania
 * Zarządza WebSocket, stanem głosowania i komunikacją z backendem
 */
const VotingPhaseContainer = ({ gameId, roomCode, currentUser, players }) => {
  const [votingSession, setVotingSession] = useState(null);
  const [hasVoted, setHasVoted] = useState(false);
  const [remainingTime, setRemainingTime] = useState(null);
  const [loading, setLoading] = useState(true);
  const stompClient = useRef(null);

  // Fetch voting session on mount
  useEffect(() => {
    fetchVotingSession();
  }, [gameId]);

  // Connect WebSocket
  useEffect(() => {
    if (roomCode) {
      connectWebSocket();
    }

    return () => {
      disconnectWebSocket();
    };
  }, [roomCode]);

  /**
   * Pobiera aktualną sesję głosowania z backendu
   */
  const fetchVotingSession = async () => {
    try {
      setLoading(true);
      const response = await httpClient.get(`/api/games/${gameId}/voting/current`);
      
      if (response.status === 204) {
        // Brak aktywnej sesji
        setVotingSession(null);
      } else {
        setVotingSession(response.data);
        setRemainingTime(response.data.remainingTimeSeconds);
      }
    } catch (error) {
      console.error('Error fetching voting session:', error);
      if (error.response?.status !== 404) {
        toast.error('Failed to load voting session');
      }
    } finally {
      setLoading(false);
    }
  };

  /**
   * Łączy się z WebSocket
   */
  const connectWebSocket = () => {
    const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';
    const socket = new SockJS(`${API_BASE_URL}/ws`);
    
    const client = new Client({
      webSocketFactory: () => socket,
      debug: (str) => {
        console.log('STOMP Debug:', str);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        console.log('WebSocket connected');

        // Subscribe to voting updates
        client.subscribe(`/topic/game/${roomCode}/voting`, (message) => {
          const update = JSON.parse(message.body);
          handleVotingUpdate(update);
        });

        // Subscribe to voting complete
        client.subscribe(`/topic/game/${roomCode}/voting/complete`, (message) => {
          const result = JSON.parse(message.body);
          handleVotingComplete(result);
        });

        // Subscribe to timer updates
        client.subscribe(`/topic/game/${roomCode}/voting/timer`, (message) => {
          const timer = JSON.parse(message.body);
          handleTimerUpdate(timer);
        });
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame);
        toast.error('WebSocket connection error');
      },
      onWebSocketClose: () => {
        console.log('WebSocket closed');
      }
    });

    client.activate();
    stompClient.current = client;
  };

  /**
   * Rozłącza WebSocket
   */
  const disconnectWebSocket = () => {
    if (stompClient.current) {
      stompClient.current.deactivate();
      stompClient.current = null;
    }
  };

  /**
   * Obsługuje aktualizację głosowania z WebSocket
   */
  const handleVotingUpdate = (update) => {
    console.log('Voting update received:', update);
    
    setVotingSession(prev => ({
      ...prev,
      votesReceived: update.votesReceived,
      totalEligibleVoters: update.totalEligibleVoters,
      currentResults: update.currentResults,
      status: update.status
    }));

    if (update.remainingTimeSeconds !== null) {
      setRemainingTime(update.remainingTimeSeconds);
    }
  };

  /**
   * Obsługuje zakończenie głosowania z WebSocket
   */
  const handleVotingComplete = (result) => {
    console.log('Voting complete:', result);

    if (result.eliminatedUsername) {
      toast.info(`${result.eliminatedUsername} has been eliminated!`, {
        autoClose: 5000
      });
    } else if (result.isTie) {
      toast.info('Tie vote - no elimination', {
        autoClose: 3000
      });
    } else {
      toast.info('Voting complete - no elimination', {
        autoClose: 3000
      });
    }

    // Reset state
    setHasVoted(false);
    setVotingSession(null);
    
    // Refresh session after a delay
    setTimeout(() => {
      fetchVotingSession();
    }, 2000);
  };

  /**
   * Obsługuje aktualizację timera z WebSocket
   */
  const handleTimerUpdate = (timer) => {
    setRemainingTime(timer.remainingSeconds);
  };

  /**
   * Oddaje głos
   */
  const handleVote = async (targetUserId) => {
    if (!votingSession || hasVoted) return;

    try {
      const response = await httpClient.post(
        `/api/games/${gameId}/voting/vote`,
        {
          votingSessionId: votingSession.sessionId,
          targetUserId: targetUserId
        }
      );

      if (response.data.success) {
        setHasVoted(true);
        toast.success('Vote cast successfully!');
      } else {
        toast.error(response.data.message || 'Failed to cast vote');
      }
    } catch (error) {
      console.error('Error casting vote:', error);
      toast.error(error.response?.data?.message || 'Failed to cast vote');
    }
  };

  // Znajdź aktualnego gracza
  const currentPlayer = players?.find(p => String(p.userId) === String(currentUser?.id));
  const isPlayerAlive = currentPlayer?.isAlive !== false;
  const canVote = isPlayerAlive && !hasVoted && votingSession?.status === 'ACTIVE';

  if (loading) {
    return (
      <div className={styles.loadingContainer}>
        <div className={styles.spinner}></div>
        <p>Loading voting session...</p>
      </div>
    );
  }

  if (!votingSession) {
    return (
      <div className={styles.noSessionContainer}>
        <p>No active voting session</p>
        <p className={styles.subtext}>Waiting for the next phase...</p>
      </div>
    );
  }

  return (
    <div className={styles.votingPhaseContainer}>
      {votingSession.phase === 'DAY_VOTE' ? (
        <DayVotingPanel
          session={votingSession}
          onVote={handleVote}
          hasVoted={hasVoted}
          currentUser={currentUser}
          players={players}
          remainingTime={remainingTime}
        />
      ) : (
        <NightVotingPanel
          session={votingSession}
          onVote={handleVote}
          hasVoted={hasVoted}
          currentUser={currentUser}
          players={players}
          remainingTime={remainingTime}
        />
      )}
    </div>
  );
};

export default VotingPhaseContainer;
