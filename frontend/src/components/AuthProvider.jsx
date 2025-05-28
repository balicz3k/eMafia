import React, { createContext, useContext, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { isTokenExpired, refreshAccessToken } from "../utils/tokenUtils";
import { decodeJwt } from "../utils/decodeJwt";

const AuthContext = createContext();

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
};

export const AuthProvider = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isAdmin, setIsAdmin] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [user, setUser] = useState(null);
  const navigate = useNavigate();

  const logout = (redirectToLogin = true) => {
    console.log("Logging out user...");

    
    const refreshToken = localStorage.getItem("refreshToken");
    if (refreshToken) {
      fetch(`${process.env.REACT_APP_API_BASE_URL}/api/auth/logout`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
        body: JSON.stringify({ refreshToken }),
      }).catch((err) => console.log("Logout request failed:", err));
    }

    localStorage.clear();
    setIsAuthenticated(false);
    setIsAdmin(false);
    setUser(null);

    if (redirectToLogin) {
      navigate("/login", { replace: true });
    }
  };

  const checkTokenAndRefresh = async () => {
    const token = localStorage.getItem("token");

    if (!token) {
      return false;
    }

    try {
      if (isTokenExpired()) {
        console.log("Token expired, attempting refresh...");

        const refreshToken = localStorage.getItem("refreshToken");
        if (refreshToken) {
          try {
            await refreshAccessToken();
            const newToken = localStorage.getItem("token");
            const decodedToken = decodeJwt(newToken);

            setIsAuthenticated(true);
            setIsAdmin(decodedToken.roles?.includes("ROLE_ADMIN") || false);
            setUser({
              id: decodedToken.sub,
              username: decodedToken.username,
              roles: decodedToken.roles,
            });
            return true;
          } catch (error) {
            console.error("Token refresh failed:", error);
            logout(false); 
            return false;
          }
        } else {
          console.log("No refresh token available");
          logout(false);
          return false;
        }
      } else {
        
        const decodedToken = decodeJwt(token);
        setIsAuthenticated(true);
        setIsAdmin(decodedToken.roles?.includes("ROLE_ADMIN") || false);
        setUser({
          id: decodedToken.sub,
          username: decodedToken.username,
          roles: decodedToken.roles,
        });
        return true;
      }
    } catch (error) {
      console.error("Token validation failed:", error);
      logout(false);
      return false;
    }
  };

  const initializeAuth = async () => {
    setIsLoading(true);
    await checkTokenAndRefresh();
    setIsLoading(false);
  };

  useEffect(() => {
    initializeAuth();
  }, []);

  
  useEffect(() => {
    if (isAuthenticated) {
      const interval = setInterval(async () => {
        const isValid = await checkTokenAndRefresh();
        if (!isValid) {
          console.log("Token validation failed, user will be logged out");
        }
      }, 30000); 

      return () => clearInterval(interval);
    }
  }, [isAuthenticated]);

  const value = {
    isAuthenticated,
    isAdmin,
    isLoading,
    user,
    logout,
    checkTokenAndRefresh,
    setIsAuthenticated,
    setIsAdmin,
    setUser,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
