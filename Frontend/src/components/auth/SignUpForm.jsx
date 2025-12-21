import { useState } from "react";
import apiService from "../../services/ApiService";

// Sign Up Form Component
const SignUpForm = ({ roles, companies, onRegistrationComplete }) => {
  const [formData, setFormData] = useState({
    employeeId: '',
    password: '',
    name: '',
    email: '',
    roleId: '',
    companyId: ''
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    
    try {
      const payload = {
        ...formData,
        roleId: parseInt(formData.roleId),
        companyId: parseInt(formData.companyId)
      };
      const result = await apiService.register(payload);
      if (result.success) {
        onRegistrationComplete(formData.email);
      } else {
        setError(result.message || 'Registration failed');
      }
    } catch (err) {
      setError('Network error. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="sign-up-form">
      <h2 className="title">Sign up</h2>
      {error && <div className="error-message">{error}</div>}
      {/* Employee ID */}
      <div className="input-field">
        <i className="fas fa-id-badge"></i>
        <input
          type="text"
          placeholder="Employee ID"
          value={formData.employeeId}
          onChange={(e) => setFormData({...formData, employeeId: e.target.value})}
          required
        />
      </div>
      {/* Name */}
      <div className="input-field">
        <i className="fas fa-user"></i>
        <input
          type="text"
          placeholder="Full Name"
          value={formData.name}
          onChange={(e) => setFormData({...formData, name: e.target.value})}
          required
        />
      </div>
      {/* Email */}
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
      {/* Password */}
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
      {/* Role */}
      <div className="input-field">
        <i className="fas fa-user-tag"></i>
        <select
          value={formData.roleId}
          onChange={(e) => setFormData({...formData, roleId: e.target.value})}
          required
        >
          <option value="">Select Role</option>
          {roles.map(role => (
            <option key={role.roleId} value={role.roleId}>{role.role}</option>
          ))}
        </select>
      </div>
      {/* Company */}
      <div className="input-field">
        <i className="fas fa-building"></i>
        <select
          value={formData.companyId}
          onChange={(e) => setFormData({...formData, companyId: e.target.value})}
          required
        >
          <option value="">Select Company</option>
          {companies.map(company => (
            <option key={company.companyId} value={company.companyId}>
              {company.companyName} ({company.companyType})
            </option>
          ))}
        </select>
      </div>
      <button type="submit" className="btn" disabled={loading}>
        {loading ? 'Signing up...' : 'Sign up'}
      </button>
    </form>
  );
};

export default SignUpForm; // 👈 Add this line
