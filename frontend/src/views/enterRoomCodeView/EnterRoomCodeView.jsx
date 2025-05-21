import React from "react";
import { useNavigate } from "react-router-dom";
import MainLayout from "../../layouts/mainLayout/MainLayout";
import EnterRoomCodeForm from "../../components/enterRoomCodeForm/EnterRoomCodeForm";
import styles from "./EnterRoomCodeView.module.css";

const EnterRoomCodeView = () => {
  const navigate = useNavigate();

  const handleCodeSubmit = (submittedCode) => {
    navigate(`/join/${submittedCode}`); // Przekieruj do istniejącego JoinGameRoomView
  };

  return (
    <MainLayout>
      <div className={styles.viewContainer}>
        <h2 className={styles.title}>Join Game by Code</h2>
        <EnterRoomCodeForm onSubmitCode={handleCodeSubmit} />
      </div>
    </MainLayout>
  );
};

export default EnterRoomCodeView;
