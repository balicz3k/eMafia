import React, { useState, useEffect, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import MainLayout from "../../layouts/mainLayout/MainLayout";
import SearchGameRoomBar from "../../components/searchGameRoomBar/SearchGameRoomBar";
import GameRoomList from "../../components/gameRoomList/GameRoomList";
import styles from "./DashboardView.module.css";
import { decodeJwt } from "../../utils/decodeJwt";

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL;

const DashboardView = () => {
  const [userGames, setUserGames] = useState([]);
  const [searchResults, setSearchResults] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");
  const [currentUser, setCurrentUser] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const token = localStorage.getItem("token");
    if (token) {
      try {
        const decoded = decodeJwt(token);
        setCurrentUser(decoded);
      } catch (e) {
        console.error("Failed to decode JWT", e);
      }
    }
  }, []);

  const fetchUserGames = useCallback(async () => {
    if (!currentUser) return;
    setIsLoading(true);
    setError("");
    const token = localStorage.getItem("token");
    try {
      const response = await fetch(`${API_BASE_URL}/api/gamerooms/my-rooms`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      if (!response.ok) {
        throw new Error("Failed to fetch user games");
      }
      const data = await response.json();
      setUserGames(data);
    } catch (err) {
      console.error("Error fetching user games:", err);
      setError(err.message || "Could not fetch your games.");
    } finally {
      setIsLoading(false);
    }
  }, [currentUser]);

  useEffect(() => {
    fetchUserGames();
  }, [fetchUserGames]);

  const handleSearch = async (searchTerm) => {
    if (!searchTerm.trim()) {
      setSearchResults(null);
      return;
    }
    setIsLoading(true);
    setError("");
    const token = localStorage.getItem("token");
    try {
      const response = await fetch(
        `${API_BASE_URL}/api/gamerooms/search?name=${encodeURIComponent(
          searchTerm
        )}`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      if (!response.ok) {
        throw new Error("Failed to search games");
      }
      const data = await response.json();
      setSearchResults(data);
    } catch (err) {
      console.error("Error searching games:", err);
      setError(err.message || "Could not perform search.");
      setSearchResults([]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleLeaveRoom = async (roomCode) => {
    const token = localStorage.getItem("token");
    try {
      const response = await fetch(
        `${API_BASE_URL}/api/gamerooms/${roomCode}/leave`,
        {
          method: "POST",
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      if (!response.ok) {
        const errorData = await response
          .json()
          .catch(() => ({ message: "Failed to leave room." }));
        throw new Error(errorData.message);
      }
      fetchUserGames();
      if (searchResults) {
        const updatedResults = searchResults.filter(
          (room) => room.roomCode !== roomCode
        );
        setSearchResults(updatedResults);
      }
    } catch (err) {
      console.error("Error leaving room:", err);
      alert(`Error: ${err.message}`);
    }
  };

  const handleEndRoom = async (roomCode) => {
    const token = localStorage.getItem("token");
    try {
      const response = await fetch(
        `${API_BASE_URL}/api/gamerooms/${roomCode}/end`,
        {
          method: "POST",
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      if (!response.ok) {
        const errorData = await response
          .json()
          .catch(() => ({ message: "Failed to end game." }));
        throw new Error(errorData.message);
      }

      fetchUserGames();
      if (searchResults) {
        const updatedResults = searchResults.filter(
          (room) => room.roomCode !== roomCode
        );
        setSearchResults(updatedResults);
      }
    } catch (err) {
      console.error("Error ending room:", err);
      alert(`Error: ${err.message}`);
    }
  };

  const gamesToDisplay = searchResults !== null ? searchResults : userGames;

  return (
    <MainLayout>
      <div className={styles.dashboardContainer}>
        <header className={styles.header}>
          <SearchGameRoomBar onSearch={handleSearch} />
          <button
            className={styles.newGameButton}
            onClick={() => navigate("/createroom")}
          >
            New Game
          </button>
        </header>

        {isLoading && <p className={styles.loadingMessage}>Loading games...</p>}
        {error && <p className={styles.errorMessage}>{error}</p>}

        {!isLoading && !error && (
          <section>
            <h2>{searchResults !== null ? "Search Results" : "Your Games"}</h2>
            <GameRoomList
              rooms={gamesToDisplay}
              currentUserId={currentUser?.sub}
              onLeaveRoom={handleLeaveRoom}
              onEndRoom={handleEndRoom}
            />
          </section>
        )}
      </div>
    </MainLayout>
  );
};

export default DashboardView;
