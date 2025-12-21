import { useState } from "react";
import apiService from "../../services/ApiService";

// Sign In Form Component
const SignInForm = ({ onSuccess }) => {
  const [formData, setFormData] = useState({ email: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    
    try {
      const result = await apiService.login(formData);
      if (result.success) {
        localStorage.setItem('jwt', result.token || result.data?.token);
        onSuccess(result);
      } else {
        setError(result.message || 'Login failed');
      }
    } catch (err) {
      setError('Network error. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="sign-in-form">
      <h2 className="title">Sign in</h2>
      {error && <div className="error-message">{error}</div>}
      <div className="input-field">
        <i className="fas fa-envelope"></i>
        <input
          type="email"
          placeholder="Email"
          value={formData.email}
          onChange={(e) => setFormData({...formData, email: e.target.value})}
          required
        />
      </div>
      <div className="input-field">
        <i className="fas fa-lock"></i>
        <input
          type="password"
          placeholder="Password"
          value={formData.password}
          onChange={(e) => setFormData({...formData, password: e.target.value})}
          required
        />
      </div>
      <button type="submit" className="btn solid" disabled={loading}>
        {loading ? 'Logging in...' : 'Login'}
      </button>
    </form>
  );
};

export default SignInForm; // 👈 Add this line
