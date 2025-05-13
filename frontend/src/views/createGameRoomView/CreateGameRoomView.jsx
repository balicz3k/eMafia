import React from 'react';
import MainLayout from '../../layouts/mainLayout/MainLayout';
import CreateRoomForm from '../../components/createGameRoomForm/CreateGameRoomForm';
import styles from './CreateGameRoomView.module.css';

const CreateGameRoomView = () => {
    return (
        <MainLayout>
            <div className={styles.viewContainer}>
                <CreateRoomForm />
            </div>
        </MainLayout>
    );
};

export default CreateGameRoomView;