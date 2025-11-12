import React, { useEffect, useMemo, useRef, useState, useCallback } from "react";
import { useNavigate, useParams } from "react-router-dom";
import MainLayout from "../../layouts/mainLayout/MainLayout";
import styles from "./GameRoomView.module.css";
import httpClient from "../../utils/httpClient";
import { QRCodeSVG } from "qrcode.react";
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";
import GameRoomSettings from "../../components/gameRoomSettings/GameRoomSettings";

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || "";

const GameRoomView = () => {
  const { roomCode } = useParams();
  const navigate = useNavigate();

  const [room, setRoom] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [copied, setCopied] = useState(false);
  const [joining, setJoining] = useState(false);
  const [starting, setStarting] = useState(false);

  const stompRef = useRef(null);

  const joinUrl = useMemo(() => `${window.location.origin}/join/${roomCode}`, [roomCode]);

  const fetchRoom = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const resp = await httpClient.get(`/api/game_rooms/${roomCode}`);
      setRoom(resp.data);
    } catch (err) {
      console.error("Failed to fetch room:", err);
      const msg = err?.response?.data?.message || err?.message || "Failed to load room";
      setError(msg);
    } finally {
      setLoading(false);
    }
  }, [roomCode]);

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
        console.log("WebSocket connected for room:", roomCode);
        client.subscribe(`/topic/game/${roomCode}/updated`, (message) => {
          try {
            const payload = JSON.parse(message.body);
            console.log("Received WS update:", payload);
            // Precyzyjny merge GameRoomUpdateDto
            setRoom((prev) => {
              if (!prev) return prev;
              return {
                ...prev,
                currentPlayers: payload.currentPlayers ?? prev.currentPlayers,
                status: payload.status ?? prev.status,
                players: payload.players ?? prev.players,
              };
            });
          } catch (e) {
            console.warn("WS parse error:", e);
          }
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
  }, [roomCode]);

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
      // Nowy endpoint z konfiguracją
      await httpClient.post(`/api/games/start`, {
        roomId: room.id,
        mafiaCount: settings.mafiaCount,
        discussionTimeSeconds: settings.discussionTimeSeconds
      });
      // Backend przekieruje lub wyśle WS update
      await fetchRoom();
    } catch (err) {
      console.error("Start failed:", err);
      const msg = err?.response?.data?.message || err?.message || "Failed to start game";
      setError(msg);
    } finally {
      setStarting(false);
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
  const canStart = isHost && players.length >= 2 && String(status) !== "IN_PROGRESS";

  useEffect(() => {
    if (String(status) === "IN_PROGRESS" && isInRoom) {
      navigate(`/game/${roomCode}`);
    }
  }, [status, isInRoom, roomCode, navigate]);

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
      </div>
    </MainLayout>
  );
};

export default GameRoomView;
