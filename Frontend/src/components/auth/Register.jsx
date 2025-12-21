// import React, { useState, useEffect } from 'react';
// import { Clock, Bug } from 'lucide-react';

// // API Service
// const API_BASE = 'http://localhost:8081/api';
// const API_ROLES = 'http://localhost:8082/api/roles';

// const apiService = {
//   register: async (data) => {
//     const response = await fetch(`${API_BASE}/auth/register`, {
//       method: 'POST',
//       headers: { 'Content-Type': 'application/json' },
//       body: JSON.stringify(data)
//     });
//     return response.json();
//   },
//   verifyEmail: async (data) => {
//     const response = await fetch(`${API_BASE}/auth/verify-email`, {
//       method: 'POST',
//       headers: { 'Content-Type': 'application/json' },
//       body: JSON.stringify(data)
//     });
//     return response.json();
//   },
//   login: async (data) => {
//     const response = await fetch(`${API_BASE}/auth/login`, {
//       method: 'POST',
//       headers: { 'Content-Type': 'application/json' },
//       body: JSON.stringify(data)
//     });
//     return response.json();
//   },
//   getRoles: async () => {
//     const response = await fetch(API_ROLES);
//     return response.json();
//   },
//   getCompanies: async () => {
//     const response = await fetch(`${API_BASE}/companies/active`);
//     return response.json();
//   }
// };

// // Clock Animation Component
// const ClockAnimation = () => {
//   const [time, setTime] = useState(new Date());

//   useEffect(() => {
//     const timer = setInterval(() => setTime(new Date()), 1000);
//     return () => clearInterval(timer);
//   }, []);

//   const seconds = time.getSeconds() * 6;
//   const minutes = time.getMinutes() * 6 + time.getSeconds() * 0.1;
//   const hours = (time.getHours() % 12) * 30 + time.getMinutes() * 0.5;

//   return (
//     <div className="clock-container">
//       <svg width="200" height="200" viewBox="0 0 200 200">
//         <circle cx="100" cy="100" r="90" fill="rgba(255,255,255,0.1)" stroke="white" strokeWidth="4"/>
//         {[...Array(12)].map((_, i) => (
//           <line
//             key={i}
//             x1="100"
//             y1="20"
//             x2="100"
//             y2="30"
//             stroke="white"
//             strokeWidth="3"
//             transform={`rotate(${i * 30} 100 100)`}
//           />
//         ))}
//         <line x1="100" y1="100" x2="100" y2="50" stroke="white" strokeWidth="6" strokeLinecap="round" transform={`rotate(${hours} 100 100)`}/>
//         <line x1="100" y1="100" x2="100" y2="35" stroke="white" strokeWidth="4" strokeLinecap="round" transform={`rotate(${minutes} 100 100)`}/>
//         <line x1="100" y1="100" x2="100" y2="25" stroke="rgba(255,100,100,0.9)" strokeWidth="2" strokeLinecap="round" transform={`rotate(${seconds} 100 100)`}/>
//         <circle cx="100" cy="100" r="8" fill="white"/>
//       </svg>
//     </div>
//   );
// };

// // Bug Animation Component
// const BugAnimation = () => {
//   const [position, setPosition] = useState({ x: 0, y: 0 });

//   useEffect(() => {
//     const interval = setInterval(() => {
//       setPosition({
//         x: Math.sin(Date.now() / 1000) * 20,
//         y: Math.cos(Date.now() / 800) * 15
//       });
//     }, 50);
//     return () => clearInterval(interval);
//   }, []);

//   return (
//     <div className="bug-container" style={{ transform: `translate(${position.x}px, ${position.y}px)` }}>
//       <Bug size={120} color="white" strokeWidth={1.5} />
//     </div>
//   );
// };

// // Sign In Form Component
// const SignInForm = ({ onSuccess }) => {
//   const [formData, setFormData] = useState({ email: '', password: '' });
//   const [error, setError] = useState('');
//   const [loading, setLoading] = useState(false);

//   const handleSubmit = async (e) => {
//     e.preventDefault();
//     setError('');
//     setLoading(true);
    
