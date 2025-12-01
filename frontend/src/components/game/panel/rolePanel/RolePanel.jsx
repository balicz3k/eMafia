import React, { useState, useEffect, useRef } from "react";
import httpClient from "../../../../utils/httpClient";
import styles from "./RolePanel.module.css";

const RolePanel = ({ roomCode }) => {
  const [loading, setLoading] = useState(false);
  const [isRoleVisible, setIsRoleVisible] = useState(false);
  const [roleData, setRoleData] = useState(null);
  const [error, setError] = useState("");
  const hideTimerRef = useRef(null);

  // Cleanup timer on unmount
  useEffect(() => {
    return () => {
      if (hideTimerRef.current) {
        clearTimeout(hideTimerRef.current);
      }
    };
  }, []);

  const handleShowRole = async () => {
    // Jeśli już widoczna - ukryj natychmiast
    if (isRoleVisible) {
      setIsRoleVisible(false);
      if (hideTimerRef.current) {
        clearTimeout(hideTimerRef.current);
      }
      return;
    }

    // Pobierz rolę z API
    setLoading(true);
    setError("");
    try {
      const response = await httpClient.get(`/api/games/rooms/${roomCode}/me/role`);
      setRoleData(response.data);
      setIsRoleVisible(true);

      // Auto-hide po 3 sekundach
      hideTimerRef.current = setTimeout(() => {
        setIsRoleVisible(false);
      }, 3000);
    } catch (err) {
      console.error("Failed to fetch role:", err);
      setError("Failed to fetch role");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.rolePanel}>
      <button
        className={styles.checkRoleButton}
        onClick={handleShowRole}
        disabled={loading}
      >
        {loading ? "Loading..." : isRoleVisible ? "🔒 Hide Role" : "🔓 Check My Role"}
      </button>

      {error && <p className={styles.error}>{error}</p>}

      {isRoleVisible && roleData && (
        <div className={styles.roleCard}>
          <div className={styles.roleIcon}>
            {roleData.role === "MAFIA" ? "🎭" : "👤"}
          </div>
          <div className={styles.roleInfo}>
            <h3 className={styles.roleTitle}>Your Role</h3>
            <p className={styles.roleName}>{roleData.role}</p>
            {roleData.isAlive === false && (
              <p className={styles.deadBadge}>💀 Eliminated</p>
            )}
          </div>
          <p className={styles.autoHideHint}>Auto-hides in 3 seconds</p>
        </div>
      )}
    </div>
  );
};

export default RolePanel;
