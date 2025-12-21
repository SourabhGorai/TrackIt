import { useState } from "react";
import apiService from "../../services/ApiService";

// OTP Verification Component
export default function OTPVerification({ email, onVerified, onBack }) {
  const [otp, setOtp] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      const result = await apiService.verifyEmail({ email, otp });
      if (result.success) {
        onVerified();
      } else {
        setError(result.message || "Invalid OTP");
      }
    } catch (err) {
      setError("Network error. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="otp-form">
      <h2 className="title">Verify Email</h2>

      <p style={{ color: "#666", marginBottom: "20px" }}>
        Enter the OTP sent to <strong>{email}</strong>
      </p>

      {error && <div className="error-message">{error}</div>}

      <div className="input-field">
        <i className="fas fa-key"></i>
        <input
          type="text"
          placeholder="Enter OTP"
          value={otp}
          onChange={(e) => setOtp(e.target.value)}
          required
          maxLength={6}
        />
      </div>

      <button type="submit" className="btn" disabled={loading}>
        {loading ? "Verifying..." : "Verify"}
      </button>

      <button
        type="button"
        className="btn transparent"
        onClick={onBack}
        style={{ marginTop: "10px" }}
      >
        Back
      </button>
    </form>
  );
}