//     try {
//       const result = await apiService.login(formData);
//       if (result.success) {
//         localStorage.setItem('jwt', result.token || result.data?.token);
//         onSuccess(result);
//       } else {
//         setError(result.message || 'Login failed');
//       }
//     } catch (err) {
//       setError('Network error. Please try again.');
//     } finally {
//       setLoading(false);
//     }
//   };

//   return (
//     <div className="sign-in-form">
//       <h2 className="title">Sign in</h2>
//       {error && <div className="error-message">{error}</div>}
//       <div className="input-field">
//         <i className="fas fa-envelope"></i>
//         <input
//           type="email"
//           placeholder="Email"
//           value={formData.email}
//           onChange={(e) => setFormData({...formData, email: e.target.value})}
//         />
//       </div>
//       <div className="input-field">
//         <i className="fas fa-lock"></i>
//         <input
//           type="password"
//           placeholder="Password"
//           value={formData.password}
//           onChange={(e) => setFormData({...formData, password: e.target.value})}
//         />
//       </div>
//       <button onClick={handleSubmit} className="btn solid" disabled={loading}>
//         {loading ? 'Logging in...' : 'Login'}
//       </button>
//     </div>
//   );
// };

// // Sign Up Form Component
// const SignUpForm = ({ roles, companies, onRegistrationComplete }) => {
//   const [formData, setFormData] = useState({
//     employeeId: '',
//     password: '',
//     name: '',
//     email: '',
//     roleId: '',
//     companyId: ''
//   });
//   const [error, setError] = useState('');
//   const [loading, setLoading] = useState(false);

//   const handleSubmit = async () => {
//     setError('');
    
//     if (!formData.employeeId || !formData.password || !formData.name || !formData.email || !formData.roleId || !formData.companyId) {
//       setError('Please fill all fields');
//       return;
//     }
    
//     setLoading(true);
    
//     try {
//       const payload = {
//         ...formData,
//         roleId: parseInt(formData.roleId),
//         companyId: parseInt(formData.companyId)
//       };
//       const result = await apiService.register(payload);
//       if (result.success) {
//         onRegistrationComplete(formData.email);
//       } else {
//         setError(result.message || 'Registration failed');
//       }
//     } catch (err) {
//       setError('Network error. Please try again.');
//     } finally {
//       setLoading(false);
//     }
//   };

//   return (
//     <div className="sign-up-form">
//       <h2 className="title">Sign up</h2>
//       {error && <div className="error-message">{error}</div>}
//       <div className="input-field">
//         <i className="fas fa-id-badge"></i>
//         <input
//           type="text"
//           placeholder="Employee ID"
//           value={formData.employeeId}
//           onChange={(e) => setFormData({...formData, employeeId: e.target.value})}
//         />
//       </div>
//       <div className="input-field">
//         <i className="fas fa-user"></i>
//         <input
//           type="text"
//           placeholder="Full Name"
//           value={formData.name}
//           onChange={(e) => setFormData({...formData, name: e.target.value})}
//         />
//       </div>
//       <div className="input-field">
//         <i className="fas fa-envelope"></i>
//         <input
//           type="email"
//           placeholder="Email"
//           value={formData.email}
//           onChange={(e) => setFormData({...formData, email: e.target.value})}
//         />
//       </div>
//       <div className="input-field">
//         <i className="fas fa-lock"></i>
//         <input
//           type="password"
//           placeholder="Password"
//           value={formData.password}
//           onChange={(e) => setFormData({...formData, password: e.target.value})}
//         />
//       </div>
//       <div className="input-field">
//         <i className="fas fa-user-tag"></i>
//         <select
//           value={formData.roleId}
//           onChange={(e) => setFormData({...formData, roleId: e.target.value})}
//         >
//           <option value="">Select Role</option>
//           {roles.map(role => (
//             <option key={role.roleId} value={role.roleId}>{role.role}</option>
//           ))}
//         </select>
//       </div>
//       <div className="input-field">
//         <i className="fas fa-building"></i>
//         <select
//           value={formData.companyId}
//           onChange={(e) => setFormData({...formData, companyId: e.target.value})}
//         >
//           <option value="">Select Company</option>
//           {companies.map(company => (
//             <option key={company.companyId} value={company.companyId}>
//               {company.companyName} ({company.companyType})
//             </option>
//           ))}
//         </select>
//       </div>
//       <button onClick={handleSubmit} className="btn" disabled={loading}>
//         {loading ? 'Signing up...' : 'Sign up'}
//       </button>
//     </div>
//   );
// };

