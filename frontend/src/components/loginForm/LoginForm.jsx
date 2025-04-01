import React from 'react';
import styles from './LoginForm.module.css';

const LoginForm = () => {
  return (
    <form className={styles.loginForm}>
      <h2>Sign in!</h2>
      <div>
        <label>Email:</label>
        <input type="email" name="email" required />
      </div>
      <div>
        <label>Password:</label>
        <input type="password" name="password" required />
      </div>
      <label>By continuing, you agree to the Terms of use and Privacy Policy.</label>
      <div>
        <input type="checkbox" name="remember" />
        <label>Remember me</label>
      </div>
      <button type="submit">Login</button>
      <label>Forget your password?</label>
      <label>Don't have an acount? Sign up!</label>
    </form>
  );
};

export default LoginForm;