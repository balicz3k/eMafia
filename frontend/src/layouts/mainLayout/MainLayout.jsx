import Header from '../../components/header/Header';
import Footer from '../../components/footer/Footer';
import Styles from './MainLayout.module.css';

const MainLayout = ({ children }) => {
  return (
    <div className="d-flex flex-column min-vh-100">
      <Header />
      <main className={Styles.mainContent}>{children}</main>
      <Footer />
    </div>
  );
};

export default MainLayout;