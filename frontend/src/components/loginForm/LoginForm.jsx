import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Link } from 'react-router-dom';
import styles from './LoginForm.module.css';

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL;

const LoginForm = () => {
  const [formData, setFormData] = useState({ email: "", password: "" });
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

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
        console.log("Token JWT:", data.token);
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
    <form className={styles.loginForm} onSubmit={handleSubmit}>
      <h2>Sign in!</h2>
      <div>
        <label>Email:</label>
        <input type="email" name="email" value={formData.email} onChange={handleChange} required />
      </div>
      <div>
        <label>Password:</label>
        <input type="password" name="password" value={formData.password} onChange={handleChange} required />
      </div>
      <label>By continuing, you agree to the Terms of use and Privacy Policy.</label>
      <div className={styles.rememberMe}>
        <input type="checkbox" name="remember" />
        <label>Remember me</label>
      </div>
      <button type="submit">Login</button>
      <label>Forget your password?</label>
      <label>
        Don't have an account? <Link to="/register">Sign up!</Link>
      </label>
    </form>
  );
};

export default LoginForm;