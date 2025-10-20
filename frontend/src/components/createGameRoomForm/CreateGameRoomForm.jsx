import React, { useState } from "react";
import { QRCodeSVG } from "qrcode.react";
import styles from "./CreateGameRoomForm.module.css";
import { useNavigate } from "react-router-dom";

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL;

const CreateGameRoomForm = () => {
  const [roomName, setRoomName] = useState("");
  const [maxPlayers, setMaxPlayers] = useState(5);
  const [createdRoomInfo, setCreatedRoomInfo] = useState(null);
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [isFormVisible, setIsFormVisible] = useState(true);

  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setCreatedRoomInfo(null);
    setIsLoading(true);

    const token = localStorage.getItem("token");
    if (!token) {
      setError("You must be logged in to create a room.");
      setIsLoading(false);
      return;
    }

    try {
      const response = await fetch(`${API_BASE_URL}/api/gamerooms/create`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          name: roomName,
          maxPlayers: parseInt(maxPlayers, 10),
        }),
      });

      if (response.status === 201) {
        const data = await response.json();
        setCreatedRoomInfo(data);
        setRoomName("");
        setMaxPlayers(5);
        setIsFormVisible(false);
      } else {
        const errorText = await response.text();
        setError(
          errorText || "Failed to create room. Status: " + response.status,
        );
        setIsFormVisible(true);
      }
    } catch (err) {
      console.error("Error creating room:", err);
      setError("An error occurred while creating the room.");
      setIsFormVisible(true);
    } finally {
      setIsLoading(false);
    }
  };

  const getFullJoinLink = () => {
    if (createdRoomInfo && createdRoomInfo.roomCode) {
      const baseUrl = window.location.origin;
      return `${baseUrl}${createdRoomInfo.joinLinkPath}${createdRoomInfo.roomCode}`;
    }
    return "";
  };

  const handleJoinRoom = () => {
    if (createdRoomInfo?.joinLinkPath && createdRoomInfo?.roomCode) {
      navigate(`${createdRoomInfo.joinLinkPath}${createdRoomInfo.roomCode}`);
    }
  };

  return (
    <div className={styles.formContainer}>
      {isFormVisible && (
        <>
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
        </>
      )}

      {error && <p className={styles.errorMessage}>{error}</p>}

      {createdRoomInfo && (
        <div className={styles.roomInfoContainer}>
          <h3>Room Created Successfully!</h3>
          <p>
            <strong>Room Name:</strong> {createdRoomInfo.name}
          </p>
          <p>
            <strong>Room Code:</strong>{" "}
            <span className={styles.roomCode}>{createdRoomInfo.roomCode}</span>
          </p>
          <p>
            <strong>Host:</strong> {createdRoomInfo.hostUsername}
          </p>
          <p>
            <strong>Max Players:</strong> {createdRoomInfo.maxPlayers}
          </p>
          <p>
            <strong>Join Link:</strong>
            <a
              href={getFullJoinLink()}
              target="_blank"
              rel="noopener noreferrer"
              className={styles.joinLink}
            >
              {getFullJoinLink()}
            </a>
          </p>
          <div className={styles.qrCodeContainer}>
            <p>
              <strong>Scan QR Code to Join:</strong>
            </p>
            <QRCodeSVG value={getFullJoinLink()} size={128} level="H" />
          </div>
          <p className={styles.shareInstructions}>
            Share the Room Code or Join Link with your friends!
          </p>
          {!isFormVisible && (
            <button
              onClick={handleJoinRoom}
              className={styles.submitButton}
              style={{ marginTop: "20px" }}
            >
              Join to the room
            </button>
          )}
        </div>
      )}
    </div>
  );
};

export default CreateGameRoomForm;