// // OTP Verification Component
// const OTPVerification = ({ email, onVerified, onBack }) => {
//   const [otp, setOtp] = useState('');
//   const [error, setError] = useState('');
//   const [loading, setLoading] = useState(false);

//   const handleSubmit = async () => {
//     if (!otp) {
//       setError('Please enter OTP');
//       return;
//     }
    
//     setError('');
//     setLoading(true);
    
//     try {
//       const result = await apiService.verifyEmail({ email, otp });
//       if (result.success) {
//         onVerified();
//       } else {
//         setError(result.message || 'Invalid OTP');
//       }
//     } catch (err) {
//       setError('Network error. Please try again.');
//     } finally {
//       setLoading(false);
//     }
//   };

//   return (
//     <div className="otp-form">
//       <h2 className="title">Verify Email</h2>
//       <p style={{color: '#666', marginBottom: '20px', textAlign: 'center'}}>
//         Enter the OTP sent to<br/><strong>{email}</strong>
//       </p>
//       {error && <div className="error-message">{error}</div>}
//       <div className="input-field">
//         <i className="fas fa-key"></i>
//         <input
//           type="text"
//           placeholder="Enter OTP"
//           value={otp}
//           onChange={(e) => setOtp(e.target.value)}
//           maxLength="6"
//         />
//       </div>
//       <button onClick={handleSubmit} className="btn" disabled={loading}>
//         {loading ? 'Verifying...' : 'Verify'}
//       </button>
//       <button onClick={onBack} className="btn transparent" style={{marginTop: '10px'}}>
//         Back
//       </button>
//     </div>
//   );
// };

// // Main App Component
// export default function App() {
//   const [isSignUpMode, setIsSignUpMode] = useState(false);
//   const [showOTP, setShowOTP] = useState(false);
//   const [registrationEmail, setRegistrationEmail] = useState('');
//   const [roles, setRoles] = useState([]);
//   const [companies, setCompanies] = useState([]);
//   const [authenticated, setAuthenticated] = useState(false);

//   useEffect(() => {
//     const fetchData = async () => {
//       try {
//         const [rolesRes, companiesRes] = await Promise.all([
//           apiService.getRoles(),
//           apiService.getCompanies()
//         ]);
//         if (rolesRes.success) setRoles(rolesRes.data);
//         if (companiesRes.success) setCompanies(companiesRes.data);
//       } catch (err) {
//         console.error('Failed to fetch roles/companies:', err);
//       }
//     };
//     fetchData();
//   }, []);

//   const handleRegistrationComplete = (email) => {
//     setRegistrationEmail(email);
//     setShowOTP(true);
//   };

//   const handleOTPVerified = () => {
//     setShowOTP(false);
//     setIsSignUpMode(false);
//     alert('Email verified successfully! Please sign in.');
//   };

//   const handleLoginSuccess = (result) => {
//     setAuthenticated(true);
//     alert('Login successful!');
//   };

//   if (authenticated) {
//     return (
//       <div style={{display: 'flex', alignItems: 'center', justifyContent: 'center', minHeight: '100vh', flexDirection: 'column'}}>
//         <h1 style={{color: '#4481eb'}}>Welcome!</h1>
//         <p>You are now logged in.</p>
//         <button className="btn" onClick={() => {
//           localStorage.removeItem('jwt');
//           setAuthenticated(false);
//         }}>Logout</button>
//       </div>
//     );
//   }

