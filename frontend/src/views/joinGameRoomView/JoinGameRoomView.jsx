import React, { useEffect, useState, useRef, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import MainLayout from '../../layouts/mainLayout/MainLayout'; // Upewnij się, że ta ścieżka jest poprawna
import styles from './JoinGameRoomView.module.css'; // Stworzymy ten plik poniżej
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { decodeJwt } from "../../utils/decodeJwt"; // Załóżmy, że masz tę funkcję do dekodowania JWT

const JoinGameRoomView = () => {
    const { roomCode } = useParams();
    const navigate = useNavigate();
    const [roomDetails, setRoomDetails] = useState(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');
    const [isJoining, setIsJoining] = useState(false);
    const stompClientRef = useRef(null);
    const token = localStorage.getItem("token");
    const [currentUser, setCurrentUser] = useState(null);

    useEffect(() => {
        if (token) {
            try {
                setCurrentUser(decodeJwt(token));
            } catch (e) {
                console.error("Failed to decode token:", e);
            }
        }
    }, [token]);

    const disconnectWebSocket = () => {
        if (stompClientRef.current && stompClientRef.current.connected) {
            stompClientRef.current.deactivate();
            console.log('WebSocket disconnected for room:', roomCode);
        }
        stompClientRef.current = null;
    };

    const connectWebSocket = useCallback((currentRoomCode) => {
        if (!token) {
            console.log("No token, WebSocket connection aborted.");
            return;
        }
        if (stompClientRef.current && stompClientRef.current.connected) {
            console.log('WebSocket already connected.');
            return;
        }

        const socketFactory = () => new SockJS('http://localhost:8080/ws'); // Upewnij się, że URL jest poprawny
        const client = new Client({
            webSocketFactory: socketFactory,
            connectHeaders: {
                Authorization: `Bearer ${token}`,
            },
            debug: function (str) {
            },
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
        });

        client.onConnect = (frame) => {
            console.log('Connected to WebSocket for room:', currentRoomCode, frame);
            stompClientRef.current = client;

            client.subscribe(`/topic/game/${currentRoomCode}/updated`, (message) => {
                try {
                    const updatedRoom = JSON.parse(message.body);
                    console.log('Received room update via WebSocket:', updatedRoom);
                    setRoomDetails(updatedRoom);
                } catch (e) {
                    console.error("Error parsing WebSocket message:", e, message.body);
                }
            });
        };

        client.onStompError = (frame) => {
            console.error('Broker reported error: ' + frame.headers['message']);
            console.error('Additional details: ' + frame.body);
            setError('WebSocket connection error. Please try refreshing the page.');
        };

        client.onWebSocketClose = (event) => {
            console.log("WebSocket closed for room:", currentRoomCode, event);
            // Możesz chcieć spróbować połączyć się ponownie lub poinformować użytkownika
        };
        
        client.activate();

    }, [token]);


    useEffect(() => {
        const fetchRoomDetails = async () => {
            setIsLoading(true);
            setError('');
            if (!token) {
                setError("You must be logged in to view or join a room.");
                setIsLoading(false);
                navigate('/login'); // Przekieruj do logowania
                return;
            }
            try {
                const response = await fetch(`http://localhost:8080/api/gamerooms/${roomCode}`, {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                    },
                });
                if (response.ok) {
                    const data = await response.json();
                    setRoomDetails(data);
                    if (!stompClientRef.current || !stompClientRef.current.connected) {
                        connectWebSocket(roomCode);
                    }
                } else {
                    const errorData = await response.json().catch(() => ({ message: "Failed to fetch room details." }));
                    setError(errorData.message || `Error: ${response.status}`);
                    if (response.status === 401 || response.status === 403) {
                         navigate('/login');
                    }
                }
            } catch (err) {
                console.error("Error fetching room details:", err);
                setError('An error occurred while fetching room details. Please try again.');
            } finally {
                setIsLoading(false);
            }
        };

        if (roomCode) {
            fetchRoomDetails();
        } else {
            setError("No room code provided.");
            setIsLoading(false);
        }

        return () => {
            disconnectWebSocket();
        };
    }, [roomCode, token, navigate, connectWebSocket]);

    const handleJoinRoom = async () => {
        setIsJoining(true);
        setError('');
        if (!token) {
            setError("You must be logged in to join a room.");
            setIsJoining(false);
            navigate('/login');
            return;
        }
        try {
            const response = await fetch(`http://localhost:8080/api/gamerooms/${roomCode}/join`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                },
            });
            if (response.ok) {
                const data = await response.json();
                setRoomDetails(data); // Backend zwraca zaktualizowane dane pokoju
                console.log('Successfully joined room:', data);
                // WebSocket powinien również wysłać aktualizację, ale ustawienie tutaj może być szybsze dla UI
            } else {
                const errorData = await response.json().catch(() => ({ message: "Failed to join room." }));
                setError(errorData.message || `Failed to join room. Status: ${response.status}`);
            }
        } catch (err) {
            console.error("Error joining room:", err);
            setError('An error occurred while trying to join the room.');
        } finally {
            setIsJoining(false);
        }
    };

    if (isLoading) {
        return <MainLayout><div className={styles.viewContainer}><p>Loading room information...</p></div></MainLayout>;
    }

    if (!roomDetails && !error) { // Jeśli nie ma danych, ale też nie ma błędu (np. początkowy stan)
        return <MainLayout><div className={styles.viewContainer}><p>Fetching room details...</p></div></MainLayout>;
    }
    
    if (error && !roomDetails) { // Jeśli jest błąd i nie ma danych pokoju
        return <MainLayout><div className={styles.viewContainer}><p className={styles.errorMessage}>{error}</p></div></MainLayout>;
    }
    
    if (!roomDetails) { // Ostateczny fallback, jeśli roomDetails jest null
        return <MainLayout><div className={styles.viewContainer}><p className={styles.errorMessage}>Could not load room details.</p></div></MainLayout>;
    }

    const isCurrentUserInRoom = currentUser && roomDetails.players.some(player => player.userId === currentUser.sub);
    const canJoin = roomDetails.status === 'WAITING_FOR_PLAYERS' &&
                    roomDetails.currentPlayers < roomDetails.maxPlayers &&
                    !isCurrentUserInRoom;

    // TODO: Logika przycisku "Start Game" dla hosta
    // const isHost = currentUser && roomDetails.hostUsername === currentUser.username;
    // const canStartGame = isHost && roomDetails.status === 'READY_TO_START';


    return (
        <MainLayout>
            <div className={styles.viewContainer}>
                <h2>Room: {roomDetails.name} ({roomDetails.roomCode})</h2>
                {error && <p className={styles.errorMessage}>{error}</p>} {/* Wyświetl błąd także gdy są dane pokoju */}
                <p>Status: <span className={`${styles.status} ${styles[roomDetails.status.toLowerCase().replace(/_/g, '')]}`}>{roomDetails.status.replace("_", " ")}</span></p>
                <p>Host: {roomDetails.hostUsername}</p>
                <p>Players: {roomDetails.currentPlayers} / {roomDetails.maxPlayers}</p>

                <h3>Players in Room:</h3>
                {roomDetails.players && roomDetails.players.length > 0 ? (
                    <ul className={styles.playerList}>
                        {roomDetails.players.map(player => (
                            <li key={player.playerId || player.userId}>
                                {player.nicknameInRoom} {player.username === roomDetails.hostUsername ? <span className={styles.hostLabel}>(Host)</span> : ''}
                            </li>
                        ))}
                    </ul>
                ) : (
                    <p>No players have joined yet.</p>
                )}

                {canJoin && (
                     <button onClick={handleJoinRoom} disabled={isJoining} className={styles.joinButton}>
                        {isJoining ? 'Joining...' : 'Join Room'}
                    </button>
                )}

                {isCurrentUserInRoom && roomDetails.status === 'WAITING_FOR_PLAYERS' && (
                    <p className={styles.infoMessage}>You are in this room. Waiting for other players...</p>
                )}
                 {isCurrentUserInRoom && roomDetails.status === 'READY_TO_START' && (
                    <p className={styles.infoMessage}>Room is ready to start!</p>
                )}

                {/* {canStartGame && (
                    <button className={styles.startButton}>Start Game</button>
                )} */}
            </div>
        </MainLayout>
    );
};

export default JoinGameRoomView;