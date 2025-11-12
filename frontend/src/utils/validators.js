export const validateEmail = (email) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
};

export const validatePassword = (password) => {
  const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).+$/;
  return password.length >= 8 && passwordRegex.test(password);
};

export const validateUsername = (username) => {
  return username.trim().length >= 3 && username.trim().length <= 20;
};

export const getEmailError = (email) => {
  if (!email.trim()) {
    return "Email is required";
  }
  if (!validateEmail(email)) {
    return "Invalid email format";
  }
  return null;
};

export const getPasswordError = (password) => {
  if (!password) {
    return "Password is required";
  }
  if (password.length < 8) {
    return "Password must be at least 8 characters long";
  }
  if (!validatePassword(password)) {
    return "Password must contain at least one lowercase letter, one uppercase letter, and one digit";
  }
  return null;
};

export const getUsernameError = (username) => {
  if (!username.trim()) {
    return "Username is required";
  }
  if (!validateUsername(username)) {
    return "Username must be between 3 and 20 characters";
  }
  return null;
};
