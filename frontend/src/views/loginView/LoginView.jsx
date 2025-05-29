import { useNavigate } from "react-router-dom";
import styles from "./LoginView.module.css";
import MainLayout from "../../layouts/mainLayout/MainLayout";
import LoginForm from "../../components/loginForm/LoginForm";
import bloodHand from "../../assets/blood-hand.svg";

const LoginView = () => {
  const navigate = useNavigate();

  return (
    <MainLayout>
      <div className={styles.mainContainer}>
          <LoginForm />
      </div>
    </MainLayout>
  );
};

export default LoginView;
