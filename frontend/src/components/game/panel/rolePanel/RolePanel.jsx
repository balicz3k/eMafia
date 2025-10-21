import React, { useState } from "react";
import styles from "./RolePanel.module.css";

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL;

const RolePanel = ({ roomCode }) => {
  const [loading, setLoading] = useState(false);
  const [info, setInfo] = useState(null);
  const [err, setErr] = useState("");

  const fetchMyRole = async () => {
    const token = localStorage.getItem("token");
    if (!token) return;
    setLoading(true);
    setErr("");
    try {
      const res = await fetch(`${API_BASE_URL}/api/game/${roomCode}/me/role`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (res.ok) {
        setInfo(await res.json());
      } else {
        setErr("Failed to fetch role.");
      }
    } catch {
      setErr("Network error.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.wrapper}>
      <button
        className={styles.secondaryButton}
        onClick={fetchMyRole}
        disabled={loading}
      >
        {loading ? "Loading..." : "Show my role"}
      </button>
      {err && <p className={styles.error}>{err}</p>}
      {info && (
        <div className={styles.roleBox}>
          <p>
            Your role: <strong>{info.role}</strong>{" "}
            {info.alive === false && "(eliminated)"}
          </p>
          {info.knownMafiosi?.length > 0 && (
            <>
              <h4>Known mafiosi:</h4>
              <ul>
                {info.knownMafiosi.map((m) => (
                  <li key={m.userId}>{m.userId}</li>
                ))}
              </ul>
            </>
          )}
        </div>
      )}
    </div>
  );
};

export default RolePanel;
