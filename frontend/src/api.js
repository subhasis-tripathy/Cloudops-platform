import axios from "axios";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || "http://localhost:8080",
});

// Attach the JWT to every request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// If the server says 401 (expired/invalid token), clear the session and go to login
api.interceptors.response.use(
  (res) => res,
  (err) => {
    const url = err.config?.url || "";
    if (err.response?.status === 401 && !url.includes("/api/auth/")) {
      localStorage.removeItem("token");
      localStorage.removeItem("user");
      window.location.href = "/login";
    }
    return Promise.reject(err);
  }
);

// Turn an axios error into a readable message
export function errorMessage(err) {
  if (!err.response) return "Cannot reach the server. Is the backend running?";
  const data = err.response.data;
  if (data?.fields) return Object.values(data.fields).join(", ");
  if (data?.error) return data.error;
  return `Request failed (${err.response.status})`;
}

export default api;
