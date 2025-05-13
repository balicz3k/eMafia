import React, { useEffect, useState } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import LoginView from './views/loginView/LoginView';
import RegisterView from './views/registerView/RegisterView';
import DashboardView from './views/dashboardView/DashboardView';
import ProtectedRoute from './components/ProtectedRoute';
import ProfileView from './views/profileView/ProfileView';
import AdminPanelView from './views/adminPanelView/AdminPanelView';
import CreateGameRoomForm from './views/createGameRoomView/CreateGameRoomView';

function App() {
    const [backendMessage, setBackendMessage] = useState('');
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetch('/api/test')
            .then(response => response.text())
            .then(data => {
                setBackendMessage(data);
                setLoading(false);
            })
            .catch(error => {
                setBackendMessage('Error connecting to backend ❌');
                setLoading(false);
            });
    }, []);

    return (
        <BrowserRouter>
          <Routes>
            <Route path="/" element={<LoginView />} />
            <Route path="/login" element={<LoginView />} />
            <Route path="/register" element={<RegisterView />} />
            <Route path="/dashboard" element={<ProtectedRoute><DashboardView /></ProtectedRoute>}/>
            <Route path="/profile"element={<ProtectedRoute><ProfileView /></ProtectedRoute>}/>
            <Route path="/admin" element={<ProtectedRoute><AdminPanelView /></ProtectedRoute>}/>
            <Route path="/create-room" element={<ProtectedRoute><CreateGameRoomForm /></ProtectedRoute>}/>
          </Routes>
        </BrowserRouter>
      );
}

export default App;