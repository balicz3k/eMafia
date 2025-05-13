import React, { useState } from 'react';
import { QRCodeSVG } from 'qrcode.react';
import styles from './CreateGameRoomForm.module.css';

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL

const CreateGameRoomForm = () => {
    const [roomName, setRoomName] = useState('');
    const [maxPlayers, setMaxPlayers] = useState(5);
    const [createdRoomInfo, setCreatedRoomInfo] = useState(null);
    const [error, setError] = useState('');
    const [isLoading, setIsLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setCreatedRoomInfo(null);
        setIsLoading(true);

        const token = localStorage.getItem("token");
        if (!token) {
            setError("You must be logged in to create a room.");
            setIsLoading(false);
            return;
        }

        try {
            const response = await fetch(`${API_BASE_URL}/api/gamerooms/create`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`,
                },
                body: JSON.stringify({ name: roomName, maxPlayers: parseInt(maxPlayers, 10) }),
            });

            if (response.status === 201) {
                const data = await response.json();
                setCreatedRoomInfo(data);
                setRoomName(''); // Resetuj formularz
                setMaxPlayers(5);
            } else {
                const errorText = await response.text();
                setError(errorText || 'Failed to create room. Status: ' + response.status);
            }
        } catch (err) {
            console.error("Error creating room:", err);
            setError('An error occurred while creating the room.');
        } finally {
            setIsLoading(false);
        }
    };

    const getFullJoinLink = () => {
        if (createdRoomInfo && createdRoomInfo.roomCode) {
            // INF Zakładamy, że frontend działa na tym samym hoście i porcie co backend (dla uproszczenia)
            // W produkcji, to URL frontendu powinien być użyty.
            // Można też pobrać bazowy URL frontendu z konfiguracji lub window.location.origin
            const baseUrl = window.location.origin;
            return `${baseUrl}${createdRoomInfo.joinLinkPath}${createdRoomInfo.roomCode}`;
        }
        return '';
    };

    return (
        <div className={styles.formContainer}>
            <h2>Create a New Game Room</h2>
            <form onSubmit={handleSubmit} className={styles.roomForm}>
                <div className={styles.formGroup}>
                    <label htmlFor="roomName">Room Name:</label>
                    <input
                        type="text"
                        id="roomName"
                        value={roomName}
                        onChange={(e) => setRoomName(e.target.value)}
                        required
                        minLength="3"
                        maxLength="100"
                    />
                </div>
                <div className={styles.formGroup}>
                    <label htmlFor="maxPlayers">Max Players (2-20):</label>
                    <input
                        type="number"
                        id="maxPlayers"
                        value={maxPlayers}
                        onChange={(e) => setMaxPlayers(e.target.value)}
                        required
                        min="2"
                        max="20"
                    />
                </div>
                <button type="submit" disabled={isLoading} className={styles.submitButton}>
                    {isLoading ? 'Creating...' : 'Create Room'}
                </button>
            </form>

            {error && <p className={styles.errorMessage}>{error}</p>}

            {createdRoomInfo && (
                <div className={styles.roomInfoContainer}>
                    <h3>Room Created Successfully!</h3>
                    <p><strong>Room Name:</strong> {createdRoomInfo.name}</p>
                    <p><strong>Room Code:</strong> <span className={styles.roomCode}>{createdRoomInfo.roomCode}</span></p>
                    <p><strong>Host:</strong> {createdRoomInfo.hostUsername}</p>
                    <p><strong>Max Players:</strong> {createdRoomInfo.maxPlayers}</p>
                    <p>
                        <strong>Join Link:</strong>
                        <a href={getFullJoinLink()} target="_blank" rel="noopener noreferrer" className={styles.joinLink}>
                            {getFullJoinLink()}
                        </a>
                    </p>
                    <div className={styles.qrCodeContainer}>
                        <p><strong>Scan QR Code to Join:</strong></p>
                        <QRCodeSVG value={getFullJoinLink()} size={128} level="H" />
                    </div>
                    <p className={styles.shareInstructions}>Share the Room Code or Join Link with your friends!</p>
                </div>
            )}
        </div>
    );
};

export default CreateGameRoomForm;