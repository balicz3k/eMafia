import React, { useEffect, useState } from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import LoginView from "./views/loginView/LoginView";
import RegisterView from "./views/registerView/RegisterView";
import DashboardView from "./views/dashboardView/DashboardView";
import ProfileView from "./views/profileView/ProfileView";
import AdminPanelView from "./views/adminPanelView/AdminPanelView";
import CreateRoomView from "./views/createGameRoomView/CreateGameRoomView";
import JoinRoomView from "./views/joinGameRoomView/JoinGameRoomView";
import ProtectedRoute from "./components/ProtectedRoute";
import AdminRoute from "./components/AdminRoute";
import EnterRoomCodeView from "./views/enterRoomCodeView/EnterRoomCodeView";
import { decodeJwt } from "./utils/decodeJwt";

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isAdmin, setIsAdmin] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem("token");
    if (token) {
      try {
        const decodedToken = decodeJwt(token);
        const currentTime = Date.now() / 1000;
        if (decodedToken.exp > currentTime) {
          setIsAuthenticated(true);
          if (decodedToken.roles && decodedToken.roles.includes("ROLE_ADMIN")) {
            setIsAdmin(true);
          }
        } else {
          localStorage.removeItem("token");
        }
      } catch (error) {
        console.error("Invalid token:", error);
        localStorage.removeItem("token");
      }
    }
    setIsLoading(false);
  }, []);

  if (isLoading) {
    return <div>Loading application...</div>;
  }

  return (
    <BrowserRouter>
      <Routes>
        <Route
          path="/"
          element={
            !isAuthenticated ? <LoginView /> : <Navigate to="/dashboard" />
          }
        />
        <Route
          path="/login"
          element={
            !isAuthenticated ? <LoginView /> : <Navigate to="/dashboard" />
          }
        />
        <Route
          path="/register"
          element={
            !isAuthenticated ? <RegisterView /> : <Navigate to="/dashboard" />
          }
        />
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute isAuthenticated={isAuthenticated}>
              <DashboardView />
            </ProtectedRoute>
          }
        />
        <Route
          path="/profile"
          element={
            <ProtectedRoute isAuthenticated={isAuthenticated}>
              <ProfileView />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin"
          element={
            <AdminRoute isAuthenticated={isAuthenticated} isAdmin={isAdmin}>
              <AdminPanelView />
            </AdminRoute>
          }
        />
        <Route
          path="/create-room"
          element={
            <ProtectedRoute isAuthenticated={isAuthenticated}>
              <CreateRoomView />
            </ProtectedRoute>
          }
        />
        <Route
          path="/enter-code"
          element={
            <ProtectedRoute isAuthenticated={isAuthenticated}>
              <EnterRoomCodeView />
            </ProtectedRoute>
          }
        />
        <Route
          path="/join/:roomCode"
          element={
            <ProtectedRoute isAuthenticated={isAuthenticated}>
              {" "}
              <JoinRoomView />
            </ProtectedRoute>
          }
        />
        {/* <Route path="*" element={<NotFoundView />} /> */}
      </Routes>
    </BrowserRouter>
  );
}

export default App;
