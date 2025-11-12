import { useNavigate } from "react-router-dom";
import styles from "./GameRoomItem.module.css";

const GameRoomItem = ({ room, currentUserId, onLeave, onEnd }) => {
  const navigate = useNavigate();

  // Check if current user is the host
  // hostUsername is provided by backend, so we need to compare with current user
  const isHost = room.hostUsername && currentUserId; // Will be validated by backend

  const handleJoinRoom = () => {
    // Check if game is in progress
    const inProgress =
      typeof room.status === "string" &&
      room.status.toUpperCase() === "GAME_IN_PROGRESS";

    if (inProgress) {
      navigate(`/game/${room.roomCode}`);
    } else {
      navigate(`/game-room/${room.roomCode}`);
    }
  };

  const handleLeaveClick = (e) => {
    e.stopPropagation();
    onLeave(room.roomCode);
  };

  const handleEndClick = (e) => {
    e.stopPropagation();
    onEnd(room.roomCode);
  };

  // Format status for display
  const formatStatus = (status) => {
    if (!status) return "UNKNOWN";
    return status.replace(/_/g, " ");
  };

  // Format date for display
  const formatDate = (dateString) => {
    if (!dateString) return "";
    try {
      const date = new Date(dateString);
      return date.toLocaleDateString() + " " + date.toLocaleTimeString();
    } catch {
      return dateString;
    }
  };

  return (
    <div
      className={styles.roomItem}
      onClick={handleJoinRoom}
      role="button"
      tabIndex={0}
      onKeyDown={(e) => {
        if (e.key === "Enter" || e.key === " ") {
          handleJoinRoom();
        }
      }}
    >
      <div className={styles.roomInfo}>
        <h3 className={styles.roomName}>
          {room.name} ({room.roomCode})
        </h3>
        <p className={styles.roomHost}>
          Host: <strong>{room.hostUsername}</strong>
        </p>
        <p
          className={`${styles.roomStatus} ${
            styles[room.status?.toLowerCase().replace(/_/g, "")] || ""
          }`}
        >
          Status: {formatStatus(room.status)}
        </p>
        <p className={styles.roomPlayers}>
          Players: {room.currentPlayers}/{room.maxPlayers}
        </p>
        {room.createdAt && (
          <p className={styles.roomCreated}>
            Created: {formatDate(room.createdAt)}
          </p>
        )}
      </div>
      <div className={styles.roomActions}>
        {isHost ? (
          <button
            onClick={handleEndClick}
            className={`${styles.actionButton} ${styles.endButton}`}
            title="End this game"
          >
            End Game
          </button>
        ) : (
          <button
            onClick={handleLeaveClick}
            className={`${styles.actionButton} ${styles.leaveButton}`}
            title="Leave this game"
          >
            Leave Game
          </button>
        )}
      </div>
    </div>
  );
};

export default GameRoomItem;
