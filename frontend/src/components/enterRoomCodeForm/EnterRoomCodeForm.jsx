import React, { useState } from "react";
import styles from "./EnterRoomCodeForm.module.css";
import { MdKey } from "react-icons/md";

const EnterRoomCodeForm = ({ onSubmitCode }) => {
  const [roomCode, setRoomCode] = useState("");

  const handleSubmit = (e) => {
    e.preventDefault();
    const trimmedCode = roomCode.trim().toUpperCase();
    if (trimmedCode) {
      if (trimmedCode.length !== 6) {
        alert("Room code must be 6 characters long.");
      } else if (!/^[A-Z0-9]+$/.test(trimmedCode)) {
        alert("Room code can only contain uppercase letters and numbers.");
      } else {
        onSubmitCode(trimmedCode);
      }
    } else {
      alert("Please enter a room code.");
    }
  };

  return (
    <form onSubmit={handleSubmit} className={styles.joinForm}>
      <h2 className={styles.title}>Enter Game Code</h2>
      <div className={styles.formGroup}>
        <label htmlFor="roomCodeInput" className={styles.label}>
          <MdKey size={25} />
        </label>
        <input
          type="text"
          id="roomCodeInput"
          value={roomCode}
          onChange={(e) => setRoomCode(e.target.value)}
          placeholder="ABCDEF"
          className={styles.inputField}
          maxLength={6}
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
