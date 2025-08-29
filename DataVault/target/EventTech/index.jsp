<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.event.dao.EventDAO" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>TARUNYAM - Tech Event 2025</title>
    <link rel="stylesheet" href="css/style.css">
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/feather-icons/4.29.0/feather.min.css">
</head>
<body>
    <!-- Background Effects -->
    <div class="bg-effects">
        <div class="bg-gradient"></div>
        <div class="bg-pattern"></div>
    </div>

    <!-- Toast Notification Container -->
    <div id="toast-container" class="toast-container"></div>

    <!-- Main Container -->
    <div class="container">
        <header>
            <div class="header-content">
                <h1 class="title">TARUNYAM</h1>
                <p class="subtitle">Tech Event 2025</p>
                <div class="header-shine"></div>
            </div>
        </header>

        <!-- Event Cards Grid -->
        <div class="game-grid">
            <%
                EventDAO eventDAO = new EventDAO();
                List<Map<String, Object>> events = eventDAO.getAllEvents();
                
                // Default events if database is empty
                String[][] defaultEvents = {
                    {"Code Rush", "Speed coding competition with algorithmic challenges"},
                    {"Tech Quiz", "Test your knowledge in latest technologies and programming"},
                    {"Hackathon", "24-hour coding marathon to build innovative solutions"},
                    {"Web Master", "Showcase your web development and design skills"},
                    {"Debug Dash", "Find and fix bugs in given code snippets quickly"},
                    {"AI Challenge", "Machine learning and artificial intelligence competition"}
                };
                
                if (events.isEmpty()) {
                    // Display default events
                    for (String[] event : defaultEvents) {
            %>
                        <div class="game-card">
                            <div class="card-glow"></div>
                            <h3><%= event[0] %></h3>
                            <p><%= event[1] %></p>
                            <button class="reveal-btn" onclick="openRegistrationModal('<%= event[0] %>')">
                                <span>Register Now</span>
                                <div class="btn-shine"></div>
                            </button>
                        </div>
            <%
                    }
                } else {
                    // Display events from database
                    for (Map<String, Object> event : events) {
            %>
                        <div class="game-card">
                            <div class="card-glow"></div>
                            <h3><%= event.get("event_name") %></h3>
                            <p><%= event.get("description") %></p>
                            <button class="reveal-btn" onclick="openRegistrationModal('<%= event.get("event_name") %>')">
                                <span>Register Now</span>
                                <div class="btn-shine"></div>
                            </button>
                        </div>
            <%
                    }
                }
            %>
        </div>

        <!-- Admin Access -->
        <div class="admin-access">
            <a href="admin-login.jsp" class="admin-link">
                <i data-feather="settings"></i>
                Admin Panel
            </a>
        </div>
    </div>

    <!-- Registration Modal -->
    <div id="registrationModal" class="modal">
        <div class="modal-content">
            <span class="close-btn" onclick="closeRegistrationModal()">&times;</span>
            <h2>Event Registration</h2>
            <form id="registrationForm" onsubmit="submitRegistration(event)">
                <div class="form-group">
                    <label for="fullName">Full Name *</label>
                    <input type="text" id="fullName" name="name" required>
                </div>
                
                <div class="form-group">
                    <label for="email">Email Address *</label>
                    <input type="email" id="email" name="email" required>
                </div>
                
                <div class="form-group">
                    <label for="college">College Name *</label>
                    <input type="text" id="college" name="college" required>
                </div>
                
                <div class="form-group">
                    <label for="event">Event *</label>
                    <select id="event" name="event" required>
                        <option value="">Select an event</option>
                        <%
                            if (events.isEmpty()) {
                                for (String[] event : defaultEvents) {
                        %>
                                    <option value="<%= event[0] %>"><%= event[0] %></option>
                        <%
                                }
                            } else {
                                for (Map<String, Object> event : events) {
                        %>
                                    <option value="<%= event.get("event_name") %>"><%= event.get("event_name") %></option>
                        <%
                                }
                            }
                        %>
                    </select>
                </div>
                
                <button type="submit" class="reveal-btn submit-btn">
                    <span>Register</span>
                    <div class="btn-shine"></div>
                </button>
            </form>
        </div>
    </div>

    <!-- Certificate Modal -->
    <div id="certificateModal" class="modal">
        <div class="modal-content certificate-modal-content">
            <span class="close-btn" onclick="closeCertificateModal()">&times;</span>
            <div id="certificateContainer">
                <!-- Certificate will be generated here -->
            </div>
            <div class="download-section">
                <button class="reveal-btn" onclick="downloadCertificate()">
                    <i data-feather="download"></i>
                    <span>Download Certificate</span>
                </button>
            </div>
        </div>
    </div>

    <!-- Scripts -->
    <script src="https://cdnjs.cloudflare.com/ajax/libs/feather-icons/4.29.0/feather.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/html2canvas/1.4.1/html2canvas.min.js"></script>
    <script src="js/main.js"></script>
    
    <script>
        // Initialize Feather Icons
        feather.replace();
        
        // Initialize page
        document.addEventListener('DOMContentLoaded', function() {
            initializeApp();
        });
    </script>
</body>
</html>
