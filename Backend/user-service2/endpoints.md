# User Service API Endpoints - Postman Guide

Base URL: `http://localhost:8081`

---

## 1. AUTHENTICATION ENDPOINTS (Public - No Token Required)

### 1.1 Register User
**POST** `/api/auth/register`

**Headers:**
```
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "employeeId": "EMP001",
  "password": "Password@123",
  "name": "Sourabh Gorai",
  "email": "sourabhGorai@example.com",
  "roleId": 1,
  "companyId": 1
}
```

**Success Response (201):**
```json
{
  "success": true,
  "message": "Registration successful. Please verify your email with the OTP sent to john.doe@example.com",
  "data": {
    "id": 1,
    "employeeId": "EMP001",
    "name": "Sourabh Gorai",
    "email": "sourabhGorai@example.com",
    "roleId": 1,
    "roleName": "ADMIN",
    "companyId": 1,
    "companyName": "Tech Corp",
    "isEmailVerified": false,
    "isAccountLocked": false,
    "createdAt": "20-12-2025 17:30:00"
  },
  "timestamp": "2025-12-20T17:30:00"
}
```

---

### 1.2 Verify Email with OTP
**POST** `/api/auth/verify-email`

**Headers:**
```
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "email": "sourabhGorai@example.com",
  "otp": "123456"
}
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Email verified successfully. You can now log in",
  "timestamp": "2025-12-20T17:35:00"
}
```

---

### 1.3 Resend OTP
**POST** `/api/auth/resend-otp`

**Headers:**
```
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "email": "sourabhGorai@example.com"
}
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "OTP has been resent to john.doe@example.com",
  "timestamp": "2025-12-20T17:36:00"
}
```

---

### 1.4 Login
**POST** `/api/auth/login`

**Headers:**
```
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "email": "sourabhGorai@example.com",
  "password": "Password@123"
}
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "user": {
      "id": 1,
      "employeeId": "EMP001",
      "name": "John Doe",
      "email": "sourabhGorai@example.com",
      "roleId": 1,
      "roleName": "ADMIN",
      "companyId": 1,
      "companyName": "Tech Corp",
      "isEmailVerified": true,
      "isAccountLocked": false,
      "createdAt": "20-12-2025 17:30:00"
    }
  },
  "timestamp": "2025-12-20T17:40:00"
}
```

**Note:** Save the `accessToken` for authenticated requests!

---

### 1.5 Refresh Token
**POST** `/api/auth/refresh-token`

**Headers:**
```
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Token refreshed successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "user": {
      "id": 1,
      "employeeId": "EMP001",
      "name": "Sourabh Gorai",
      "email": "sourabhGorai@example.com",
      "roleId": 1,
      "roleName": "ADMIN",
      "companyId": 1,
      "companyName": "Tech Corp",
      "isEmailVerified": true,
      "isAccountLocked": false
    }
  },
  "timestamp": "2025-12-20T18:00:00"
}
```

---

### 1.6 Forgot Password
**POST** `/api/auth/forgot-password`

**Headers:**
```
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "email": "sourabhGorai@example.com"
}
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Password reset OTP has been sent to john.doe@example.com",
  "timestamp": "2025-12-20T18:05:00"
}
```

---

### 1.7 Reset Password
**POST** `/api/auth/reset-password`

**Headers:**
```
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "email": "sourabhGorai@example.com",
  "otp": "123456",
  "newPassword": "NewPassword@123"
}
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Password reset successful. You can now log in with your new password",
  "timestamp": "2025-12-20T18:10:00"
}
```

---

## 2. AUTHENTICATED ENDPOINTS (Token Required)

**For all endpoints below, add this header:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

---

### 2.1 Change Password (Authenticated User)
**POST** `/api/auth/change-password`

**Headers:**
```
Authorization: Bearer YOUR_ACCESS_TOKEN
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "currentPassword": "Password@123",
  "newPassword": "NewPassword@456"
}
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Password changed successfully",
  "timestamp": "2025-12-20T18:15:00"
}
```

