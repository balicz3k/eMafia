import React from "react";
import { useNavigate } from "react-router-dom";
import MainLayout from "../../layouts/mainLayout/MainLayout";
import EnterRoomCodeForm from "../../components/enterRoomCodeForm/EnterRoomCodeForm";
import styles from "./EnterRoomCodeView.module.css";

const EnterRoomCodeView = () => {
  const navigate = useNavigate();

  const handleCodeSubmit = (submittedCode) => {
    navigate(`/join/${submittedCode}`);
  };

  return (
    <MainLayout>
      <div className={styles.viewContainer}>
        <EnterRoomCodeForm onSubmitCode={handleCodeSubmit} />
      </div>
    </MainLayout>
  );
};

export default EnterRoomCodeView;
