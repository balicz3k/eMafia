import React from "react";
import styles from "./FormMessage.module.css";

const FormMessage = ({ type = "error", message }) => {
  if (!message) return null;

  return (
    <div className={`${styles.message} ${styles[type]}`}>
      {message}
    </div>
  );
};

export default FormMessage;
