/**
 * Parsuje błąd z odpowiedzi API
 * @param {Response} response - Odpowiedź z fetch
 * @param {string} defaultMessage - Domyślna wiadomość błędu
 * @returns {Promise<string>} - Wiadomość błędu
 */
export const parseApiError = async (
  response,
  defaultMessage = "An error occurred",
) => {
  const contentType = response.headers.get("content-type");

  if (contentType && contentType.includes("application/json")) {
    try {
      const errorData = await response.json();
      return errorData.message || errorData.error || defaultMessage;
    } catch {
      return (await response.text()) || defaultMessage;
    }
  }

  return (await response.text()) || defaultMessage;
};

/**
 * Obsługuje błędy sieciowe
 * @param {Error} error - Błąd
 * @returns {string} - Wiadomość błędu
 */
export const handleNetworkError = (error) => {
  console.error("Network error:", error);
  return "Network error. Please check your connection and try again.";
};

/**
 * Obsługuje błędy fetch z obsługą błędów sieciowych
 * @param {Promise} fetchPromise - Promise z fetch
 * @param {string} defaultMessage - Domyślna wiadomość błędu
 * @returns {Promise<{ok: boolean, data?: any, error?: string}>}
 */
export const handleFetchError = async (
  fetchPromise,
  defaultMessage = "An error occurred",
) => {
  try {
    const response = await fetchPromise;

    if (!response.ok) {
      const errorMessage = await parseApiError(response, defaultMessage);
      return { ok: false, error: errorMessage };
    }

    const data = await response.json();
    return { ok: true, data };
  } catch (error) {
    const errorMessage = handleNetworkError(error);
    return { ok: false, error: errorMessage };
  }
};
