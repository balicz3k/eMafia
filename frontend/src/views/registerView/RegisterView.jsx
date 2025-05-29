import { useNavigate } from "react-router-dom";
import styles from "./RegisterView.module.css";
import MainLayout from "../../layouts/mainLayout/MainLayout";
import RegisterForm from "../../components/registerForm/RegisterForm";
import bloodHand from "../../assets/blood-hand.svg";

const RegisterView = () => {
  const navigate = useNavigate();

  return (
    <MainLayout>
      <div className={styles.mainContainer}>
          <RegisterForm />
      </div>
    </MainLayout>
  );
};

export default RegisterView;
