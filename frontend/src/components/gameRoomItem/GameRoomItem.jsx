import { useNavigate } from "react-router-dom";
import styles from "./GameRoomItem.module.css";

const GameRoomItem = ({ room, currentUserId, onLeave, onEnd }) => {
  const navigate = useNavigate();

  const isHost = room.hostId === currentUserId;

  const handleJoinRoom = () => {
    navigate(`/join/${room.roomCode}`);
  };

  const handleLeaveClick = (e) => {
    e.stopPropagation();
    if (
      window.confirm(`Are you sure you want to leave the room "${room.name}"?`)
    ) {
      onLeave(room.roomCode);
    }
  };

  const handleEndClick = (e) => {
    e.stopPropagation();
    if (
      window.confirm(
        `Are you sure you want to end the game "${room.name}"? This action cannot be undone.`,
      )
    ) {
      onEnd(room.roomCode);
    }
  };

  return (
    <div
      className={styles.roomItem}
      onClick={handleJoinRoom}
      role="button"
      tabIndex={0}
    >
      <div className={styles.roomInfo}>
        <h3 className={styles.roomName}>
          {room.name} ({room.roomCode})
        </h3>
        <p
          className={`${styles.roomStatus} ${
            styles[room.status?.toLowerCase().replace(/_/g, "")] || ""
          }`}
        >
          Status: {room.status ? room.status.replace(/_/g, " ") : "UNKNOWN"}
        </p>
        <p className={styles.roomPlayers}>
          Players: {room.currentPlayers}/{room.maxPlayers}
        </p>
      </div>
      <div className={styles.roomActions}>
        {isHost ? (
          <button
            onClick={handleEndClick}
            className={`${styles.actionButton} ${styles.endButton}`}
          >
            End Game
          </button>
        ) : (
          <button
            onClick={handleLeaveClick}
            className={`${styles.actionButton} ${styles.leaveButton}`}
          >
            Leave Game
          </button>
        )}
      </div>
    </div>
  );
};

export default GameRoomItem;
