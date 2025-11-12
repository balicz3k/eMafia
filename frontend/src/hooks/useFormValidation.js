import { useState } from "react";

export const useFormValidation = (initialValues, onSubmit) => {
  const [formData, setFormData] = useState(initialValues);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
    setError("");
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      await onSubmit(formData);
    } catch (err) {
      setError(err.message || "An error occurred");
    } finally {
      setLoading(false);
    }
  };

  const setFormError = (errorMessage) => {
    setError(errorMessage);
  };

  return {
    formData,
    setFormData,
    loading,
    error,
    setError: setFormError,
    handleChange,
    handleSubmit,
  };
};
