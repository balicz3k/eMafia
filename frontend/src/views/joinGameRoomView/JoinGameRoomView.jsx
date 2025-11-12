import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import MainLayout from "../../layouts/mainLayout/MainLayout";
import styles from "./JoinGameRoomView.module.css";
import httpClient from "../../utils/httpClient";

const JoinGameRoomView = () => {
  const { roomCode } = useParams();
  const navigate = useNavigate();

  const [room, setRoom] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [joining, setJoining] = useState(false);

  // Fetch room info for validation
  useEffect(() => {
    const fetchRoom = async () => {
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
    };

    if (roomCode) {
      fetchRoom();
    }
  }, [roomCode]);

  const handleJoin = async () => {
    setJoining(true);
    setError("");
    try {
      await httpClient.post(`/api/game_rooms/join/${roomCode}`, { roomCode });
      // Po sukcesie nawiguj do widoku pokoju
      navigate(`/room/${roomCode}`);
    } catch (err) {
      console.error("Join failed:", err);
      const msg = err?.response?.data?.message || err?.message || "Failed to join room";
      setError(msg);
    } finally {
      setJoining(false);
    }
  };

  return (
    <MainLayout>
      <div className={styles.container}>
        <h2 className={styles.title}>Join Game Room</h2>

        {loading && <p className={styles.info}>Loading room...</p>}

        {error && <p className={styles.error}>{error}</p>}

        {!loading && room && (
          <>
            <div className={styles.roomInfo}>
              <p>
                <strong>Room:</strong> {room.name} ({roomCode})
              </p>
              <p>
                <strong>Host:</strong> {room.hostUsername || "Unknown"}
              </p>
              <p>
                <strong>Players:</strong> {room.currentPlayers}/{room.maxPlayers}
              </p>
              <p>
                <strong>Status:</strong> {String(room.status || "UNKNOWN").replace(/_/g, " ")}
              </p>
            </div>

            <div className={styles.actions}>
              <button
                className={styles.primaryBtn}
                onClick={handleJoin}
                disabled={joining}
              >
                {joining ? "Joining..." : "Join Room"}
              </button>
              <button
                className={styles.secondaryBtn}
                onClick={() => navigate("/dashboard")}
              >
                Back to Dashboard
              </button>
            </div>
          </>
        )}

        {!loading && !room && !error && (
          <div className={styles.actions}>
            <button
              className={styles.secondaryBtn}
              onClick={() => navigate("/dashboard")}
            >
              Back to Dashboard
            </button>
          </div>
        )}
      </div>
    </MainLayout>
  );
};

export default JoinGameRoomView;
