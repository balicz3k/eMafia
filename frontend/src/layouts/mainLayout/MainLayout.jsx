import Header from '../../components/header/Header';
import Footer from '../../components/footer/Footer';

const MainLayout = ({ children }) => {
  return (
    <div className="d-flex flex-column min-vh-100">
      <Header />
      <main className="lex-grow-1 container-fluid px-3 px-md-4 py-4">{children}</main>
      <Footer />
    </div>
  );
};

export default MainLayout;