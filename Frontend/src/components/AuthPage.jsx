import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import apiService from '../services/ApiService';
import SignInForm from './auth/SignInForm';
import SignUpForm from './auth/SignUpForm';
import OTPVerification from './auth/OtpVerification';
import ClockAnimation from './auth/ClockAnimation';
import RocketAnimation from './auth/RocketAnimation';
// import BugAnimation from './auth/BugAnimation';
import './AuthPage.css'; // Move all the styles here

export default function AuthPage() {
  const navigate = useNavigate();
  const [isSignUpMode, setIsSignUpMode] = useState(false);
  const [showOTP, setShowOTP] = useState(false);
  const [registrationEmail, setRegistrationEmail] = useState('');
  const [roles, setRoles] = useState([]);
  const [companies, setCompanies] = useState([]);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [rolesRes, companiesRes] = await Promise.all([
          apiService.getRoles(),
          apiService.getCompanies()
        ]);
        if (rolesRes.success) setRoles(rolesRes.data);
        if (companiesRes.success) setCompanies(companiesRes.data);
      } catch (err) {
        console.error('Failed to fetch roles/companies:', err);
      }
    };
    fetchData();
  }, []);

  const handleRegistrationComplete = (email) => {
    setRegistrationEmail(email);
    setShowOTP(true);
  };

  const handleOTPVerified = () => {
    setShowOTP(false);
    setIsSignUpMode(false);
    alert('Email verified successfully! Please sign in.');
  };

  const handleLoginSuccess = (result) => {
    // Navigate to dashboard or home page
    navigate('/dashboard');
  };

  return (
    <div className={`container ${isSignUpMode ? 'sign-up-mode' : ''}`}>
      <div className="forms-container">
        <div className="signin-signup">
          {!showOTP ? (
            <>
              <SignInForm onSuccess={handleLoginSuccess} />
              <SignUpForm 
                roles={roles} 
                companies={companies}
                onRegistrationComplete={handleRegistrationComplete}
              />
            </>
          ) : (
            <OTPVerification 
              email={registrationEmail}
              onVerified={handleOTPVerified}
              onBack={() => setShowOTP(false)}
            />
          )}
        </div>
      </div>
      
      <div className="panels-container">
        <div className="panel left-panel">
          <div className="content">
            <h3>New here?</h3>
            <p>Join us today and start your journey with our platform!</p>
            <button 
              className="btn transparent" 
              onClick={() => setIsSignUpMode(true)}
            >
              Sign up
            </button>
          </div>
          <RocketAnimation />
        </div>
        <div className="panel right-panel">
          <div className="content">
            <h3>One of us?</h3>
            <p>Sign in to continue your experience with us!</p>
            <button 
              className="btn transparent"
              onClick={() => {
                setIsSignUpMode(false);
                setShowOTP(false);
              }}
            >
              Sign in
            </button>
          </div>
          <ClockAnimation />
        </div>
      </div>
    </div>
  );
}
