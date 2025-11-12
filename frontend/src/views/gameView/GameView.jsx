import React, { useEffect, useState, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import MainLayout from '../../layouts/mainLayout/MainLayout';
import VotingPhaseContainer from '../../components/game/voting/VotingPhaseContainer/VotingPhaseContainer';
import GameResultPanel from '../../components/game/GameResultPanel/GameResultPanel';
import httpClient from '../../utils/httpClient';
import toast from '../../utils/notifications';
import styles from './GameView.module.css';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

const GameView = () => {
  const { roomCode } = useParams();
  const navigate = useNavigate();
  
  const [gameData, setGameData] = useState(null);
  const [gameResult, setGameResult] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const stompClient = useRef(null);

  // Pobierz aktualnego użytkownika z tokena
  const getCurrentUser = () => {
    const token = localStorage.getItem('token');
    if (!token) return null;
    
    try {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split('')
          .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      );
      const decoded = JSON.parse(jsonPayload);
      return { id: decoded.sub, username: decoded.username };
    } catch (e) {
      console.error('Error decoding token:', e);
      return null;
    }
  };

  const currentUser = getCurrentUser();

  // Pobierz aktywną grę
  const fetchActiveGame = async () => {
    try {
      setLoading(true);
      setError('');
      const response = await httpClient.get(`/api/games/rooms/${roomCode}/active-game`);
      setGameData(response.data);
      
      // Sprawdź czy gra się zakończyła
      if (response.data.currentPhase === 'GAME_OVER') {
        console.log('Game has ended, waiting for gameOver event');
        toast.info('Game has ended');
      }
    } catch (err) {
      console.error('Error fetching active game:', err);
      const msg = err?.response?.data?.message || err?.message || 'Failed to load game';
      setError(msg);
      
      // Jeśli nie ma aktywnej gry, wróć do pokoju
      if (err?.response?.status === 404 || err?.response?.status === 400) {
        toast.error('No active game found');
        setTimeout(() => navigate(`/room/${roomCode}`), 2000);
      }
    } finally {
      setLoading(false);
    }
  };

  // Połącz WebSocket
  const connectWebSocket = () => {
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
        console.log('WebSocket connected for game');

        // Subscribe to game over
        client.subscribe(`/topic/game/${roomCode}/gameOver`, (message) => {
          const result = JSON.parse(message.body);
          console.log('Game over received:', result);
          setGameResult(result);
        });

        // Subscribe to phase changes (opcjonalne - dla przyszłych rozszerzeń)
        client.subscribe(`/topic/game/${roomCode}/phaseChange`, (message) => {
          const phaseData = JSON.parse(message.body);
          console.log('Phase changed:', phaseData);
          // Odśwież dane gry
          fetchActiveGame();
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

  // Rozłącz WebSocket
  const disconnectWebSocket = () => {
    if (stompClient.current) {
      stompClient.current.deactivate();
      stompClient.current = null;
    }
  };

  useEffect(() => {
    if (!currentUser) {
      toast.error('You must be logged in to view the game');
      navigate('/login');
      return;
    }

    fetchActiveGame();
    connectWebSocket();

    return () => {
      disconnectWebSocket();
    };
  }, [roomCode]);

  // Jeśli gra się zakończyła, pokaż wyniki
  if (gameResult) {
    return (
      <MainLayout>
        <GameResultPanel gameResult={gameResult} roomCode={roomCode} />
      </MainLayout>
    );
  }

  if (loading) {
    return (
      <MainLayout>
        <div className={styles.container}>
          <div className={styles.loading}>
            <div className={styles.spinner}></div>
            <p>Loading game...</p>
          </div>
        </div>
      </MainLayout>
    );
  }

  if (error) {
    return (
      <MainLayout>
        <div className={styles.container}>
          <div className={styles.error}>
            <h2>Error</h2>
            <p>{error}</p>
            <button 
              className={styles.backButton}
              onClick={() => navigate(`/room/${roomCode}`)}
            >
              Back to Room
            </button>
          </div>
        </div>
      </MainLayout>
    );
  }

  if (!gameData) {
    return (
      <MainLayout>
        <div className={styles.container}>
          <div className={styles.noData}>
            <p>No game data available</p>
            <button 
              className={styles.backButton}
              onClick={() => navigate(`/room/${roomCode}`)}
            >
              Back to Room
            </button>
          </div>
        </div>
      </MainLayout>
    );
  }

  return (
    <MainLayout>
      <div className={styles.container}>
        <VotingPhaseContainer
          gameId={gameData.gameId}
          roomCode={roomCode}
          currentUser={currentUser}
          players={gameData.players}
        />
      </div>
    </MainLayout>
  );
};

export default GameView;
