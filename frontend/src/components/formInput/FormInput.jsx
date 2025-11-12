import React from "react";
import styles from "./FormInput.module.css";

const FormInput = ({
  label: Icon,
  type = "text",
  name,
  placeholder,
  value,
  onChange,
  disabled = false,
  required = false,
  autoComplete = "off",
}) => {
  return (
    <div className={styles.formGroup}>
      <label htmlFor={name}>{Icon}</label>
      <input
        type={type}
        name={name}
        id={name}
        placeholder={placeholder}
        value={value}
        onChange={onChange}
        disabled={disabled}
        required={required}
        autoComplete={autoComplete}
        autoCorrect="off"
        autoCapitalize="none"
        spellCheck="false"
        data-private={type === "password" ? "true" : "false"}
      />
    </div>
  );
};

export default FormInput;
