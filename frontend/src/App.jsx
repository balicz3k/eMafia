import React, { useEffect, useState } from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import LoginView from "./views/loginView/LoginView";
import RegisterView from "./views/registerView/RegisterView";
import DashboardView from "./views/dashboardView/DashboardView";
import ProfileView from "./views/profileView/ProfileView";
import AdminPanelView from "./views/adminPanelView/AdminPanelView";
import CreateRoomView from "./views/createGameRoomView/CreateGameRoomView";
import JoinRoomView from "./views/joinGameRoomView/JoinGameRoomView";
import EnterRoomCodeView from "./views/enterRoomCodeView/EnterRoomCodeView";
import AuthGuard from "./components/AuthGuard";
import { AuthProvider } from "./components/AuthProvider";

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          {/* Public routes - redirect to dashboard if authenticated */}
          <Route
            path="/"
            element={
              <AuthGuard requireAuth={false}>
                <Navigate to="/login" replace />
              </AuthGuard>
            }
          />
          <Route
            path="/login"
            element={
              <AuthGuard requireAuth={false}>
                <LoginView />
              </AuthGuard>
            }
          />
          <Route
            path="/register"
            element={
              <AuthGuard requireAuth={false}>
                <RegisterView />
              </AuthGuard>
            }
          />

          {/* Protected routes - require authentication */}
          <Route
            path="/dashboard"
            element={
              <AuthGuard requireAuth={true}>
                <DashboardView />
              </AuthGuard>
            }
          />
          <Route
            path="/profile"
            element={
              <AuthGuard requireAuth={true}>
                <ProfileView />
              </AuthGuard>
            }
          />
          <Route
            path="/create-room"
            element={
              <AuthGuard requireAuth={true}>
                <CreateRoomView />
              </AuthGuard>
            }
          />
          <Route
            path="/enter-code"
            element={
              <AuthGuard requireAuth={true}>
                <EnterRoomCodeView />
              </AuthGuard>
            }
          />
          <Route
            path="/join/:roomCode"
            element={
              <AuthGuard requireAuth={true}>
                <JoinRoomView />
              </AuthGuard>
            }
          />

          {/* Admin routes - require authentication + admin role */}
          <Route
            path="/admin"
            element={
              <AuthGuard requireAuth={true} requireAdmin={true}>
                <AdminPanelView />
              </AuthGuard>
            }
          />

          {/* Fallback route */}
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
