import { useContext, useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import Cookies from "js-cookie";
import { AuthContext } from "../context/AuthProvider";
import { IoEye, IoEyeOff } from "react-icons/io5";
import { useSnackbar } from "notistack";
import { FiLoader } from "react-icons/fi";

const Login = () => {
  const { setAccessToken } = useContext(AuthContext);
  const [loginForm, setLoginForm] = useState({ username: "", password: "" });
  const [loading, setLoading] = useState(false);
  const { enqueueSnackbar } = useSnackbar();
  const [showPassword, setShowPassword] = useState(false);

  const navigate = useNavigate();

  const handleChange = (event) => {
    const { name, value } = event.target;
    setLoginForm((prevData) => ({
      ...prevData,
      [name]: value,
    }));
  };

  const handleRegistration = async () => {
    navigate("/register");
  };

  const togglePasswordVisibility = () => {
    setShowPassword((prevShowPassword) => !prevShowPassword);
  };

  const handleClick = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      const response = await axios.post(
        "http://localhost:8080/user/authenticate",
        {
          username: loginForm.username,
          password: loginForm.password,
        }
      );

      if (response.status === 200) {
        const { accessToken, refreshToken } = response.data;

        const tokenExpiryTime = 1 / 24;
        Cookies.set("accessToken", accessToken, {
          secure: false,
          sameSite: "Strict",
          expires: tokenExpiryTime,
        });

        Cookies.set("refreshToken", refreshToken, {
          secure: false,
          sameSite: "Strict",
          expires: 7,
        });
        setAccessToken(accessToken);
        enqueueSnackbar("Successful login", { variant: "success" });
        navigate("/home");
      }
    } catch (e) {
      if (e.response && e.response.status === 401) {
        enqueueSnackbar("Wrong credentials", { variant: "error" });
      } else {
        enqueueSnackbar("Error logging in", { variant: "error" });
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container d-flex justify-content-center align-items-center min-vh-100">
      <form className="row shadow-lg rounded p-0 w-100 w-md-50 bg-white overflow-hidden">
        {/* Linker panel met mooie gradient */}
        <div
          className="col-md-6 d-flex flex-column justify-content-center align-items-center p-5"
          style={{
            background: "linear-gradient(135deg, #667eea, #764ba2)",
            color: "#fff",
          }}
        >
          <h1 className="display-4 fw-light">Welcome,</h1>
          <h2 className="fw-bold">To TaskFlow</h2>
        </div>

        {/* Rechter panel met het formulier */}
        <div className="col-md-6 p-5">
          <h3 className="fw-semibold text-center mb-4">Log in</h3>
          <div className="form-group mb-3">
            <label htmlFor="username">Username</label>
            <input
              type="text"
              id="username"
              name="username"
              value={loginForm.username}
              onChange={handleChange}
              required
              className="form-control"
              placeholder="Username"
            />
          </div>
          <div className="form-group mb-3 position-relative">
            <label htmlFor="password">Password</label>
            <input
              type={showPassword ? "text" : "password"}
              id="password"
              name="password"
              value={loginForm.password}
              onChange={handleChange}
              required
              className="form-control"
              placeholder="Password"
            />
          </div>
          <div className="d-flex flex-column align-items-center mt-4">
            <button
              onClick={handleClick}
              disabled={loading}
              className="btn w-100 text-white"
              style={{
                background: "linear-gradient(135deg, #667eea, #764ba2)",
                border: "none",
                fontWeight: "600",
                padding: "0.75rem 1.25rem",
                transition: "background 0.3s ease",
              }}
              onMouseOver={(e) =>
                (e.currentTarget.style.background =
                  "linear-gradient(135deg, #5a67d8, #6b46c1)")
              }
              onMouseOut={(e) =>
                (e.currentTarget.style.background =
                  "linear-gradient(135deg, #667eea, #764ba2)")
              }
            >
              {loading ? <FiLoader className="animate-spin" /> : "Login"}
            </button>
            <p className="text-end mt-2">
              No account yet?{" "}
              <span
                onClick={handleRegistration}
                className="text-decoration-underline fw-semibold text-success"
                style={{ cursor: "pointer" }}
              >
                Registration
              </span>
            </p>
          </div>
        </div>
      </form>
    </div>
  );
};

export default Login;
