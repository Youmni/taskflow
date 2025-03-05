import { useState } from "react";
import axios from "axios";

export default function Login() {
  const [formData, setFormData] = useState({
    username: "",
    password: "",
  });
  const [message, setMessage] = useState("");

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (formData.username.length < 5 || formData.username.length > 15) {
      setMessage("Username must be between 5 and 15 characters");
      return;
    }
    if (formData.password.length < 8 || formData.password.length > 50) {
      setMessage("Password must be between 8 and 50 characters");
      return;
    }

    try {
      const response = await axios.post(
        "http://localhost:8080/user/authenticate",
        formData,
        {
          headers: { "Content-Type": "application/json" },
        }
      );

      if (response.status === 200) {
        setMessage("Login successful!");
        // Hier kun je een token opslaan of doorgaan naar een andere pagina
      }
    } catch (error) {
      setMessage(error.response?.data || "Login failed");
    }
  };

  return (
    <div className="flex justify-center items-center h-screen">
      <form className="bg-white p-6 rounded-lg shadow-md w-96" onSubmit={handleSubmit}>
        <h2 className="text-xl font-bold mb-4">Login</h2>
        <input
          type="text"
          name="username"
          placeholder="Username"
          className="w-full p-2 border rounded mb-2"
          value={formData.username}
          onChange={handleChange}
          required
        />
        <input
          type="password"
          name="password"
          placeholder="Password"
          className="w-full p-2 border rounded mb-2"
          value={formData.password}
          onChange={handleChange}
          required
        />
        <button type="submit" className="w-full bg-blue-500 text-white p-2 rounded">
          Login
        </button>
        {message && <p className="mt-2 text-red-500">{message}</p>}
      </form>
    </div>
  );
}
