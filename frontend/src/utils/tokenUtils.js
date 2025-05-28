export const refreshAccessToken = async () => {
  const refreshToken = localStorage.getItem("refreshToken");
  const API_BASE_URL = process.env.REACT_APP_API_BASE_URL;

  if (!refreshToken) {
    throw new Error("No refresh token available");
  }

  try {
    const response = await fetch(`${API_BASE_URL}/api/auth/refresh`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ refreshToken }),
    });

    if (response.ok) {
      const data = await response.json();

      localStorage.setItem("token", data.token);
      if (data.refreshToken) {
        localStorage.setItem("refreshToken", data.refreshToken);
      }
      if (data.expiresIn) {
        localStorage.setItem("expiresIn", data.expiresIn);
        const expirationTime = Date.now() + data.expiresIn * 1000;
        localStorage.setItem("tokenExpiration", expirationTime);
      }

      console.log("Token refreshed successfully");
      return data.token;
    } else {
      const errorText = await response.text();
      console.error("Token refresh failed:", response.status, errorText);

      localStorage.clear();
      throw new Error(`Token refresh failed: ${response.status}`);
    }
  } catch (error) {
    console.error("Token refresh error:", error);
    localStorage.clear();
    throw error;
  }
};

export const isTokenExpired = () => {
  const expiration = localStorage.getItem("tokenExpiration");
  if (!expiration) {
    const token = localStorage.getItem("token");
    if (!token) return true;

    try {
      const base64Url = token.split(".")[1];
      const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split("")
          .map(function (c) {
            return "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2);
          })
          .join("")
      );

      const decoded = JSON.parse(jsonPayload);
      const currentTime = Math.floor(Date.now() / 1000);

      return decoded.exp < currentTime;
    } catch (error) {
      console.error("Error checking token expiration:", error);
      return true;
    }
  }

  return Date.now() > parseInt(expiration);
};

export const clearAuthData = () => {
  localStorage.removeItem("token");
  localStorage.removeItem("refreshToken");
  localStorage.removeItem("expiresIn");
  localStorage.removeItem("tokenExpiration");
};
