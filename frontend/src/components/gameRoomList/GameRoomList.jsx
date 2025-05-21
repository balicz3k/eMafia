import React from "react";
import GameRoomItem from "../gameRoomItem/GameRoomItem";
import styles from "./GameRoomList.module.css";

const GameRoomList = ({ rooms, currentUserId, onLeaveRoom, onEndRoom }) => {
  if (!rooms || rooms.length === 0) {
    return (
      <p className={styles.noRoomsMessage}>
        You are not part of any games yet, or no games match your search.
      </p>
    );
  }

  return (
    <div className={styles.listContainer}>
      {rooms.map((room) => (
        <GameRoomItem
          to
          fetch
          user
          games
          key={room.id || room.roomCode} // Użyj stabilnego klucza
          room={room}
          currentUserId={currentUserId}
          onLeave={onLeaveRoom}
          onEnd={onEndRoom}
        />
      ))}
    </div>
  );
};

export default GameRoomList;
