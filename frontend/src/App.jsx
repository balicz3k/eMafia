import React, { useEffect, useState } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import StartView from './views/StartView';

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
            <Route path="/" element={<StartView />} />
          </Routes>
        </BrowserRouter>
      );
}

export default App;