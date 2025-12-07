import React, { useEffect, useMemo, useRef, useState, useCallback } from "react";
import { useNavigate, useParams, useLocation } from "react-router-dom";
import MainLayout from "../../layouts/mainLayout/MainLayout";
import styles from "./GameRoomView.module.css";
import httpClient from "../../utils/httpClient";
import { QRCodeSVG } from "qrcode.react";
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";
import GameRoomSettings from "../../components/gameRoomSettings/GameRoomSettings";
import toast from "../../utils/notifications";

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || "";

const GameRoomView = () => {
  const { roomCode } = useParams();
  const navigate = useNavigate();
  const location = useLocation();

  const [room, setRoom] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [copied, setCopied] = useState(false);
  const [joining, setJoining] = useState(false);
  const [starting, setStarting] = useState(false);
  const [leaving, setLeaving] = useState(false);
  const [gameReady, setGameReady] = useState(false);

  const stompRef = useRef(null);

  const joinUrl = useMemo(() => `${window.location.origin}/join/${roomCode}`, [roomCode]);

  const fetchRoom = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const resp = await httpClient.get(`/api/game_rooms/${roomCode}`);
      setRoom(resp.data);
      // Reset gameReady gdy wracamy z zakończonej gry
      setGameReady(false);
      setStarting(false);
    } catch (err) {
      console.error("Failed to fetch room:", err);
      const msg = err?.response?.data?.message || err?.message || "Failed to load room";
      setError(msg);
    } finally {
      setLoading(false);
    }
  }, [roomCode]);

  // Odśwież dane gdy wracamy z ekranu wyników gry
  useEffect(() => {
    if (location.state?.fromGameResult) {
      console.log("🔄 Returning from game result - refreshing room data");
      fetchRoom();
    }
  }, [location.state?.fromGameResult, location.state?.timestamp, fetchRoom]);

  useEffect(() => {
    fetchRoom();
  }, [fetchRoom]);

  const connectWs = useCallback(() => {
    try {
      const socketFactory = () => new SockJS(`${API_BASE_URL}/ws`);
      const client = new Client({
        webSocketFactory: socketFactory,
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
      });
      client.onConnect = () => {
        console.log("🟢 WebSocket connected for room:", roomCode);
        
        // Subskrypcja na aktualizacje pokoju
        client.subscribe(`/topic/game/${roomCode}/updated`, (message) => {
          try {
            const payload = JSON.parse(message.body);
            console.log("🔔 Received WS update:", payload);
            
            // Jeśli pokój wraca do stanu OPEN (po zakończeniu gry), zresetuj gameReady
            if (payload.status === "OPEN" || payload.type === "room_status_changed") {
              console.log("🔄 Room status changed to OPEN - resetting gameReady state");
              setGameReady(false);
              setStarting(false);
            }
            
            // Precyzyjny merge GameRoomUpdateDto
            setRoom((prev) => {
              if (!prev) {
                console.log("⚠️ No previous room state, skipping update");
                return prev;
              }
              
              console.log("📊 Previous status:", prev.status, "| New status:", payload.status);
              
              const updated = {
                ...prev,
                currentPlayers: payload.currentPlayers ?? prev.currentPlayers,
                status: payload.status ?? prev.status,
                gameRoomStatus: payload.status ?? prev.gameRoomStatus,
                players: payload.players ?? prev.players,
              };
              
              console.log("✅ Room state updated:", updated);
              return updated;
            });
          } catch (e) {
            console.error("❌ WS parse error:", e);
          }
        });

        // Subskrypcja na "game_ready" - gra w pełni gotowa
        client.subscribe(`/topic/game/${roomCode}/ready`, (message) => {
          try {
            const payload = JSON.parse(message.body);
            console.log("🎮 GAME READY received:", payload);
            setGameReady(true);
          } catch (e) {
            console.error("❌ WS parse error (ready):", e);
          }
        });

        // Subskrypcja na usunięcie pokoju przez hosta
        client.subscribe(`/topic/game/${roomCode}/roomDeleted`, (message) => {
          console.log("🗑️ Room deleted by host:", message.body);
          toast.info("Room has been deleted by the host");
          navigate("/dashboard");
        });
      };
      client.onStompError = (frame) => {
        console.error("STOMP error:", frame.headers["message"], frame.body);
      };
      client.onWebSocketClose = () => {
        // ignore
      };
      client.activate();
      stompRef.current = client;
      return () => {
        try {
          client.deactivate();
        } catch {}
      };
    } catch (e) {
      console.warn("WS connect error:", e);
      return () => {};
    }
  }, [roomCode, navigate]);

  useEffect(() => {
    const cleanup = connectWs();
    return cleanup;
  }, [connectWs]);

  const handleCopyJoinLink = async () => {
    try {
      await navigator.clipboard.writeText(joinUrl);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch {
      try {
        const temp = document.createElement("input");
        temp.value = joinUrl;
        document.body.appendChild(temp);
        temp.select();
        document.execCommand("copy");
        document.body.removeChild(temp);
        setCopied(true);
        setTimeout(() => setCopied(false), 2000);
      } catch (e) {
        console.error("Copy failed:", e);
      }
    }
  };

  const handleJoin = async () => {
    setJoining(true);
    setError("");
    try {
      await httpClient.post(`/api/game_rooms/join/${roomCode}`);
      await fetchRoom();
    } catch (err) {
      console.error("Join failed:", err);
      const msg = err?.response?.data?.message || err?.message || "Failed to join room";
      setError(msg);
    } finally {
      setJoining(false);
    }
  };

  const handleStartWithSettings = async (settings) => {
    if (!room?.id) return;
    setStarting(true);
    setError("");
    try {
      console.log("🎮 Starting game with settings:", settings);
      
      // Nowy endpoint z konfiguracją
      await httpClient.post(`/api/games/start`, {
        roomId: room.id,
        mafiaCount: settings.mafiaCount,
        discussionTimeSeconds: settings.discussionTimeSeconds
      });
      
      console.log("✅ Game start request successful - waiting for game_ready event");
      // NIE nawiguj tutaj! Czekamy na WebSocket "game_ready"
      // await fetchRoom(); - też nie potrzebne, WS zaktualizuje stan
    } catch (err) {
      console.error("❌ Start failed:", err);
      const msg = err?.response?.data?.message || err?.message || "Failed to start game";
      setError(msg);
      setStarting(false); // Tylko przy błędzie
    }
    // NIE rób setStarting(false) tutaj - zostaw "Starting..." aż przyjdzie game_ready
  };

  const handleLeaveRoom = async () => {
    const isHostLeaving = isHost;
    const confirmMessage = isHostLeaving 
      ? "As the host, leaving will delete this room for everyone. Are you sure?"
      : "Are you sure you want to leave this room?";
    
    if (!window.confirm(confirmMessage)) {
      return;
    }

    setLeaving(true);
    setError("");
    try {
      await httpClient.post(`/api/game_rooms/leave/${roomCode}`);
      toast.success(isHostLeaving ? "Room deleted successfully" : "Left room successfully");
      navigate("/dashboard");
    } catch (err) {
      console.error("Leave room failed:", err);
      const msg = err?.response?.data?.message || err?.message || "Failed to leave room";
      setError(msg);
      toast.error(msg);
    } finally {
      setLeaving(false);
    }
  };

  // Derived flags
  const status = room?.status || room?.gameRoomStatus || "UNKNOWN";
  const players = Array.isArray(room?.players) ? room.players : [];
  const currentPlayers = room?.currentPlayers ?? players.length ?? 0;
  const maxPlayers = room?.maxPlayers ?? 0;
  const hostUsername = room?.hostUsername ?? "";

  // Wymaga useAuth, ale minimalnie wykryjmy po nazwie
  const token = localStorage.getItem("token");
  let currentUserId = null;
  try {
    if (token) {
      const base64Url = token.split(".")[1];
      const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split("")
          .map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
          .join("")
      );
      const decoded = JSON.parse(jsonPayload);
      currentUserId = decoded.sub || null;
    }
  } catch {}

  const isInRoom = players.some((p) => String(p.userId) === String(currentUserId));
  const canJoin = !isInRoom && currentPlayers < maxPlayers && ["WAITING_FOR_PLAYERS", "READY_TO_START", "OPEN"].includes(String(status));
  const isHost = !!room?.hostId && String(room.hostId) === String(currentUserId);
  const isGameInProgress = String(status) === "GAME_IN_PROGRESS" || String(status) === "IN_PROGRESS";
  const canStart = isHost && players.length >= 2 && !isGameInProgress;

  // Nawigacja do gry TYLKO po otrzymaniu "game_ready"
  useEffect(() => {
    if (gameReady && isInRoom) {
      console.log("🚀 GAME READY - Navigating to game view:", `/game/${roomCode}`);
      navigate(`/game/${roomCode}`);
    }
  }, [gameReady, isInRoom, roomCode, navigate]);

  if (loading) {
    return (
      <div className={styles.container}>
        <p className={styles.info}>Loading room...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className={styles.container}>
        <p className={styles.error}>{error}</p>
        <button className={styles.secondaryBtn} onClick={() => navigate("/dashboard")}>Back to Dashboard</button>
      </div>
    );
  }

  if (!room) {
    return (
      <div className={styles.container}>
        <p className={styles.error}>Room not found.</p>
        <button className={styles.secondaryBtn} onClick={() => navigate("/dashboard")}>Back to Dashboard</button>
      </div>
    );
  }

  return (
    <MainLayout>
      <div className={styles.container}>
        <div className={styles.headerRow}>
          <h2 className={styles.title}>Room: {room.name} ({roomCode})</h2>
          <div className={styles.actionsRight}>
            <button className={styles.secondaryBtn} onClick={() => navigate("/dashboard")}>Back to Dashboard</button>
          </div>
        </div>

        <div className={styles.qrSection}>
          <div className={styles.qrWrapper} onClick={handleCopyJoinLink} title="Click to copy join link">
            <QRCodeSVG value={joinUrl} size={128} level="H" />
          </div>
          <div className={styles.qrText}>
            <div className={styles.qrLabel}>Scan to join</div>
            <div className={styles.qrHint}>{copied ? "Join link copied!" : "Click QR to copy join link"}</div>
            <div className={styles.qrLink}>{joinUrl}</div>
          </div>
        </div>

        <div className={styles.meta}>
          <div><strong>Status:</strong> {String(status).replace(/_/g, " ")}</div>
          <div><strong>Host:</strong> {hostUsername || "Unknown"}</div>
          <div><strong>Players:</strong> {currentPlayers}/{maxPlayers}</div>
        </div>

        <div className={styles.section}>
          <h3>Players</h3>
          {players.length === 0 ? (
            <p className={styles.info}>No players in this room yet.</p>
          ) : (
            <ul className={styles.playerList}>
              {players.map((p, idx) => (
                <li key={p.userId || idx} className={styles.playerItem}>
                  <span>{p.nicknameInRoom || p.username || p.displayName || p.userId}</span>
                  {String(p.userId) === String(room.hostId) && (
                    <span className={styles.hostBadge}>Host</span>
                  )}
                </li>
              ))}
            </ul>
          )}
        </div>

        {/* Przycisk Join dla nie-członków */}
        {canJoin && (
          <div className={styles.btnRow}>
            <button className={styles.primaryBtn} onClick={handleJoin} disabled={joining}>
              {joining ? "Joining..." : "Join Room"}
            </button>
          </div>
        )}

        {/* Game Settings dla hosta */}
        {canStart && (
          <GameRoomSettings
            onStartGame={handleStartWithSettings}
            isStarting={starting}
            minPlayers={3}
          />
        )}

        {/* Przycisk Leave/Delete Room dla członków pokoju */}
        {isInRoom && (
          <div className={styles.btnRow}>
            <button 
              className={`${styles.dangerBtn} ${isHost ? styles.deleteBtn : ''}`} 
              onClick={handleLeaveRoom} 
              disabled={leaving || isGameInProgress}
              title={isGameInProgress ? "Cannot leave during active game" : ""}
            >
              {leaving ? "Leaving..." : (isHost ? "Delete Room" : "Leave Room")}
            </button>
          </div>
        )}
      </div>
    </MainLayout>
  );
};

export default GameRoomView;
