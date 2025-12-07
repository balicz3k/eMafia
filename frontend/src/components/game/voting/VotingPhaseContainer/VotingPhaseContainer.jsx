import React, { useEffect, useState, useRef } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import toast from '../../../../utils/notifications';
import httpClient from '../../../../utils/httpClient';
import DayVotingPanel from '../DayVotingPanel/DayVotingPanel';
import NightVotingPanel from '../NightVotingPanel/NightVotingPanel';
import EliminationDialog from '../../common/EliminationDialog/EliminationDialog';
import SpectatorView from '../../common/SpectatorView/SpectatorView';
import styles from './VotingPhaseContainer.module.css';

/**
 * Główny kontener dla fazy głosowania
 * Zarządza WebSocket, stanem głosowania i komunikacją z backendem
 * Dla martwych graczy wyświetla SpectatorView zamiast panelu głosowania
 */
const VotingPhaseContainer = ({ gameId, roomCode, currentUser, players: initialPlayers }) => {
  const [votingSession, setVotingSession] = useState(null);
  const [hasVoted, setHasVoted] = useState(false);
  const [remainingTime, setRemainingTime] = useState(null);
  const [loading, setLoading] = useState(true);
  const [players, setPlayers] = useState(initialPlayers || []);
  const [lastElimination, setLastElimination] = useState(null);
  const stompClient = useRef(null);
  
  // Stan dla dialogu eliminacji
  const [eliminationDialog, setEliminationDialog] = useState({
    isOpen: false,
    eliminatedUsername: null,
    phase: null,
    isTie: false,
    resultType: null
  });

  // Aktualizuj players gdy props się zmieni
  useEffect(() => {
    if (initialPlayers) {
      setPlayers(initialPlayers);
    }
  }, [initialPlayers]);

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
    const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080';
    const socket = new SockJS(`${API_BASE_URL}/ws`);
    
    const client = new Client({
      webSocketFactory: () => socket,
      debug: (str) => {
        // Tylko loguj ważne wiadomości
        if (!str.includes('PING') && !str.includes('PONG')) {
          console.log('STOMP:', str);
        }
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        console.log('🟢 WebSocket connected for voting');

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

        // Subscribe to timer updates (server-side synchronized timer)
        client.subscribe(`/topic/game/${roomCode}/voting/timer`, (message) => {
          const timer = JSON.parse(message.body);
          handleTimerUpdate(timer);
        });

        // Subscribe to phase changes
        client.subscribe(`/topic/game/${roomCode}/phase/change`, (message) => {
          const phaseData = JSON.parse(message.body);
          handlePhaseChange(phaseData);
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
   * Pokazuje dialog eliminacji zamiast toast
   * Aktualizuje listę graczy jeśli ktoś został wyeliminowany
   */
  const handleVotingComplete = (result) => {
    console.log('Voting complete:', result);

    // Zapisz fazę przed resetem sesji
    const currentPhase = votingSession?.phase;

    // Zapisz informację o ostatniej eliminacji
    if (result.eliminatedUsername && result.resultType !== 'NO_ELIMINATION') {
      setLastElimination({
        username: result.eliminatedUsername,
        phase: currentPhase
      });
      
      // Aktualizuj status gracza na liście
      setPlayers(prevPlayers => 
        prevPlayers.map(p => 
          p.username === result.eliminatedUsername 
            ? { ...p, isAlive: false, alive: false }
            : p
        )
      );
    }

    // Pokaż dialog eliminacji
    setEliminationDialog({
      isOpen: true,
      eliminatedUsername: result.eliminatedUsername,
      phase: currentPhase,
      isTie: result.isTie || false,
      resultType: result.resultType || 'ELIMINATION'
    });

    // Reset state
    setHasVoted(false);
    setVotingSession(null);
  };

  /**
   * Zamyka dialog eliminacji i pobiera nową sesję
   */
  const handleCloseEliminationDialog = () => {
    setEliminationDialog({
      isOpen: false,
      eliminatedUsername: null,
      phase: null,
      isTie: false,
      resultType: null
    });
    
    // Pobierz nową sesję głosowania
    fetchVotingSession();
  };

  /**
   * Obsługuje aktualizację timera z WebSocket
   */
  const handleTimerUpdate = (timer) => {
    setRemainingTime(timer.remainingSeconds);
  };

  /**
   * Obsługuje zmianę fazy gry z WebSocket
   */
  const handlePhaseChange = (phaseData) => {
    console.log('🔄 Phase change received:', phaseData);
    
    toast.info(`Phase changed to ${phaseData.phase.replace('_', ' ')}`, {
      autoClose: 3000
    });
    
    // Reset state for new phase
    setHasVoted(false);
    setVotingSession(null);
    
    // Fetch new voting session
    setTimeout(() => {
      fetchVotingSession();
    }, 1000);
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

  // Helper funkcja sprawdzająca czy gracz żyje
  const checkIsAlive = (player) => {
    if (player === null || player === undefined) return false;
    const alive = player.isAlive !== undefined ? player.isAlive : player.alive;
    return alive === true;
  };

  // Znajdź aktualnego gracza
  const currentPlayer = players?.find(p => String(p.userId) === String(currentUser?.id));
  const isPlayerAlive = checkIsAlive(currentPlayer);
  const canVote = isPlayerAlive && !hasVoted && votingSession?.status === 'ACTIVE';

  // Dla martwych graczy pokazujemy SpectatorView
  if (!isPlayerAlive && currentPlayer) {
    return (
      <>
        <EliminationDialog
          isOpen={eliminationDialog.isOpen}
          onClose={handleCloseEliminationDialog}
          eliminatedUsername={eliminationDialog.eliminatedUsername}
          phase={eliminationDialog.phase}
          isTie={eliminationDialog.isTie}
          resultType={eliminationDialog.resultType}
        />
        <SpectatorView
          currentPlayer={currentPlayer}
          players={players}
          currentPhase={votingSession?.phase}
          dayNumber={votingSession?.dayNumber || 1}
          lastElimination={lastElimination}
        />
      </>
    );
  }

  if (loading) {
    return (
      <>
        <EliminationDialog
          isOpen={eliminationDialog.isOpen}
          onClose={handleCloseEliminationDialog}
          eliminatedUsername={eliminationDialog.eliminatedUsername}
          phase={eliminationDialog.phase}
          isTie={eliminationDialog.isTie}
          resultType={eliminationDialog.resultType}
        />
        <div className={styles.loadingContainer}>
          <div className={styles.spinner}></div>
          <p>Loading voting session...</p>
        </div>
      </>
    );
  }

  if (!votingSession) {
    return (
      <>
        <EliminationDialog
          isOpen={eliminationDialog.isOpen}
          onClose={handleCloseEliminationDialog}
          eliminatedUsername={eliminationDialog.eliminatedUsername}
          phase={eliminationDialog.phase}
          isTie={eliminationDialog.isTie}
          resultType={eliminationDialog.resultType}
        />
        <div className={styles.noSessionContainer}>
          <p>No active voting session</p>
          <p className={styles.subtext}>Waiting for the next phase...</p>
        </div>
      </>
    );
  }

  return (
    <>
      <EliminationDialog
        isOpen={eliminationDialog.isOpen}
        onClose={handleCloseEliminationDialog}
        eliminatedUsername={eliminationDialog.eliminatedUsername}
        phase={eliminationDialog.phase}
        isTie={eliminationDialog.isTie}
        resultType={eliminationDialog.resultType}
      />
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
    </>
  );
};

export default VotingPhaseContainer;