---

### 2.2 Get Current User Profile
**GET** `/api/users/profile`

**Headers:**
```
Authorization: Bearer YOUR_ACCESS_TOKEN
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Profile retrieved successfully",
  "data": {
    "id": 1,
    "employeeId": "EMP001",
    "name": "Sourabh Gorai",
    "email": "sourabhGorai@example.com",
    "roleId": 1,
    "roleName": "ADMIN",
    "companyId": 1,
    "companyName": "Tech Corp",
    "isEmailVerified": true,
    "isAccountLocked": false,
    "createdAt": "20-12-2025 17:30:00",
    "updatedAt": "20-12-2025 18:15:00"
  },
  "timestamp": "2025-12-20T18:20:00"
}
```

---

### 2.3 Get User by ID (ADMIN/MANAGER only)
**GET** `/api/users/{userId}`

**Example:** `/api/users/1`

**Headers:**
```
Authorization: Bearer YOUR_ACCESS_TOKEN
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "User retrieved successfully",
  "data": {
    "id": 1,
    "employeeId": "EMP001",
    "name": "Sourabh Gorai",
    "email": "sourabhGorai@example.com",
    "roleId": 1,
    "roleName": "ADMIN",
    "companyId": 1,
    "companyName": "Tech Corp",
    "isEmailVerified": true,
    "isAccountLocked": false,
    "createdAt": "20-12-2025 17:30:00"
  },
  "timestamp": "2025-12-20T18:25:00"
}
```

---

### 2.4 Get All Users (ADMIN/MANAGER only)
**GET** `/api/users`

**Headers:**
```
Authorization: Bearer YOUR_ACCESS_TOKEN
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Retrieved 3 users",
  "data": [
    {
      "id": 1,
      "employeeId": "EMP001",
      "name": "Sourabh Gorai",
      "email": "sourabhGorai@example.com",
      "roleId": 1,
      "roleName": "ADMIN",
      "companyId": 1,
      "companyName": "Tech Corp",
      "isEmailVerified": true,
      "isAccountLocked": false,
      "createdAt": "20-12-2025 17:30:00"
    },
    {
      "id": 2,
      "employeeId": "EMP002",
      "name": "Jane Smith",
      "email": "jane.smith@example.com",
      "roleId": 2,
      "roleName": "MANAGER",
      "companyId": 1,
      "companyName": "Tech Corp",
      "isEmailVerified": true,
      "isAccountLocked": false,
      "createdAt": "20-12-2025 17:35:00"
    }
  ],
  "timestamp": "2025-12-20T18:30:00"
}
```

---

### 2.5 Lock User Account (ADMIN only)
**PUT** `/api/users/{userId}/lock`

**Example:** `/api/users/2/lock`

**Headers:**
```
Authorization: Bearer YOUR_ACCESS_TOKEN
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Account locked successfully",
  "timestamp": "2025-12-20T18:35:00"
}
```

---

### 2.6 Unlock User Account (ADMIN only)
**PUT** `/api/users/{userId}/unlock`

**Example:** `/api/users/2/unlock`

**Headers:**
```
Authorization: Bearer YOUR_ACCESS_TOKEN
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Account unlocked successfully",
  "timestamp": "2025-12-20T18:40:00"
}
```

---

### 2.7 Soft Delete User (ADMIN only)
**DELETE** `/api/users/{userId}`

**Example:** `/api/users/2`

**Headers:**
```
Authorization: Bearer YOUR_ACCESS_TOKEN
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "User deleted successfully",
  "timestamp": "2025-12-20T18:45:00"
}
```

---

### 2.8 Restore Deleted User (ADMIN only)
**PUT** `/api/users/{userId}/restore`

**Example:** `/api/users/2/restore`

**Headers:**
```
Authorization: Bearer YOUR_ACCESS_TOKEN
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "User restored successfully",
  "timestamp": "2025-12-20T18:50:00"
}
```

---

## 3. ERROR RESPONSES

