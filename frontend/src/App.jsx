import React from "react";
import {BrowserRouter, Navigate, Route, Routes} from "react-router-dom";
import LoginView from "./views/loginView/LoginView";
import RegisterView from "./views/registerView/RegisterView";
import DashboardView from "./views/dashboardView/DashboardView";
import ProfileView from "./views/profileView/ProfileView";
import AdminPanelView from "./views/adminPanelView/AdminPanelView";
import CreateRoomView from "./views/createGameRoomView/CreateGameRoomView";
import JoinRoomView from "./views/joinGameRoomView/JoinGameRoomView";
import EnterRoomCodeView from "./views/enterRoomCodeView/EnterRoomCodeView";
import AuthGuard from "./components/AuthGuard";
import {AuthProvider} from "./components/AuthProvider";
import GameView from "./views/gameView/GameView";
import GameRoomView from "./views/gameRoomView/GameRoomView";

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
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

          <Route
            path="/game-room/:roomCode"
            element={
              <AuthGuard requireAuth={true}>
                <GameRoomView />
              </AuthGuard>
            }
          />

          <Route
            path="/admin"
            element={
              <AuthGuard requireAuth={true} requireAdmin={true}>
                <AdminPanelView />
              </AuthGuard>
            }
          />

          <Route
            path="/game/:roomCode"
            element={
              <AuthGuard requireAuth={true}>
                <GameView />
              </AuthGuard>
            }
          />

          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
