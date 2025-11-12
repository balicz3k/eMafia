import React, { useEffect, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import styles from "./LoginForm.module.css";
import { MdEmail, MdLock } from "react-icons/md";
import { useFormValidation } from "../../hooks/useFormValidation";
import FormMessage from "../formMessage/FormMessage";
import FormInput from "../formInput/FormInput";
import { handleFetchError } from "../../utils/apiErrorHandler";
import { getEmailError } from "../../utils/validators";

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL;

const LoginForm = () => {
  const [remember, setRemember] = useState(false);
  const [successMessage, setSuccessMessage] = useState("");
  const navigate = useNavigate();
  const location = useLocation();

  const {
    formData,
    loading,
    error,
    setError,
    handleChange,
    handleSubmit: handleFormSubmit,
  } = useFormValidation({ email: "", password: "" }, async (data) => {
    if (!validateForm(data)) {
      throw new Error("");
    }

    const result = await handleFetchError(
      fetch(`${API_BASE_URL}/api/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data),
      }),
      "Login failed. Please check your credentials.",
    );

    if (!result.ok) {
      throw new Error(result.error);
    }

    const {
      token,
      refreshToken,
      tokenType = "Bearer",
      expiresIn,
    } = result.data;

    localStorage.setItem("token", token);
    localStorage.setItem("refreshToken", refreshToken);
    localStorage.setItem("tokenType", tokenType);
    localStorage.setItem("expiresIn", expiresIn);

    const expirationTime = Date.now() + expiresIn * 1000;
    localStorage.setItem("tokenExpiration", expirationTime.toString());

    if (remember) {
      localStorage.setItem("rememberMe", "true");
    } else {
      localStorage.removeItem("rememberMe");
    }

    navigate("/dashboard");
  });

  useEffect(() => {
    const token = localStorage.getItem("token");
    if (token) {
      navigate("/dashboard");
    }

    // Display success message from registration
    if (location.state?.message) {
      setSuccessMessage(location.state.message);
      setTimeout(() => setSuccessMessage(""), 5000);
    }
  }, [navigate, location]);

  const validateForm = (data) => {
    const emailError = getEmailError(data.email);
    if (emailError) {
      setError(emailError);
      return false;
    }

    if (!data.password) {
      setError("Password is required");
      return false;
    }

    return true;
  };

  const handleSubmit = async (e) => {
    try {
      await handleFormSubmit(e);
    } catch (err) {
      if (err.message) {
        setError(err.message);
      }
    }
  };

  const handleRememberChange = () => {
    setRemember(!remember);
  };

  return (
    <div className={styles.container}>
      <div className={styles.signinForm}>
        <h2 className={styles.formTitle}>Sign in</h2>
        <FormMessage type="success" message={successMessage} />
        <FormMessage type="error" message={error} />
        <form onSubmit={handleSubmit}>
          <FormInput
            label={<MdEmail size={25} />}
            type="email"
            name="email"
            placeholder="Your Email"
            value={formData.email}
            onChange={handleChange}
            disabled={loading}
            required
          />

          <FormInput
            label={<MdLock size={25} />}
            type="password"
            name="password"
            placeholder="Password"
            value={formData.password}
            onChange={handleChange}
            disabled={loading}
            required
          />

          <div className={styles.cleanFormGroup}>
            <div className={styles.rememberWrapper}>
              <input
                type="checkbox"
                name="remember"
                id="remember-me"
                className={styles.agreeTerm}
                checked={remember}
                onChange={handleRememberChange}
                disabled={loading}
              />
              <label htmlFor="remember-me" className={styles.rememberMe}>
                Remember me
              </label>
            </div>
            <Link className={styles.forgotPassword} to="/forgot-password">
              Forgot password?
            </Link>
          </div>

          <button className={styles.loginButton} disabled={loading}>
            {loading ? "Signing in..." : "Log in"}
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
