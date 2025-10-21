import React, { useState } from "react";
import styles from "./DayEliminationPanel.module.css";

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL;

const DayEliminationPanel = ({ roomCode, players, onVoted }) => {
  const [submitting, setSubmitting] = useState(false);
  const [err, setErr] = useState("");

  const vote = async (targetUserId) => {
    const token = localStorage.getItem("token");
    if (!token) return;
    setSubmitting(true);
    setErr("");
    try {
      const res = await fetch(`${API_BASE_URL}/api/game/${roomCode}/vote`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ phase: "DAY_ELIMINATION", targetUserId }),
      });
      if (!res.ok) {
        setErr("Failed to cast vote.");
      } else {
        onVoted && onVoted();
      }
    } catch {
      setErr("Network error.");
    } finally {
      setSubmitting(false);
    }
  };

  if (!players || players.length === 0) return <p>No players.</p>;

  return (
    <div className={styles.wrapper}>
      {err && <p className={styles.error}>{err}</p>}
      <ul className={styles.playerList}>
        {players.map((p) => (
          <li key={p.playerId} className={styles.playerRow}>
            <span>{p.displayName}</span>
            <button
              disabled={submitting}
              className={styles.voteButton}
              onClick={() => vote(p.userId)}
            >
              {submitting ? "..." : "Vote"}
            </button>
          </li>
        ))}
      </ul>
    </div>
  );
};

export default DayEliminationPanel;
