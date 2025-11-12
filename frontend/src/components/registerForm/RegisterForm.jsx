import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import styles from "./RegisterForm.module.css";
import { MdEmail, MdLock, MdPerson } from "react-icons/md";
import { useFormValidation } from "../../hooks/useFormValidation";
import FormMessage from "../formMessage/FormMessage";
import FormInput from "../formInput/FormInput";
import { handleFetchError } from "../../utils/apiErrorHandler";
import {
  getEmailError,
  getPasswordError,
  getUsernameError,
} from "../../utils/validators";

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL;

const RegisterForm = () => {
  const [agreeTerms, setAgreeTerms] = useState(false);
  const navigate = useNavigate();

  const {
    formData,
    loading,
    error,
    setError,
    handleChange,
    handleSubmit: handleFormSubmit,
  } = useFormValidation(
    { username: "", email: "", password: "", confirmPassword: "" },
    async (data) => {
      if (!validateForm(data)) {
        throw new Error("");
      }

      const result = await handleFetchError(
        fetch(`${API_BASE_URL}/api/auth/register`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            username: data.username,
            email: data.email,
            password: data.password,
          }),
        }),
        "An error occurred during registration",
      );

      if (!result.ok) {
        throw new Error(result.error);
      }

      navigate("/login", {
        state: { message: "Registration successful! Please log in." },
      });
    },
  );

  const validateForm = (data) => {
    const usernameError = getUsernameError(data.username);
    if (usernameError) {
      setError(usernameError);
      return false;
    }

    const emailError = getEmailError(data.email);
    if (emailError) {
      setError(emailError);
      return false;
    }

    const passwordError = getPasswordError(data.password);
    if (passwordError) {
      setError(passwordError);
      return false;
    }

    if (data.password !== data.confirmPassword) {
      setError("Passwords do not match");
      return false;
    }

    if (!agreeTerms) {
      setError("You must agree to the Terms of Use and Privacy Policy");
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

  const handleAgreeTermsChange = () => {
    setAgreeTerms(!agreeTerms);
    setError("");
  };

  return (
    <div className={styles.container}>
      <div className={styles.signupForm}>
        <h2 className={styles.formTitle}>Create Account</h2>
        <FormMessage type="error" message={error} />
        <form onSubmit={handleSubmit}>
          <FormInput
            label={<MdPerson size={25} />}
            type="text"
            name="username"
            placeholder="Your Username (3-20 characters)"
            value={formData.username}
            onChange={handleChange}
            disabled={loading}
            required
          />

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
            placeholder="Password (min 8 chars, uppercase, lowercase, digit)"
            value={formData.password}
            onChange={handleChange}
            disabled={loading}
            required
          />

          <FormInput
            label={<MdLock size={25} />}
            type="password"
            name="confirmPassword"
            placeholder="Confirm Password"
            value={formData.confirmPassword}
            onChange={handleChange}
            disabled={loading}
            required
          />

          <div className={styles.cleanFormGroup}>
            <div className={styles.termsWrapper}>
              <input
                type="checkbox"
                name="agreeTerms"
                id="agree-terms"
                className={styles.agreeTerm}
                checked={agreeTerms}
                onChange={handleAgreeTermsChange}
                disabled={loading}
                required
              />
              <label htmlFor="agree-terms" className={styles.termsText}>
                I agree to the{" "}
                <Link to="/terms" className={styles.termsLink}>
                  Terms of Use
                </Link>{" "}
                and{" "}
                <Link to="/privacy" className={styles.termsLink}>
                  Privacy Policy
                </Link>
              </label>
            </div>
          </div>

          <button className={styles.registerButton} disabled={loading}>
            {loading ? "Creating Account..." : "Sign up"}
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
