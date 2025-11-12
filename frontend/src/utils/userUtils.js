import { decodeJwt } from "./decodeJwt";

/**
 * Get current user ID from JWT token stored in localStorage
 * @returns {string|null} User ID (UUID) or null if not found
 */
export const getCurrentUserId = () => {
  const token = localStorage.getItem("token");
  if (!token) {
    return null;
  }

  const decoded = decodeJwt(token);
  if (!decoded || !decoded.sub) {
    console.warn("Could not extract user ID from token");
    return null;
  }

  return decoded.sub;
};

/**
 * Get current user info from JWT token
 * @returns {Object|null} User object with id, username, roles or null
 */
export const getCurrentUser = () => {
  const token = localStorage.getItem("token");
  if (!token) {
    return null;
  }

  const decoded = decodeJwt(token);
  if (!decoded) {
    return null;
  }

  return {
    id: decoded.sub,
    username: decoded.username,
    roles: decoded.roles || [],
  };
};

/**
 * Check if user is authenticated
 * @returns {boolean} true if token exists and is valid
 */
export const isUserAuthenticated = () => {
  const token = localStorage.getItem("token");
  return !!token;
};
