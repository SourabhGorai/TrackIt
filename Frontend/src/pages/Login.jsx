import "../App.css"
import { FaBug } from "react-icons/fa";
import { MdEmail, MdOutlinePassword } from "react-icons/md";
import { IoEye, IoEyeOff } from "react-icons/io5";
import { useState } from "react";

/* ---------------- ICON INPUT ---------------- */
function IconInput({ children, placeholder, type, value, onChange, showToggle, onToggle, showPassword }) {
  return (
    <div className="flex justify-left items-center w-full relative h-12 border mt-4 shadow-xl rounded">
      <div className="icon__wraper w-14 absolute flex justify-center items-center">
        <span className="text-xl opacity-80 text-gray-500">{children}</span>
      </div>
      <input
        type={type}
        placeholder={placeholder}
        value={value}
        onChange={onChange}
        className="w-full h-full pl-14 pr-12 outline-none"
      />
      {showToggle && (
        <button
          type="button"
          onClick={onToggle}
          className="absolute right-4 text-xl text-gray-500 hover:text-gray-700"
        >
          {showPassword ? <IoEyeOff /> : <IoEye />}
        </button>
      )}
    </div>
  );
}

/* ---------------- LOGIN PAGE ---------------- */
function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const isFormValid = email.trim() !== "" && password.trim() !== "";

  const handleLogin = async () => {
    if (!isFormValid) return;

    setError("");

    try {
      setLoading(true);

      const response = await fetch(
        "http://localhost:8080/api/auth/login",
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({ email, password }),
        }
      );

      if (!response.ok) {
        throw new Error("Invalid email or password");
      }

      const data = await response.json();
      console.log("Login Success:", data);

      // Note: localStorage is not available in Claude artifacts
      // In production, uncomment: localStorage.setItem("token", data.token);
      window.location.href = "/Dashboard";

    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex justify-center items-center w-full h-screen bg-slate-100">
      <div className="form-container rounded-2xl flex justify-between shadow-2xl w-11/12 max-w-screen-xl bg-white">

        {/* ---------------- LEFT ---------------- */}
        <div className="form-section w-1/2 px-24 py-16">
          <div className="logo-wrap flex justify-left gap-x-3 items-center">
            <FaBug className="text-2xl" />
            <span className="font-bold opacity-800">TrackIt</span>
          </div>

          <h1 className="text-3xl font-semibold mt-6 opacity-80 text-neutral-900">
            Sign In to your account
          </h1>

          <p className="opacity-60 mt-3 pt-10">Welcome back!</p>

          <IconInput
            placeholder="Email"
            type="text"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          >
            <MdEmail />
          </IconInput>

          <IconInput
            placeholder="Password"
            type={showPassword ? "text" : "password"}
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            showToggle={true}
            onToggle={() => setShowPassword(!showPassword)}
            showPassword={showPassword}
          >
            <MdOutlinePassword />
          </IconInput>

          {error && (
            <p className="text-red-500 text-sm mt-3">{error}</p>
          )}

          <div className="flex justify-between items-center mt-3 py-3">
            <div className="item">
              <input type="checkbox" />
              <span className="text-neutral-500"> Remember me</span>
            </div>
            <div className="item">
              <a href="" className="text-blue-600 hover:underline">Forgot Password</a>
            </div>
          </div>

          <button
            onClick={handleLogin}
            disabled={!isFormValid || loading}
            className={`
              w-full py-3 rounded mt-5 text-white text-xl transition-all duration-300
              ${
                !isFormValid || loading
                  ? "bg-gray-400 cursor-not-allowed opacity-70"
                  : "bg-gray-700 hover:bg-gray-800 hover:shadow-lg cursor-pointer"
              }
            `}
          >
            {loading ? "Logging in..." : "Login"}
          </button>
        </div>

        {/* ---------------- RIGHT ---------------- */}
        <div className="illustration-section w-1/2 bg-gradient-to-br from-blue-100 to-purple-100 rounded-r-2xl flex items-center justify-center">
          <div className="text-center p-8">
            <div className="text-6xl mb-4">🔐</div>
            <h2 className="text-2xl font-bold text-gray-800">Welcome to TrackIt</h2>
            <p className="text-gray-600 mt-2">Your task management solution</p>
          </div>
        </div>

      </div>
    </div>
  );
}

export default Login;