// components/Footer/Footer.jsx
import styles from './Footer.module.css';
import facebook from '../../assets/facebook.svg';
import instagram from '../../assets/instagram.svg';
import github from '../../assets/github.svg';
import linkedin from '../../assets/linkedin.svg';
import footerLine from '../../assets/footer-line.svg';

const Footer = () => {
  return (
    <footer className={styles.footer}>
      <img 
        className={styles.footerLine1} 
        src={footerLine} 
        alt="Decoration line" 
      />
      <div className={styles.socialWrapper}>
        <a href="https://www.facebook.com/profile.php?id=100034413046489">
            <img className={styles.footerLogo} src={facebook} alt="Facebook" />
        </a>
        <a href="https://www.instagram.com/balicz3k/">
            <img className={styles.footerLogo} src={instagram} alt="Instagram" />
        </a>
        <a href="https://github.com/balicz3k">
            <img className={styles.footerLogo} src={github} alt="GitHub" />
        </a>
        <a href="https://www.linkedin.com/in/jakub-balicki-283bba2ba/">
            <img className={styles.footerLogo} src={linkedin} alt="LinkedIn" />
        </a>
      </div>
      <img 
        className={styles.footerLine2} 
        src={footerLine} 
        alt="Decoration line" 
      />
    </footer>
  );
};

export default Footer;