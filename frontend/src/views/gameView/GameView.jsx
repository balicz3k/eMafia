import React, { useEffect, useState } from "react";
import MainLayout from "../../layouts/mainLayout/MainLayout";
import styles from "./GameView.module.css";
import DayEliminationPanel from "../../components/game/day/dayEliminationPanel/DayEliminationPanel";
import RolePanel from "../../components/game/panel/rolePanel/RolePanel";
import SystemBanner from "../../components/game/common/systemBanner/SystemBanner";

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL;

const GameView = () => {
  const roomCode = window.location.pathname.split("/").pop();
  const token = localStorage.getItem("token");

  const [roomDetails, setRoomDetails] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  const fetchRoom = async () => {
    if (!token) return;
    setLoading(true);
    setError("");
    try {
      const res = await fetch(`${API_BASE_URL}/api/gamerooms/${roomCode}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (res.ok) {
        const data = await res.json();
        setRoomDetails(data);
      } else {
        setError("Failed to load room details.");
      }
    } catch {
      setError("Network error while loading room.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRoom();
    // TODO: w następnej iteracji dodać subskrypcję WebSocket stanu gry
  }, [roomCode]);

  return (
    <MainLayout>
      <div className={styles.viewContainer}>
        <h2>
          Game Room{" "}
          {roomDetails ? `– ${roomDetails.name} (${roomDetails.roomCode})` : ""}
        </h2>

        <SystemBanner />

        {loading && <p>Loading game...</p>}
        {error && <p className={styles.errorMessage}>{error}</p>}

        {!loading && roomDetails && (
          <>
            <div className={styles.section}>
              <h3>Day – Elimination Vote</h3>
              <DayEliminationPanel
                roomCode={roomDetails.roomCode}
                players={(roomDetails.players || []).filter(
                  (p) => p.isAlive !== false,
                )}
                onVoted={fetchRoom}
              />
            </div>

            <div className={styles.section}>
              <h3>My role</h3>
              <RolePanel roomCode={roomDetails.roomCode} />
            </div>

            {/* Sekcje do rozbudowy:
                - NightVotePanel
                - DayTaskPanel
                - CardVotePanel
                - Announcements driven by WS
            */}
          </>
        )}
      </div>
    </MainLayout>
  );
};

export default GameView;
