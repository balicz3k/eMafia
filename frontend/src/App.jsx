import React, { useEffect, useState } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import LoginView from './views/loginView/LoginView';
import RegisterView from './views/registerView/RegisterView';

function App() {
    const [backendMessage, setBackendMessage] = useState('');
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        // Test połączenia z backendem
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
          </Routes>
        </BrowserRouter>
      );
}

export default App;