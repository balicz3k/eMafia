import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import styles from "./CreateGameRoomForm.module.css";
import httpClient from "../../utils/httpClient";

const CreateGameRoomForm = () => {
  const [roomName, setRoomName] = useState("");
  const [maxPlayers, setMaxPlayers] = useState(5);
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setIsLoading(true);

    try {
      // CreateGameRoomReq { name, maxPlayers }
      const resp = await httpClient.post("/api/game_rooms/create", {
        name: roomName.trim(),
        maxPlayers: Number(maxPlayers),
      });

      // CreateGameRoomResp { roomCode, name }
      const data = resp.data;

      // Navigate directly to the room view (poczekalnia)
      navigate(`/room/${data.roomCode}`);
    } catch (err) {
      console.error("Error creating room:", err);
      const msg =
        err?.response?.data?.message ||
        err?.message ||
        "Failed to create room.";
      setError(msg);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className={styles.formContainer}>
      <h2 className={styles.formTitle}>Create a Game Room</h2>
      <form onSubmit={handleSubmit} className={styles.roomForm}>
        <div className={styles.formGroup}>
          <input
            type="text"
            id="roomName"
            value={roomName}
            onChange={(e) => setRoomName(e.target.value)}
            placeholder="Room name"
            required
            minLength="3"
            maxLength="100"
          />
        </div>
        <div className={styles.formGroup}>
          <label htmlFor="maxPlayers">Max Players (2-20):</label>{" "}
          <input
            type="number"
            id="maxPlayers"
            value={maxPlayers}
            onChange={(e) => setMaxPlayers(e.target.value)}
            required
            min="2"
            max="20"
          />
        </div>
        <button
          type="submit"
          disabled={isLoading}
          className={styles.submitButton}
        >
          {isLoading ? "Creating..." : "Create Room"}
        </button>
      </form>

      {error && <p className={styles.errorMessage}>{error}</p>}
    </div>
  );
};

export default CreateGameRoomForm;
