import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import styles from './RegisterForm.module.css';
import { MdEmail, MdLock, MdPerson } from 'react-icons/md';

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL;

const RegisterForm = () => {
  const [formData, setFormData] = useState({ username: "", email: "", password: "", confirmPassword: "" });
  const [agreeTerms, setAgreeTerms] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleAgreeTermsChange = () => {
    setAgreeTerms(!agreeTerms);
  }

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (formData.password !== formData.confirmPassword) {
      alert("Passwords do not match!");
      return;
    }

    if (!agreeTerms) {
      alert("You must agree to the Terms of Use and Privacy Policy");
      return;
    }

    try {
      const response = await fetch(`${API_BASE_URL}/api/auth/register`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          username: formData.username,
          email: formData.email,
          password: formData.password
        }),
      });
      if (response.ok) {
        alert("Registration successful!");
        navigate("/login");
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
      <div className={styles.signupForm}>
        <h2 className={styles.formTitle}>Create Account</h2>
        <form onSubmit={handleSubmit}>
          <div className={styles.formGroup}>
            <label htmlFor="username">
              <MdPerson size={25} />
            </label>
            <input 
              type="text" 
              name="username" 
              id="username"
              placeholder="Your Username"
              value={formData.username} 
              onChange={handleChange} 
              required 
            />
          </div>

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
              autoCorrect="off"
              autoCapitalize="none"
              spellCheck="false"
              data-private="true" 
            />
          </div>

          <div className={styles.formGroup}>
            <label htmlFor="confirmPassword">
              <MdLock size={25} />
            </label>
            <input 
              type="password" 
              name="confirmPassword" 
              id="confirmPassword"
              placeholder="Confirm Password" 
              value={formData.confirmPassword} 
              onChange={handleChange} 
              required
              autoCorrect="off"
              autoCapitalize="none"
              spellCheck="false"
              data-private="true"  
            />
          </div>
          
          <div className={styles.cleanFormGroup}>
            <div className={styles.termsWrapper}>
              <input 
                type="checkbox" 
                name="agreeTerms" 
                id="agree-terms"
                className={styles.agreeTerm} 
                checked={agreeTerms}
                onChange={handleAgreeTermsChange}
                required
              />
              <label htmlFor="agree-terms" className={styles.termsText}>
                I agree to the <Link to="/terms" className={styles.termsLink}>Terms of Use</Link> and <Link to="/privacy" className={styles.termsLink}>Privacy Policy</Link>
              </label>
            </div>
          </div>
          
          <button className={styles.registerButton}>
            Sign up
          </button>
        </form>
      </div>
      <Link to="/login" className={styles.signInLink}>
        Already have an account?
      </Link>
    </div>
  );
};

export default RegisterForm;