//   return (
//     <>
//       <style>{`
//         @import url("https://fonts.googleapis.com/css2?family=Poppins:wght@200;300;400;500;600;700;800&display=swap");
//         * { margin: 0; padding: 0; box-sizing: border-box; }
//         body, input, select { font-family: "Poppins", sans-serif; }
//         .container { position: relative; width: 100%; background-color: #fff; min-height: 100vh; overflow: hidden; }
//         .forms-container { position: absolute; width: 100%; height: 100%; top: 0; left: 0; }
//         .signin-signup { position: absolute; top: 50%; transform: translate(-50%, -50%); left: 75%; width: 50%; transition: 1s 0.7s ease-in-out; display: grid; grid-template-columns: 1fr; z-index: 5; }
//         .sign-in-form, .sign-up-form, .otp-form { display: flex; align-items: center; justify-content: center; flex-direction: column; padding: 0rem 5rem; transition: all 0.2s 0.7s; overflow: hidden; grid-column: 1 / 2; grid-row: 1 / 2; }
//         .sign-up-form { opacity: 0; z-index: 1; }
//         .sign-in-form { z-index: 2; }
//         .otp-form { z-index: 3; opacity: 1; }
//         .title { font-size: 2.2rem; color: #444; margin-bottom: 10px; }
//         .input-field { max-width: 380px; width: 100%; background-color: #f0f0f0; margin: 10px 0; height: 55px; border-radius: 55px; display: grid; grid-template-columns: 15% 85%; padding: 0 0.4rem; position: relative; }
//         .input-field i { text-align: center; line-height: 55px; color: #acacac; transition: 0.5s; font-size: 1.1rem; }
//         .input-field input, .input-field select { background: none; outline: none; border: none; line-height: 1; font-weight: 600; font-size: 1.1rem; color: #333; width: 100%; }
//         .input-field input::placeholder { color: #aaa; font-weight: 500; }
//         .input-field select { cursor: pointer; }
//         .btn { width: 150px; background-color: #5995fd; border: none; outline: none; height: 49px; border-radius: 49px; color: #fff; text-transform: uppercase; font-weight: 600; margin: 10px 0; cursor: pointer; transition: 0.5s; }
//         .btn:hover { background-color: #4d84e2; }
//         .btn:disabled { opacity: 0.6; cursor: not-allowed; }
//         .btn.transparent { margin: 0; background: none; border: 2px solid #fff; width: 130px; height: 41px; font-weight: 600; font-size: 0.8rem; }
//         .panels-container { position: absolute; height: 100%; width: 100%; top: 0; left: 0; display: grid; grid-template-columns: repeat(2, 1fr); }
//         .container:before { content: ""; position: absolute; height: 2000px; width: 2000px; top: -10%; right: 48%; transform: translateY(-50%); background-image: linear-gradient(-45deg, #4481eb 0%, #04befe 100%); transition: 1.8s ease-in-out; border-radius: 50%; z-index: 6; }
//         .panel { display: flex; flex-direction: column; align-items: flex-end; justify-content: space-around; text-align: center; z-index: 6; }
//         .left-panel { pointer-events: all; padding: 3rem 17% 2rem 12%; }
//         .right-panel { pointer-events: none; padding: 3rem 12% 2rem 17%; }
//         .panel .content { color: #fff; transition: transform 0.9s ease-in-out; transition-delay: 0.6s; }
//         .panel h3 { font-weight: 600; line-height: 1; font-size: 1.5rem; }
//         .panel p { font-size: 0.95rem; padding: 0.7rem 0; }
//         .clock-container, .bug-container { transition: transform 0.9s ease-in-out; transition-delay: 0.4s; }
//         .right-panel .clock-container, .right-panel .content { transform: translateX(800px); }
//         .container.sign-up-mode:before { transform: translate(100%, -50%); right: 52%; }
//         .container.sign-up-mode .left-panel .bug-container, .container.sign-up-mode .left-panel .content { transform: translateX(-800px); }
//         .container.sign-up-mode .signin-signup { left: 25%; }
//         .container.sign-up-mode .sign-up-form { opacity: 1; z-index: 2; }
//         .container.sign-up-mode .sign-in-form { opacity: 0; z-index: 1; }
//         .container.sign-up-mode .right-panel .clock-container, .container.sign-up-mode .right-panel .content { transform: translateX(0%); }
//         .container.sign-up-mode .left-panel { pointer-events: none; }
//         .container.sign-up-mode .right-panel { pointer-events: all; }
//         .error-message { background-color: #fee; color: #c33; padding: 10px; border-radius: 5px; margin-bottom: 15px; font-size: 0.9rem; max-width: 380px; width: 100%; text-align: center; }
//         @media (max-width: 870px) {
//           .container { min-height: 800px; height: 100vh; }
//           .signin-signup { width: 100%; top: 95%; transform: translate(-50%, -100%); transition: 1s 0.8s ease-in-out; }
//           .signin-signup, .container.sign-up-mode .signin-signup { left: 50%; }
//           .panels-container { grid-template-columns: 1fr; grid-template-rows: 1fr 2fr 1fr; }
//           .panel { flex-direction: row; justify-content: space-around; align-items: center; padding: 2.5rem 8%; grid-column: 1 / 2; }
//           .right-panel { grid-row: 3 / 4; }
//           .left-panel { grid-row: 1 / 2; }
//           .clock-container svg, .bug-container svg { width: 120px; height: 120px; }
//           .panel .content { padding-right: 15%; transition: transform 0.9s ease-in-out; transition-delay: 0.8s; }
//           .panel h3 { font-size: 1.2rem; }
//           .panel p { font-size: 0.7rem; padding: 0.5rem 0; }
//           .btn.transparent { width: 110px; height: 35px; font-size: 0.7rem; }
//           .container:before { width: 1500px; height: 1500px; transform: translateX(-50%); left: 30%; bottom: 68%; right: initial; top: initial; transition: 2s ease-in-out; }
//           .container.sign-up-mode:before { transform: translate(-50%, 100%); bottom: 32%; right: initial; }
//           .container.sign-up-mode .left-panel .bug-container, .container.sign-up-mode .left-panel .content { transform: translateY(-300px); }
//           .container.sign-up-mode .right-panel .clock-container, .container.sign-up-mode .right-panel .content { transform: translateY(0px); }
//           .right-panel .clock-container, .right-panel .content { transform: translateY(300px); }
//           .container.sign-up-mode .signin-signup { top: 5%; transform: translate(-50%, 0); }
//         }
//         @media (max-width: 570px) {
//           .sign-in-form, .sign-up-form, .otp-form { padding: 0 1.5rem; }
//           .clock-container, .bug-container { display: none; }
//           .panel .content { padding: 0.5rem 1rem; }
//           .container { padding: 1.5rem; }
//           .container:before { bottom: 72%; left: 50%; }
//           .container.sign-up-mode:before { bottom: 28%; left: 50%; }
//         }
//       `}</style>
      
