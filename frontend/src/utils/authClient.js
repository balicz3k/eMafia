import axios from "axios";

/**
 * Klient HTTP dla Auth Service.
 * Używany do operacji logowania, rejestracji i zarządzania użytkownikami.
 */
const authClient = axios.create({
  baseURL: process.env.REACT_APP_AUTH_API_URL || process.env.REACT_APP_API_BASE_URL || "",
});

// Interceptor do dodawania tokena do żądań (dla chronionych endpointów)
authClient.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Interceptor do obsługi błędów
authClient.interceptors.response.use(
  (response) => response,
  (error) => {
    // Logowanie błędów do konsoli (przydatne przy debugowaniu)
    console.error("[AuthClient] Error:", error.response?.status, error.response?.data);
    return Promise.reject(error);
  }
);

export default authClient;
