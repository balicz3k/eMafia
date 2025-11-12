import React, { useState, useEffect } from "react";
import styles from "./DayEliminationPanel.module.css";
import { decodeJwt } from "../../../../utils/decodeJwt";

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL;

const DayEliminationPanel = ({ roomCode, players, onVoted, dayNumber = 1, gamePhase = "DAY_ELIMINATION" }) => {
  const [submitting, setSubmitting] = useState(false);
  const [err, setErr] = useState("");
  const [votesReceived, setVotesReceived] = useState(0);
  const [totalAlive, setTotalAlive] = useState(players?.length || 0);
  const [votingComplete, setVotingComplete] = useState(false);
  const [eliminatedPlayer, setEliminatedPlayer] = useState(null);
  const [currentUserRole, setCurrentUserRole] = useState(null);

  useEffect(() => {
    setTotalAlive(players?.length || 0);
  }, [players]);

  // Decode JWT to get current user info
  useEffect(() => {
    const token = localStorage.getItem("token");
    if (token) {
      try {
        const decoded = decodeJwt(token);
        // Role will be determined by backend from player state
        console.log("Current user from token:", decoded);
      } catch (e) {
        console.error("Failed to decode token:", e);
      }
    }
  }, []);

  const vote = async (targetUsername, targetRole) => {
    const token = localStorage.getItem("token");
    if (!token) {
      setErr("Authentication required. Please login.");
      return;
    }

    setSubmitting(true);
    setErr("");

    try {
      // Get current user from token
      let voterUsername = null;
      try {
        const decoded = decodeJwt(token);
        voterUsername = decoded.sub; // username is in 'sub' claim
      } catch (e) {
        console.error("Failed to decode token:", e);
        setErr("Failed to get voter information");
        setSubmitting(false);
        return;
      }

      const voteRequest = {
        targetUsername,
        voterUsername, // Set from JWT
        gamePhase,
        voterRole: null, // Will be determined by backend from player state
        dayNumber,
        votedAt: new Date().toISOString(),
      };

      console.log("Sending vote request:", voteRequest);

      const res = await fetch(`${API_BASE_URL}/api/games/${roomCode}/vote`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(voteRequest),
      });

      if (!res.ok) {
        let errorMessage = "Failed to cast vote.";
        try {
          const errorData = await res.json();
          errorMessage = errorData.message || errorData.error || errorMessage;
          console.error("Vote error response:", errorData);
        } catch (parseError) {
          const textMsg = await res.text().catch(() => "");
          errorMessage = textMsg || `Error: ${res.status} ${res.statusText}`;
        }
        setErr(errorMessage);
        console.error("Vote failed:", errorMessage);
      } else {
        const responseData = await res.json();
        console.log("Vote response:", responseData);
        setErr("");
        
        // Update vote progress
        setVotesReceived(responseData.votesReceived);
        setTotalAlive(responseData.totalPlayersAlive);
        
        if (responseData.votingComplete) {
          setVotingComplete(true);
          if (responseData.eliminatedPlayerId) {
            setEliminatedPlayer({
              id: responseData.eliminatedPlayerId,
              username: responseData.eliminatedPlayerUsername,
            });
          }
        }
        
        onVoted && onVoted(responseData);
      }
    } catch (error) {
      setErr(`Network error: ${error.message || "Unknown error"}`);
      console.error("Vote error:", error);
    } finally {
      setSubmitting(false);
    }
  };

  if (!players || players.length === 0) return <p>No players available.</p>;

  return (
    <div className={styles.wrapper}>
      {err && <p className={styles.error}>{err}</p>}
      
      {/* Vote Progress */}
      <div className={styles.voteProgress}>
        <p className={styles.progressText}>
          Votes received: <strong>{votesReceived}/{totalAlive}</strong>
        </p>
        <div className={styles.progressBar}>
          <div 
            className={styles.progressFill} 
            style={{ width: totalAlive > 0 ? `${(votesReceived / totalAlive) * 100}%` : "0%" }}
          />
        </div>
      </div>

      {/* Elimination Result */}
      {votingComplete && eliminatedPlayer && (
        <div className={styles.eliminationResult}>
          <p className={styles.eliminatedMessage}>
            🔪 <strong>{eliminatedPlayer.username}</strong> has been eliminated!
          </p>
        </div>
      )}

      {/* Player List */}
      <ul className={styles.playerList}>
        {players.map((p) => (
          <li key={p.id || p.userId || p.playerId} className={styles.playerRow}>
            <span className={styles.playerName}>
              {p.username || p.displayName || "Unknown"}
            </span>
            <button
              disabled={submitting || votingComplete}
              className={styles.voteButton}
              onClick={() => vote(p.username || p.displayName, p.role)}
            >
              {submitting ? "..." : votingComplete ? "✓" : "Vote"}
            </button>
          </li>
        ))}
      </ul>
    </div>
  );
};

export default DayEliminationPanel;
