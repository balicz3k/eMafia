import { Link } from 'react-router-dom';
import styles from './Header.module.css';

const Header = () => {
  return (
    <header className={styles.header}>
      <h1 className={styles.logo}>Mafia</h1>
      <nav className={styles.navbar}>
        <Link to="/profile" className={styles.navLink}>Your Profile</Link>
        <Link to="/admin" className={styles.navLink}>Admin Panel</Link>
        <Link to="/create-room" className={styles.navLink}>Create Room</Link>
        <Link to="/" className={styles.navLink}>Create Game</Link>
        <Link to="/" className={styles.navLink}>Rules</Link>
        <Link to="/" className={styles.navLink}>About Autor</Link>
      </nav>
    </header>
  );
};

export default Header;