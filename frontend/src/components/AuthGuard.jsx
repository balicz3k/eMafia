import React, { useEffect, useState } from "react";
import { Navigate } from "react-router-dom";
import { isTokenExpired, refreshAccessToken } from "../utils/tokenUtils";

const AuthGuard = ({ children, requireAuth = true, requireAdmin = false }) => {
  const [isChecking, setIsChecking] = useState(true);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isAdmin, setIsAdmin] = useState(false);

  useEffect(() => {
    const checkAuth = async () => {
      try {
        const token = localStorage.getItem("token");

        if (!token) {
          if (requireAuth) {
            setIsAuthenticated(false);
          }
          setIsChecking(false);
          return;
        }

        
        if (isTokenExpired()) {
          console.log("Token expired, trying to refresh...");

          const refreshToken = localStorage.getItem("refreshToken");
          if (refreshToken) {
            try {
              await refreshAccessToken();
              
              const newToken = localStorage.getItem("token");
              if (newToken) {
                const { decodeJwt } = await import("../utils/decodeJwt");
                const decodedToken = decodeJwt(newToken);

                setIsAuthenticated(true);
                setIsAdmin(decodedToken.roles?.includes("ROLE_ADMIN") || false);
              }
            } catch (error) {
              console.error("Token refresh failed:", error);
              
              localStorage.clear();
              setIsAuthenticated(false);
              setIsAdmin(false);
            }
          } else {
            console.log("No refresh token available");
            localStorage.clear();
            setIsAuthenticated(false);
            setIsAdmin(false);
          }
        } else {
          
          const { decodeJwt } = await import("../utils/decodeJwt");
          const decodedToken = decodeJwt(token);

          setIsAuthenticated(true);
          setIsAdmin(decodedToken.roles?.includes("ROLE_ADMIN") || false);
        }
      } catch (error) {
        console.error("Auth check failed:", error);
        localStorage.clear();
        setIsAuthenticated(false);
        setIsAdmin(false);
      } finally {
        setIsChecking(false);
      }
    };

    checkAuth();
  }, [requireAuth]);

  if (isChecking) {
    return (
      <div
        style={{
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
          height: "100vh",
          fontSize: "1.2rem",
        }}
      >
        Checking authentication...
      </div>
    );
  }

  
  if (!requireAuth) {
    
    if (isAuthenticated) {
      return <Navigate to="/dashboard" replace />;
    }
    return children;
  }

  
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  
  if (requireAdmin && !isAdmin) {
    return <Navigate to="/dashboard" replace />;
  }

  return children;
};

export default AuthGuard;