### 3.1 Validation Error (400)
```json
{
  "success": false,
  "message": "Validation failed",
  "errors": [
    "email: Invalid email format",
    "password: Password must be at least 8 characters"
  ],
  "timestamp": "2025-12-20T19:00:00"
}
```

### 3.2 User Not Found (404)
```json
{
  "success": false,
  "message": "User not found with ID: 999",
  "errorCode": "USER_NOT_FOUND",
  "timestamp": "2025-12-20T19:05:00"
}
```

### 3.3 Invalid Credentials (401)
```json
{
  "success": false,
  "message": "Invalid email or password",
  "errorCode": "INVALID_CREDENTIALS",
  "timestamp": "2025-12-20T19:10:00"
}
```

### 3.4 Email Not Verified (403)
```json
{
  "success": false,
  "message": "Please verify your email before logging in",
  "errorCode": "EMAIL_NOT_VERIFIED",
  "timestamp": "2025-12-20T19:15:00"
}
```

### 3.5 Account Locked (403)
```json
{
  "success": false,
  "message": "Your account has been locked. Please contact support",
  "errorCode": "ACCOUNT_LOCKED",
  "timestamp": "2025-12-20T19:20:00"
}
```

### 3.6 Invalid OTP (400)
```json
{
  "success": false,
  "message": "Invalid OTP. 2 attempt(s) remaining",
  "errorCode": "INVALID_OTP",
  "timestamp": "2025-12-20T19:25:00"
}
```

### 3.7 OTP Expired (400)
```json
{
  "success": false,
  "message": "OTP has expired. Please request a new one",
  "errorCode": "OTP_EXPIRED",
  "timestamp": "2025-12-20T19:30:00"
}
```

### 3.8 User Already Exists (409)
```json
{
  "success": false,
  "message": "User with this email already exists",
  "errorCode": "USER_ALREADY_EXISTS",
  "timestamp": "2025-12-20T19:35:00"
}
```

### 3.9 Access Denied (403)
```json
{
  "success": false,
  "message": "Access denied",
  "errorCode": "ACCESS_DENIED",
  "timestamp": "2025-12-20T19:40:00"
}
```

### 3.10 Invalid Token (401)
```json
{
  "success": false,
  "message": "Invalid or expired refresh token",
  "errorCode": "INVALID_TOKEN",
  "timestamp": "2025-12-20T19:45:00"
}
```

---

## 4. TESTING WORKFLOW

### Step 1: Register a New User
```
POST /api/auth/register
```

### Step 2: Check Email for OTP
(OTP will be sent to the email - check console logs if email service is not configured)

### Step 3: Verify Email
```
POST /api/auth/verify-email
```

### Step 4: Login
```
POST /api/auth/login
```

### Step 5: Copy Access Token
Save the `accessToken` from login response

### Step 6: Use Protected Endpoints
Add token to Authorization header:
```
Authorization: Bearer YOUR_ACCESS_TOKEN
```

---

## 5. POSTMAN SETUP TIPS

### Setting Up Environment Variables:
1. Create a new environment in Postman
2. Add these variables:
    - `base_url`: `http://localhost:8081`
    - `access_token`: (will be set after login)
    - `refresh_token`: (will be set after login)

### Using Variables in Requests:
- URL: `{{base_url}}/api/auth/login`
- Authorization: `Bearer {{access_token}}`

### Auto-Set Token After Login:
Add this to the "Tests" tab of your login request:
```javascript
var jsonData = pm.response.json();
if (jsonData.data && jsonData.data.accessToken) {
    pm.environment.set("access_token", jsonData.data.accessToken);
    pm.environment.set("refresh_token", jsonData.data.refreshToken);
}
```

---

## 6. NOTES

- **Port**: Service runs on `8081`
- **Base URL**: `http://localhost:8081`
- **Token Expiry**: Access token expires in 24 hours
- **Refresh Token Expiry**: 7 days
- **OTP Expiry**: 10 minutes
- **OTP Attempts**: Maximum 3 attempts

Make sure your **independent-service** is running and has roles and companies created before registering users!