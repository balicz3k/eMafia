import React, { useEffect, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import styles from './LoginForm.module.css';
import { MdEmail, MdLock, MdPerson } from 'react-icons/md';

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL;

const LoginForm = () => {
  const [formData, setFormData] = useState({ email: "", password: "" });
  const [remember, setRemember] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    const token = localStorage.getItem("token");
    if (token) {
      navigate("/dashboard");
    }
  }, [navigate]);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleRememberChange = () => {
    setRemember(!remember);
  }

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(formData),
      });
      if (response.ok) {
        const data = await response.json();
        localStorage.setItem("token", data.token);
        navigate("/dashboard");
      } else {
        const error = await response.text();
        alert(error || "An error occurred!");
      }
    } catch (err) {
      console.error(err);
      alert("An error occurred!");
    }
  };

  return (
    <div className={styles.container}>
        <div className={styles.signinForm}>
          <h2 className={styles.formTitle}>Sign in</h2>
          <form onSubmit={handleSubmit}>

            {/* <div className={styles.formGroup}>
              <label htmlFor="username">
                <MdPerson size={25} />
              </label>
              <input 
                type="username" 
                name="username" 
                id="username"
                placeholder="Your username"
                value={formData.username} 
                onChange={handleChange} 
                required 
              />
            </div> */}

            <div className={styles.formGroup}>
              <label htmlFor="email">
                <MdEmail size={25} />
              </label>
              <input 
                type="email" 
                name="email" 
                id="email"
                placeholder="Your Email"
                value={formData.email} 
                onChange={handleChange} 
                required 
              />
            </div>
            
            <div className={styles.formGroup}>
              <label htmlFor="password">
                <MdLock size={25} />
              </label>
              <input 
                type="password" 
                name="password" 
                id="password"
                placeholder="Password" 
                value={formData.password} 
                onChange={handleChange} 
                required 
              />
            </div>
            
            <div className={styles.cleanFormGroup}>
              <div className={styles.rememberWrapper}>
                <input 
                  type="checkbox" 
                  name="remember" 
                  id="remember-me"
                  className={styles.agreeTerm} 
                  checked={remember}
                  onChange={handleRememberChange}
                />
                <label htmlFor="remember-me" className={styles.rememberMe}>
                  Remember me
                </label>
              </div>
              <Link className={styles.forgotPassword} to="/forgot-password">Forgot password?</Link>
            </div>
            
            <button className={styles.loginButton}>
              Log in
            </button>
          </form>
        
        </div>
      <Link to="/register" className={styles.signUpLink}>
        Create an account
      </Link>
    </div>
  );
};

export default LoginForm;