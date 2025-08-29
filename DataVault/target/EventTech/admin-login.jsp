<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="javax.servlet.http.HttpSession" %>
<%
    // Check if admin is already logged in
    if (session != null && session.getAttribute("admin_id") != null) {
        response.sendRedirect("dashboard.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Login - TARUNYAM Tech Event</title>
    <link rel="stylesheet" href="css/style.css">
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/feather-icons/4.29.0/feather.min.css">
</head>
<body class="admin-body">
    <!-- Background Effects -->
    <div class="bg-effects">
        <div class="bg-gradient"></div>
        <div class="bg-pattern"></div>
    </div>

    <!-- Toast Notification Container -->
    <div id="toast-container" class="toast-container"></div>

    <!-- Navigation -->
    <nav class="admin-nav">
        <div class="nav-content">
            <a href="index.jsp" class="nav-back">
                <i data-feather="arrow-left"></i>
                Back to Events
            </a>
            <div class="nav-title">Admin Panel</div>
        </div>
    </nav>

    <!-- Login Container -->
    <div class="login-container">
        <div class="login-card">
            <div class="login-header">
                <div class="login-icon">
                    <i data-feather="shield"></i>
                </div>
                <h2>Admin Login</h2>
                <p>Access the admin dashboard</p>
            </div>

            <form id="adminLoginForm" class="login-form" onsubmit="submitAdminLogin(event)">
                <div class="form-group">
                    <label for="username">Username</label>
                    <div class="input-wrapper">
                        <i data-feather="user"></i>
                        <input type="text" id="username" name="username" required placeholder="Enter your username">
                    </div>
                </div>

                <div class="form-group">
                    <label for="password">Password</label>
                    <div class="input-wrapper">
                        <i data-feather="lock"></i>
                        <input type="password" id="password" name="password" required placeholder="Enter your password">
                        <button type="button" class="password-toggle" onclick="togglePassword()">
                            <i data-feather="eye" id="password-eye"></i>
                        </button>
                    </div>
                </div>

                <button type="submit" class="login-btn">
                    <span>Login</span>
                    <div class="btn-shine"></div>
                </button>
            </form>

            <div class="login-footer">
                <p class="demo-info">
                    <i data-feather="info"></i>
                    Demo Credentials: admin / admin123
                </p>
            </div>
        </div>
    </div>

    <!-- Loading Overlay -->
    <div id="loadingOverlay" class="loading-overlay hidden">
        <div class="loading-spinner">
            <div class="spinner"></div>
            <p>Authenticating...</p>
        </div>
    </div>

    <!-- Scripts -->
    <script src="https://cdnjs.cloudflare.com/ajax/libs/feather-icons/4.29.0/feather.min.js"></script>
    <script src="js/main.js"></script>
    <script src="js/admin.js"></script>
    
    <script>
        // Initialize Feather Icons
        feather.replace();
        
        // Initialize page
        document.addEventListener('DOMContentLoaded', function() {
            initializeAdminLogin();
        });

        // Submit admin login
        function submitAdminLogin(event) {
            event.preventDefault();
            
            const formData = new FormData(event.target);
            const username = formData.get('username');
            const password = formData.get('password');
            
            if (!username || !password) {
                showToast('Please fill in all fields', 'error');
                return;
            }
            
            showLoadingOverlay();
            
            fetch('admin-login', {
                method: 'POST',
                body: formData
            })
            .then(response => response.json())
            .then(data => {
                hideLoadingOverlay();
                
                if (data.status === 'success') {
                    showToast(data.message, 'success');
                    setTimeout(() => {
                        window.location.href = data.redirect_url || 'dashboard.jsp';
                    }, 1500);
                } else {
                    showToast(data.message, 'error');
                }
            })
            .catch(error => {
                hideLoadingOverlay();
                console.error('Login error:', error);
                showToast('Login failed. Please try again.', 'error');
            });
        }

        // Toggle password visibility
        function togglePassword() {
            const passwordInput = document.getElementById('password');
            const eyeIcon = document.getElementById('password-eye');
            
            if (passwordInput.type === 'password') {
                passwordInput.type = 'text';
                eyeIcon.setAttribute('data-feather', 'eye-off');
            } else {
                passwordInput.type = 'password';
                eyeIcon.setAttribute('data-feather', 'eye');
            }
            
            feather.replace();
        }

        // Show loading overlay
        function showLoadingOverlay() {
            document.getElementById('loadingOverlay').classList.remove('hidden');
        }

        // Hide loading overlay
        function hideLoadingOverlay() {
            document.getElementById('loadingOverlay').classList.add('hidden');
        }

        // Initialize admin login page
        function initializeAdminLogin() {
            // Focus on username field
            document.getElementById('username').focus();
            
            // Add enter key support
            document.addEventListener('keypress', function(event) {
                if (event.key === 'Enter') {
                    document.getElementById('adminLoginForm').dispatchEvent(new Event('submit'));
                }
            });
        }
    </script>
</body>
</html>
