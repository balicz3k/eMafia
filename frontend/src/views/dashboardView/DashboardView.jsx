import React, { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import MainLayout from "../../layouts/mainLayout/MainLayout";
import SearchGameRoomBar from "../../components/searchGameRoomBar/SearchGameRoomBar";
import GameRoomList from "../../components/gameRoomList/GameRoomList";
import styles from "./DashboardView.module.css";
import { useAuth } from "../../components/AuthProvider";
import { useGameRooms } from "../../hooks/useGameRooms";
import httpClient from "../../utils/httpClient";

const DashboardView = () => {
  const { user } = useAuth();
  const { rooms, loading, error, fetchMyRooms } = useGameRooms();
  const [searchResults, setSearchResults] = useState(null);
  const [searchLoading, setSearchLoading] = useState(false);
  const [searchError, setSearchError] = useState("");
  const navigate = useNavigate();

  // Fetch user's rooms on component mount
  useEffect(() => {
    if (user?.id) {
      fetchMyRooms();
    }
  }, [user?.id, fetchMyRooms]);

  const handleSearch = useCallback(
    async (searchTerm) => {
      if (!searchTerm.trim()) {
        setSearchResults(null);
        setSearchError("");
        return;
      }

      setSearchLoading(true);
      setSearchError("");

      try {
        const response = await httpClient.get("/api/game_rooms/search", {
          params: { name: searchTerm },
        });
        setSearchResults(response.data || []);
      } catch (err) {
        console.error("Error searching games:", err);
        const msg = err?.response?.data?.message || err?.message || "Could not perform search.";
        setSearchError(msg);
        setSearchResults([]);
      } finally {
        setSearchLoading(false);
      }
    },
    []
  );

  const handleLeaveRoom = useCallback(
    async (roomCode) => {
      if (!window.confirm("Are you sure you want to leave this room?")) {
        return;
      }

      try {
        await httpClient.post(`/api/game_rooms/leave/${roomCode}`, { roomCode });

        // Refresh rooms after leaving
        await fetchMyRooms();

        // Update search results if they exist
        if (searchResults) {
          const updatedResults = searchResults.filter(
            (room) => room.roomCode !== roomCode
          );
          setSearchResults(updatedResults);
        }
      } catch (err) {
        console.error("Error leaving room:", err);
        const msg = err?.response?.data?.message || err?.message || "Could not leave room";
        alert(`Error: ${msg}`);
      }
    },
    [fetchMyRooms, searchResults]
  );

  const handleEndRoom = useCallback(
    async (roomCode) => {
      if (
        !window.confirm(
          "Are you sure you want to end this game? This action cannot be undone."
        )
      ) {
        return;
      }

      try {
        await httpClient.post(`/api/game_rooms/leave/${roomCode}`, { roomCode });

        // Refresh rooms after ending
        await fetchMyRooms();

        // Update search results if they exist
        if (searchResults) {
          const updatedResults = searchResults.filter(
            (room) => room.roomCode !== roomCode
          );
          setSearchResults(updatedResults);
        }
      } catch (err) {
        console.error("Error ending room:", err);
        const msg = err?.response?.data?.message || err?.message || "Could not end room";
        alert(`Error: ${msg}`);
      }
    },
    [fetchMyRooms, searchResults]
  );

  const gamesToDisplay = searchResults !== null ? searchResults : rooms;
  const isLoading = searchResults !== null ? searchLoading : loading;
  const displayError = searchResults !== null ? searchError : error;

  return (
    <MainLayout>
      <div className={styles.dashboardContainer}>
        <header className={styles.header}>
          <button
            className={styles.newGameButton}
            onClick={() => navigate("/create-room")}
          >
            New Game
          </button>
          <SearchGameRoomBar onSearch={handleSearch} />
        </header>

        {isLoading && <p className={styles.loadingMessage}>Loading games...</p>}
        {displayError && <p className={styles.errorMessage}>{displayError}</p>}

        {!isLoading && !displayError && (
          <section>
            <h2>{searchResults !== null ? "Search Results" : "Your Games"}</h2>
            {gamesToDisplay.length === 0 ? (
              <div className={styles.emptyState}>
                <p className={styles.emptyMessage}>
                  {searchResults !== null
                    ? "No games match your search."
                    : "You are not currently participating in any games."}
                </p>
                {searchResults === null && (
                  <p className={styles.emptySubtext}>
                    Create a new game or search for existing ones to join!
                  </p>
                )}
              </div>
            ) : (
              <GameRoomList
                rooms={gamesToDisplay}
                currentUserId={user?.id}
                onLeaveRoom={handleLeaveRoom}
                onEndRoom={handleEndRoom}
              />
            )}
          </section>
        )}
      </div>
    </MainLayout>
  );
};

export default DashboardView;
