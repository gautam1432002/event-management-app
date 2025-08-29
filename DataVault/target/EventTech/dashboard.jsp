<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="javax.servlet.http.HttpSession" %>
<%
    // Check admin authentication
    if (session == null || session.getAttribute("admin_id") == null) {
        response.sendRedirect("admin-login.jsp");
        return;
    }
    
    String adminUsername = (String) session.getAttribute("admin_username");
    Integer adminId = (Integer) session.getAttribute("admin_id");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard - TARUNYAM Tech Event</title>
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
            <div class="nav-left">
                <a href="index.jsp" class="nav-back">
                    <i data-feather="home"></i>
                    Events
                </a>
                <div class="nav-title">Admin Dashboard</div>
            </div>
            <div class="nav-right">
                <span class="admin-username">
                    <i data-feather="user"></i>
                    <%= adminUsername %>
                </span>
                <button class="nav-btn" onclick="logout()">
                    <i data-feather="log-out"></i>
                    Logout
                </button>
            </div>
        </div>
    </nav>

    <!-- Main Content -->
    <div class="dashboard-container">
        <!-- Dashboard Header -->
        <div class="dashboard-header">
            <h1>Dashboard Overview</h1>
            <div class="dashboard-actions">
                <a href="event-settings.jsp" class="action-btn">
                    <i data-feather="settings"></i>
                    Manage Events
                </a>
                <button class="action-btn" onclick="refreshDashboard()">
                    <i data-feather="refresh-cw"></i>
                    Refresh
                </button>
            </div>
        </div>

        <!-- Statistics Cards -->
        <div class="stats-grid">
            <div class="stat-card">
                <div class="stat-icon">
                    <i data-feather="users"></i>
                </div>
                <div class="stat-content">
                    <h3 id="totalRegistrations">Loading...</h3>
                    <p>Total Registrations</p>
                </div>
            </div>
            
            <div class="stat-card">
                <div class="stat-icon winner">
                    <i data-feather="award"></i>
                </div>
                <div class="stat-content">
                    <h3 id="totalWinners">Loading...</h3>
                    <p>Winners Selected</p>
                </div>
            </div>
            
            <div class="stat-card">
                <div class="stat-icon">
                    <i data-feather="calendar"></i>
                </div>
                <div class="stat-content">
                    <h3 id="totalEvents">Loading...</h3>
                    <p>Active Events</p>
                </div>
            </div>
            
            <div class="stat-card">
                <div class="stat-icon">
                    <i data-feather="file-text"></i>
                </div>
                <div class="stat-content">
                    <h3 id="totalCertificates">Loading...</h3>
                    <p>Certificates Generated</p>
                </div>
            </div>
        </div>

        <!-- Participants Management -->
        <div class="dashboard-section">
            <div class="section-header">
                <h2>Participants Management</h2>
                <div class="section-actions">
                    <button class="action-btn secondary" onclick="exportData('csv')">
                        <i data-feather="download"></i>
                        Export CSV
                    </button>
                    <button class="action-btn secondary" onclick="exportData('html')">
                        <i data-feather="file-text"></i>
                        Export HTML
                    </button>
                </div>
            </div>

            <!-- Filters -->
            <div class="filters">
                <div class="filter-group">
                    <label for="eventFilter">Filter by Event:</label>
                    <select id="eventFilter" onchange="applyFilters()">
                        <option value="">All Events</option>
                    </select>
                </div>
                <div class="filter-group">
                    <label for="statusFilter">Filter by Status:</label>
                    <select id="statusFilter" onchange="applyFilters()">
                        <option value="">All Participants</option>
                        <option value="winner">Winners Only</option>
                        <option value="participant">Participants Only</option>
                    </select>
                </div>
                <div class="filter-group">
                    <button class="action-btn secondary" onclick="clearFilters()">
                        <i data-feather="x"></i>
                        Clear Filters
                    </button>
                </div>
            </div>

            <!-- Participants Table -->
            <div class="table-container">
                <table class="participants-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Name</th>
                            <th>Email</th>
                            <th>College</th>
                            <th>Event</th>
                            <th>Registration Date</th>
                            <th>Status</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody id="participantsTableBody">
                        <tr>
                            <td colspan="8" class="loading-row">
                                <div class="loading-spinner">
                                    <div class="spinner"></div>
                                    Loading participants...
                                </div>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>

            <!-- Pagination -->
            <div class="pagination">
                <button class="pagination-btn" id="prevBtn" onclick="changePage(-1)" disabled>
                    <i data-feather="chevron-left"></i>
                    Previous
                </button>
                <span class="pagination-info" id="paginationInfo">
                    Page 1 of 1
                </span>
                <button class="pagination-btn" id="nextBtn" onclick="changePage(1)" disabled>
                    Next
                    <i data-feather="chevron-right"></i>
                </button>
            </div>
        </div>
    </div>

    <!-- Confirmation Modal -->
    <div id="confirmationModal" class="modal">
        <div class="modal-content confirmation-modal">
            <div class="modal-header">
                <h3 id="confirmationTitle">Confirm Action</h3>
                <span class="close-btn" onclick="closeConfirmationModal()">&times;</span>
            </div>
            <div class="modal-body">
                <p id="confirmationMessage">Are you sure you want to perform this action?</p>
            </div>
            <div class="modal-footer">
                <button class="action-btn secondary" onclick="closeConfirmationModal()">Cancel</button>
                <button class="action-btn danger" id="confirmActionBtn" onclick="confirmAction()">Confirm</button>
            </div>
        </div>
    </div>

    <!-- Winner Certificate Modal -->
    <div id="winnerCertificateModal" class="modal">
        <div class="modal-content certificate-modal-content">
            <span class="close-btn" onclick="closeWinnerCertificateModal()">&times;</span>
            <div id="winnerCertificateContainer">
                <!-- Certificate will be generated here -->
            </div>
            <div class="download-section">
                <button class="reveal-btn" onclick="downloadWinnerCertificate()">
                    <i data-feather="download"></i>
                    <span>Download Winner Certificate</span>
                </button>
            </div>
        </div>
    </div>

    <!-- Scripts -->
    <script src="https://cdnjs.cloudflare.com/ajax/libs/feather-icons/4.29.0/feather.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/html2canvas/1.4.1/html2canvas.min.js"></script>
    <script src="js/main.js"></script>
    <script src="js/admin.js"></script>
    
    <script>
        // Initialize Feather Icons
        feather.replace();
        
        // Global variables
        let currentPage = 1;
        let totalPages = 1;
        let participantsData = [];
        let pendingAction = null;
        
        // Initialize dashboard
        document.addEventListener('DOMContentLoaded', function() {
            initializeDashboard();
        });
        
        // Initialize dashboard
        function initializeDashboard() {
            loadStatistics();
            loadParticipants();
            loadEventFilters();
        }
        
        // Load dashboard statistics
        function loadStatistics() {
            fetch('admin-dashboard?action=get_statistics')
                .then(response => response.json())
                .then(data => {
                    if (data.status === 'success') {
                        const stats = data.statistics;
                        document.getElementById('totalRegistrations').textContent = stats.total_registrations || 0;
                        document.getElementById('totalWinners').textContent = stats.total_winners || 0;
                        document.getElementById('totalEvents').textContent = Object.keys(stats.event_registrations || {}).length;
                        document.getElementById('totalCertificates').textContent = stats.certificate_statistics?.total_certificates || 0;
                    }
                })
                .catch(error => {
                    console.error('Error loading statistics:', error);
                    showToast('Failed to load statistics', 'error');
                });
        }
        
        // Load participants
        function loadParticipants(page = 1) {
            currentPage = page;
            
            fetch(`admin-dashboard?action=get_participants&page=${page}&limit=10`)
                .then(response => response.json())
                .then(data => {
                    if (data.status === 'success') {
                        participantsData = data.participants;
                        totalPages = data.pagination.total_pages;
                        displayParticipants(data.participants);
                        updatePagination(data.pagination);
                    } else {
                        showToast(data.message || 'Failed to load participants', 'error');
                    }
                })
                .catch(error => {
                    console.error('Error loading participants:', error);
                    showToast('Failed to load participants', 'error');
                });
        }
        
        // Display participants in table
        function displayParticipants(participants) {
            const tbody = document.getElementById('participantsTableBody');
            
            if (participants.length === 0) {
                tbody.innerHTML = `
                    <tr>
                        <td colspan="8" class="empty-row">
                            <div class="empty-state">
                                <i data-feather="users"></i>
                                <p>No participants found</p>
                            </div>
                        </td>
                    </tr>
                `;
                feather.replace();
                return;
            }
            
            tbody.innerHTML = participants.map(participant => `
                <tr class="${participant.winner_status ? 'winner-row' : ''}">
                    <td>${participant.id}</td>
                    <td>${escapeHtml(participant.name)}</td>
                    <td>${escapeHtml(participant.email)}</td>
                    <td>${escapeHtml(participant.college)}</td>
                    <td>${escapeHtml(participant.event)}</td>
                    <td>${formatDate(participant.registration_date)}</td>
                    <td>
                        <span class="status-badge ${participant.winner_status ? 'winner' : 'participant'}">
                            ${participant.winner_status ? '🏆 Winner' : '✅ Participant'}
                        </span>
                    </td>
                    <td class="actions-cell">
                        ${!participant.winner_status ? 
                            `<button class="action-btn small winner" onclick="selectWinner(${participant.id})" title="Select as Winner">
                                <i data-feather="award"></i>
                            </button>` :
                            `<button class="action-btn small secondary" onclick="revokeWinner(${participant.id})" title="Revoke Winner">
                                <i data-feather="x"></i>
                            </button>
                            <button class="action-btn small primary" onclick="generateWinnerCertificate(${participant.id})" title="Generate Certificate">
                                <i data-feather="file-text"></i>
                            </button>`
                        }
                        <button class="action-btn small danger" onclick="deleteParticipant(${participant.id})" title="Delete">
                            <i data-feather="trash-2"></i>
                        </button>
                    </td>
                </tr>
            `).join('');
            
            feather.replace();
        }
        
        // Update pagination
        function updatePagination(pagination) {
            document.getElementById('paginationInfo').textContent = 
                `Page ${pagination.current_page} of ${pagination.total_pages} (${pagination.total_count} total)`;
            
            document.getElementById('prevBtn').disabled = !pagination.has_previous;
            document.getElementById('nextBtn').disabled = !pagination.has_next;
        }
        
        // Change page
        function changePage(direction) {
            const newPage = currentPage + direction;
            if (newPage >= 1 && newPage <= totalPages) {
                loadParticipants(newPage);
            }
        }
        
        // Refresh dashboard
        function refreshDashboard() {
            showToast('Refreshing dashboard...', 'info');
            loadStatistics();
            loadParticipants(currentPage);
        }
        
        // Logout
        function logout() {
            if (confirm('Are you sure you want to logout?')) {
                fetch('admin-login?action=logout')
                    .then(response => response.json())
                    .then(data => {
                        if (data.status === 'success') {
                            showToast('Logged out successfully', 'success');
                            setTimeout(() => {
                                window.location.href = data.redirect_url || 'admin-login.jsp';
                            }, 1000);
                        }
                    })
                    .catch(error => {
                        console.error('Logout error:', error);
                        window.location.href = 'admin-login.jsp';
                    });
            }
        }
        
        // Export data
        function exportData(format) {
            const url = `export?format=${format}`;
            window.open(url, '_blank');
            showToast(`Exporting data as ${format.toUpperCase()}...`, 'info');
        }
        
        // Select winner
        function selectWinner(participantId) {
            showConfirmationModal(
                'Select Winner',
                'Are you sure you want to select this participant as a winner?',
                () => {
                    fetch('winner', {
                        method: 'POST',
                        body: new URLSearchParams({
                            action: 'select_winner',
                            id: participantId
                        })
                    })
                    .then(response => response.json())
                    .then(data => {
                        if (data.status === 'success') {
                            showToast(data.message, 'success');
                            loadParticipants(currentPage);
                            loadStatistics();
                            
                            // Show winner certificate if available
                            if (data.certificate_data) {
                                showWinnerCertificate(data.certificate_data);
                            }
                        } else {
                            showToast(data.message, 'error');
                        }
                    })
                    .catch(error => {
                        console.error('Error selecting winner:', error);
                        showToast('Failed to select winner', 'error');
                    });
                }
            );
        }
        
        // Revoke winner
        function revokeWinner(participantId) {
            showConfirmationModal(
                'Revoke Winner',
                'Are you sure you want to revoke winner status from this participant?',
                () => {
                    fetch('winner', {
                        method: 'POST',
                        body: new URLSearchParams({
                            action: 'revoke_winner',
                            id: participantId
                        })
                    })
                    .then(response => response.json())
                    .then(data => {
                        if (data.status === 'success') {
                            showToast(data.message, 'success');
                            loadParticipants(currentPage);
                            loadStatistics();
                        } else {
                            showToast(data.message, 'error');
                        }
                    })
                    .catch(error => {
                        console.error('Error revoking winner:', error);
                        showToast('Failed to revoke winner', 'error');
                    });
                }
            );
        }
        
        // Delete participant
        function deleteParticipant(participantId) {
            showConfirmationModal(
                'Delete Participant',
                'Are you sure you want to delete this participant? This action cannot be undone.',
                () => {
                    fetch('admin-dashboard', {
                        method: 'POST',
                        body: new URLSearchParams({
                            action: 'delete_participant',
                            id: participantId
                        })
                    })
                    .then(response => response.json())
                    .then(data => {
                        if (data.status === 'success') {
                            showToast(data.message, 'success');
                            loadParticipants(currentPage);
                            loadStatistics();
                        } else {
                            showToast(data.message, 'error');
                        }
                    })
                    .catch(error => {
                        console.error('Error deleting participant:', error);
                        showToast('Failed to delete participant', 'error');
                    });
                }
            );
        }
        
        // Generate winner certificate
        function generateWinnerCertificate(participantId) {
            fetch('winner', {
                method: 'POST',
                body: new URLSearchParams({
                    action: 'generate_winner_certificate',
                    id: participantId
                })
            })
            .then(response => response.json())
            .then(data => {
                if (data.status === 'success') {
                    showToast(data.message, 'success');
                    showWinnerCertificate(data.certificate_data);
                } else {
                    showToast(data.message, 'error');
                }
            })
            .catch(error => {
                console.error('Error generating certificate:', error);
                showToast('Failed to generate certificate', 'error');
            });
        }
        
        // Show winner certificate modal
        function showWinnerCertificate(certificateData) {
            const certificateHtml = generateCertificateHTML(certificateData, true);
            document.getElementById('winnerCertificateContainer').innerHTML = certificateHtml;
            document.getElementById('winnerCertificateModal').style.display = 'flex';
        }
        
        // Close winner certificate modal
        function closeWinnerCertificateModal() {
            document.getElementById('winnerCertificateModal').style.display = 'none';
        }
        
        // Download winner certificate
        function downloadWinnerCertificate() {
            const certificate = document.querySelector('#winnerCertificateContainer .certificate');
            if (certificate) {
                html2canvas(certificate, {
                    scale: 2,
                    backgroundColor: '#ffffff'
                }).then(canvas => {
                    const link = document.createElement('a');
                    link.download = 'winner-certificate.jpg';
                    link.href = canvas.toDataURL('image/jpeg', 0.95);
                    link.click();
                    showToast('Certificate downloaded successfully!', 'success');
                });
            }
        }
        
        // Helper functions
        function escapeHtml(text) {
            const div = document.createElement('div');
            div.textContent = text;
            return div.innerHTML;
        }
        
        function formatDate(dateString) {
            return new Date(dateString).toLocaleDateString('en-US', {
                year: 'numeric',
                month: 'short',
                day: 'numeric',
                hour: '2-digit',
                minute: '2-digit'
            });
        }
    </script>
</body>
</html>
