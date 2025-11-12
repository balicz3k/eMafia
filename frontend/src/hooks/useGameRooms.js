import {useCallback, useState} from "react";
import {getCurrentUserId} from "../utils/userUtils";
import httpClient from "../utils/httpClient";

/**
 * Hook to fetch game rooms by filter (roomCode or userId)
 * @returns {Object} { rooms, loading, error, fetchRoomsByCode, fetchRoomsByUserId, fetchMyRooms }
 */
export const useGameRooms = () => {
  const [rooms, setRooms] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  /**
   * Fetch rooms by room code
   * @param {string} roomCode - The room code to search for
   */
  const fetchRoomsByCode = useCallback(async (roomCode) => {
    setLoading(true);
    setError("");

    try {
      const response = await httpClient.post("/api/game_rooms/info", {
        roomCode,
      });

      setRooms(response.data.rooms || []);
    } catch (err) {
      console.error("Error fetching room by code:", err);
      const errorMessage =
        err.response?.data?.message ||
        err.message ||
        "Failed to fetch room by code";
      setError(errorMessage);
      setRooms([]);
    } finally {
      setLoading(false);
    }
  }, []);

  /**
   * Fetch rooms for a specific user
   * @param {string} userId - The user ID to fetch rooms for
   */
  const fetchRoomsByUserId = useCallback(async (userId) => {
    setLoading(true);
    setError("");

    try {
      const response = await httpClient.post("/api/game_rooms/info", {
        userId,
      });

      setRooms(response.data.rooms || []);
    } catch (err) {
      console.error("Error fetching user rooms:", err);
      const errorMessage =
        err.response?.data?.message ||
        err.message ||
        "Failed to fetch user rooms";
      setError(errorMessage);
      setRooms([]);
    } finally {
      setLoading(false);
    }
  }, []);

  /**
   * Fetch rooms for current authenticated user
   */
  const fetchMyRooms = useCallback(async () => {
    const userId = getCurrentUserId();
    if (!userId) {
      setError("User not authenticated");
      setRooms([]);
      return;
    }

    await fetchRoomsByUserId(userId);
  }, [fetchRoomsByUserId]);

  return {
    rooms,
    loading,
    error,
    fetchRoomsByCode,
    fetchRoomsByUserId,
    fetchMyRooms,
  };
};
