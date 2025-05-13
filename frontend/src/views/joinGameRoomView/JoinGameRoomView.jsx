import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import MainLayout from '../../layouts/mainLayout/MainLayout';
import styles from './JoinGameRoomView.module.css';

const JoinGameRoomView = () => {
    const { roomCode } = useParams();
    const [message, setMessage] = useState('');
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        // Tutaj w przyszłości będzie logika sprawdzania pokoju i dołączania
        // Na razie tylko wyświetlamy informację
        if (roomCode) {
            setMessage(`Attempting to join room with code: ${roomCode}`);
            // TODO: Wyślij żądanie do backendu, aby dołączyć do pokoju
            // np. fetch(`/api/gamerooms/${roomCode}/join`, { method: 'POST', ... })
            // lub przekieruj do strony logowania, jeśli użytkownik nie jest zalogowany,
            // a następnie po zalogowaniu spróbuj dołączyć.
        } else {
            setMessage('No room code provided.');
        }
        setIsLoading(false);
    }, [roomCode]);

    if (isLoading) {
        return (
            <MainLayout>
                <div className={styles.viewContainer}>
                    <p>Loading room information...</p>
                </div>
            </MainLayout>
        );
    }

    return (
        <MainLayout>
            <div className={styles.viewContainer}>
                <h2>Join Game Room</h2>
                <p>{message}</p>
                {/* Tutaj w przyszłości będzie formularz do podania nicku,
                    informacje o pokoju, lista graczy itp. */}
            </div>
        </MainLayout>
    );
};

export default JoinGameRoomView;