//       <div className={`container ${isSignUpMode ? 'sign-up-mode' : ''}`}>
//         <div className="forms-container">
//           <div className="signin-signup">
//             {!showOTP ? (
//               <>
//                 <SignInForm onSuccess={handleLoginSuccess} />
//                 <SignUpForm 
//                   roles={roles} 
//                   companies={companies}
//                   onRegistrationComplete={handleRegistrationComplete}
//                 />
//               </>
//             ) : (
//               <OTPVerification 
//                 email={registrationEmail}
//                 onVerified={handleOTPVerified}
//                 onBack={() => setShowOTP(false)}
//               />
//             )}
//           </div>
//         </div>
        
//         <div className="panels-container">
//           <div className="panel left-panel">
//             <div className="content">
//               <h3>New here?</h3>
//               <p>Join us today and start your journey with our platform!</p>
//               <button 
//                 className="btn transparent" 
//                 onClick={() => setIsSignUpMode(true)}
//               >
//                 Sign up
//               </button>
//             </div>
//             <BugAnimation />
//           </div>
//           <div className="panel right-panel">
//             <div className="content">
//               <h3>One of us?</h3>
//               <p>Sign in to continue your experience with us!</p>
//               <button 
//                 className="btn transparent"
//                 onClick={() => {
//                   setIsSignUpMode(false);
//                   setShowOTP(false);
//                 }}
//               >
//                 Sign in
//               </button>
//             </div>
//             <ClockAnimation />
//           </div>
//         </div>
//       </div>
//     </>
//   );
// }