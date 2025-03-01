import React, { useEffect, useState } from 'react';

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
        <div className="App">
            <h1>Mafia Game 🕵️♂️</h1>
            <div className="connection-status">
                {loading ? (
                    <p>Connecting to server...</p>
                ) : (
                    <>
                        <p>{backendMessage}</p>
                        <button onClick={() => window.location.reload()}>
                            Refresh Connection
                        </button>
                    </>
                )}
            </div>
        </div>
    );
}

export default App;