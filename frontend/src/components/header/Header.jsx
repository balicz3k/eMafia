// components/Header/Header.jsx
import { Link } from 'react-router-dom';
import styles from './Header.module.css';
import { Navbar, Container } from 'react-bootstrap';

const Header = () => {
  return (
    <header>
        <div className={styles.header}>
            <h1 className={styles.logo}>Mafia</h1>
            <Navbar expand="lg" className={styles.navbar}>
                <Container>
                    <Link to="/" className={styles.navLink}>Create Game</Link>
                    <Link to="/" className={styles.navLink}>Rules</Link>
                    <Link to="/" className={styles.navLink}>About Autor</Link>
                </Container>
            </Navbar>
        </div>
    </header>
  );
};

export default Header;