import React, { useState } from "react";
import styles from "./EnterRoomCodeForm.module.css";

const EnterRoomCodeForm = ({ onSubmitCode }) => {
  const [roomCode, setRoomCode] = useState("");

  const handleSubmit = (e) => {
    e.preventDefault();
    const trimmedCode = roomCode.trim().toUpperCase();
    if (trimmedCode) {
      onSubmitCode(trimmedCode);
    } else {
      alert("Please enter a room code."); // Prosta walidacja
    }
  };

  return (
    <form onSubmit={handleSubmit} className={styles.joinForm}>
      <div className={styles.formGroup}>
        <label htmlFor="roomCodeInput" className={styles.label}>
          Enter Room Code:
        </label>
        <input
          type="text"
          id="roomCodeInput"
          value={roomCode}
          onChange={(e) => setRoomCode(e.target.value)}
          placeholder="e.g., ABCDEF"
          className={styles.inputField}
          maxLength={10} // Możesz dostosować maksymalną długość kodu
          required
          autoFocus
        />
      </div>
      <button type="submit" className={styles.joinButton}>
        Join Game
      </button>
    </form>
  );
};

export default EnterRoomCodeForm;
