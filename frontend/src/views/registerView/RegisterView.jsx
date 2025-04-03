// frontend/src/views/StartView.jsx
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Container, Row, Col, Form, Button } from 'react-bootstrap';
import styles from './RegisterView.module.css';
import MainLayout from '../../layouts/mainLayout/MainLayout';
import RegisterForm from '../../components/registerForm/RegisterForm';
import bloodHand from '../../assets/blood-hand.svg';

const StartView = () => {
  const [playersCount, setPlayersCount] = useState(12);
  const navigate = useNavigate();

  const handleStart = () => {
    // Tymczasowa nawigacja - później podmienimy na połączenie z API
    navigate(`/room?players=${playersCount}`);
  };

  return (
     <MainLayout>
      <div className={styles.mainContainer}>
        {/* Lewa kolumna */}
        <div className={styles.leftColumn}>
          <RegisterForm />
        </div>

        {/* Prawa kolumna */}
        <div className={styles.rightColumn}>
          <img src={bloodHand} alt="Blood Hand" />
        </div>
      </div>
     </MainLayout>
  );
};

export default StartView;