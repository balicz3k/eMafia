import { useNavigate } from 'react-router-dom';
import styles from './DashboardView.module.css';
import MainLayout from '../../layouts/mainLayout/MainLayout';
import bloodHand from '../../assets/blood-hand.svg';
import UserSearch from '../../components/userSearch/UserSearch';

const DashboardView = () => {
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.removeItem("token");
    navigate("/login");
  };

  return (
    <MainLayout>
      <button onClick={handleLogout}>Wyloguj</button>
      <UserSearch />
    </MainLayout>
  );
};

export default DashboardView;