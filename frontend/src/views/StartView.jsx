// frontend/src/views/StartView.jsx
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Container, Row, Col, Form, Button } from 'react-bootstrap';
import styles from './StartView.module.css';
import MainLayout from '../layouts/mainLayout/MainLayout';

const StartView = () => {
  const [playersCount, setPlayersCount] = useState(12);
  const navigate = useNavigate();

  const handleStart = () => {
    // Tymczasowa nawigacja - później podmienimy na połączenie z API
    navigate(`/room?players=${playersCount}`);
  };

  return (
     <MainLayout>
      { <img src="../../assets/blood-hand.svg" alt="" /> }
     </MainLayout>
  );
};

export default StartView;