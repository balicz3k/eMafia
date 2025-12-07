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
  const { user, isLoading: authLoading } = useAuth();
  const { rooms, loading, error, fetchRoomsByUserId } = useGameRooms();
  const [searchResults, setSearchResults] = useState(null);
  const [searchLoading, setSearchLoading] = useState(false);
  const [searchError, setSearchError] = useState("");
  const navigate = useNavigate();

  // Fetch user's rooms when auth is ready and user is available
  useEffect(() => {
    // Czekaj aż auth się załaduje i user będzie dostępny
    if (!authLoading && user?.id) {
      console.log("Auth ready, fetching rooms for user:", user.id);
      fetchRoomsByUserId(user.id);
    }
  }, [authLoading, user?.id, fetchRoomsByUserId]);

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
        if (user?.id) {
          await fetchRoomsByUserId(user.id);
        }

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
    [fetchRoomsByUserId, searchResults, user?.id]
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
        if (user?.id) {
          await fetchRoomsByUserId(user.id);
        }

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
    [fetchRoomsByUserId, searchResults, user?.id]
  );

  const gamesToDisplay = searchResults !== null ? searchResults : rooms;
  // Pokaż loading gdy: auth się ładuje LUB (nie ma search results I rooms się ładują)
  const isLoading = authLoading || (searchResults !== null ? searchLoading : loading);
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
