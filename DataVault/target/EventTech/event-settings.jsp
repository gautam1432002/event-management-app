<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="javax.servlet.http.HttpSession" %>
<%
    // Check admin authentication
    if (session == null || session.getAttribute("admin_id") == null) {
        response.sendRedirect("admin-login.jsp");
        return;
    }
    
    String adminUsername = (String) session.getAttribute("admin_username");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Event Settings - TARUNYAM Tech Event</title>
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
                <a href="dashboard.jsp" class="nav-back">
                    <i data-feather="arrow-left"></i>
                    Dashboard
                </a>
                <div class="nav-title">Event Settings</div>
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
    <div class="settings-container">
        <!-- Settings Header -->
        <div class="settings-header">
            <h1>Event Management</h1>
            <div class="settings-actions">
                <button class="action-btn primary" onclick="openAddEventModal()">
                    <i data-feather="plus"></i>
                    Add New Event
                </button>
                <button class="action-btn secondary" onclick="refreshEvents()">
                    <i data-feather="refresh-cw"></i>
                    Refresh
                </button>
            </div>
        </div>

        <!-- Events Grid -->
        <div class="events-grid" id="eventsGrid">
            <div class="loading-card">
                <div class="loading-spinner">
                    <div class="spinner"></div>
                    Loading events...
                </div>
            </div>
        </div>
    </div>

    <!-- Add/Edit Event Modal -->
    <div id="eventModal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <h3 id="eventModalTitle">Add New Event</h3>
                <span class="close-btn" onclick="closeEventModal()">&times;</span>
            </div>
            <form id="eventForm" onsubmit="submitEvent(event)">
                <div class="modal-body">
                    <input type="hidden" id="eventId" name="id">
                    
                    <div class="form-group">
                        <label for="eventName">Event Name *</label>
                        <input type="text" id="eventName" name="event_name" required 
                               placeholder="Enter event name">
                    </div>
                    
                    <div class="form-group">
                        <label for="eventDescription">Description *</label>
                        <textarea id="eventDescription" name="description" required 
                                rows="4" placeholder="Enter event description"></textarea>
                    </div>
                </div>
                
                <div class="modal-footer">
                    <button type="button" class="action-btn secondary" onclick="closeEventModal()">
                        Cancel
                    </button>
                    <button type="submit" class="action-btn primary" id="eventSubmitBtn">
                        <span>Add Event</span>
                    </button>
                </div>
            </form>
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
                <p id="confirmationWarning" class="warning-text" style="display: none;"></p>
            </div>
            <div class="modal-footer">
                <button class="action-btn secondary" onclick="closeConfirmationModal()">Cancel</button>
                <button class="action-btn danger" id="confirmActionBtn" onclick="confirmAction()">Confirm</button>
            </div>
        </div>
    </div>

    <!-- Scripts -->
    <script src="https://cdnjs.cloudflare.com/ajax/libs/feather-icons/4.29.0/feather.min.js"></script>
    <script src="js/main.js"></script>
    <script src="js/admin.js"></script>
    
    <script>
        // Initialize Feather Icons
        feather.replace();
        
        // Global variables
        let eventsData = [];
        let pendingAction = null;
        let editingEventId = null;
        
        // Initialize page
        document.addEventListener('DOMContentLoaded', function() {
            loadEvents();
        });
        
        // Load events
        function loadEvents() {
            fetch('event-settings?action=get_events')
                .then(response => response.json())
                .then(data => {
                    if (data.status === 'success') {
                        eventsData = data.events;
                        displayEvents(data.events);
                    } else {
                        showToast(data.message || 'Failed to load events', 'error');
                        displayEvents([]);
                    }
                })
                .catch(error => {
                    console.error('Error loading events:', error);
                    showToast('Failed to load events', 'error');
                    displayEvents([]);
                });
        }
        
        // Display events
        function displayEvents(events) {
            const eventsGrid = document.getElementById('eventsGrid');
            
            if (events.length === 0) {
                eventsGrid.innerHTML = `
                    <div class="empty-state-card">
                        <div class="empty-state">
                            <i data-feather="calendar"></i>
                            <h3>No Events Found</h3>
                            <p>Start by adding your first event</p>
                            <button class="action-btn primary" onclick="openAddEventModal()">
                                <i data-feather="plus"></i>
                                Add New Event
                            </button>
                        </div>
                    </div>
                `;
                feather.replace();
                return;
            }
            
            eventsGrid.innerHTML = events.map(event => `
                <div class="event-card">
                    <div class="event-header">
                        <h3>${escapeHtml(event.event_name)}</h3>
                        <div class="event-stats">
                            <span class="registration-count">
                                <i data-feather="users"></i>
                                ${event.registration_count || 0} registrations
                            </span>
                        </div>
                    </div>
                    
                    <div class="event-description">
                        <p>${escapeHtml(event.description)}</p>
                    </div>
                    
                    <div class="event-actions">
                        <button class="action-btn small primary" onclick="editEvent(${event.id})" title="Edit Event">
                            <i data-feather="edit-2"></i>
                        </button>
                        <button class="action-btn small danger" onclick="deleteEvent(${event.id}, '${escapeHtml(event.event_name)}', ${event.registration_count || 0})" title="Delete Event">
                            <i data-feather="trash-2"></i>
                        </button>
                    </div>
                </div>
            `).join('');
            
            feather.replace();
        }
        
        // Open add event modal
        function openAddEventModal() {
            editingEventId = null;
            document.getElementById('eventModalTitle').textContent = 'Add New Event';
            document.getElementById('eventSubmitBtn').innerHTML = '<span>Add Event</span>';
            document.getElementById('eventForm').reset();
            document.getElementById('eventId').value = '';
            document.getElementById('eventModal').style.display = 'flex';
            document.getElementById('eventName').focus();
        }
        
        // Edit event
        function editEvent(eventId) {
            const event = eventsData.find(e => e.id === eventId);
            if (!event) {
                showToast('Event not found', 'error');
                return;
            }
            
            editingEventId = eventId;
            document.getElementById('eventModalTitle').textContent = 'Edit Event';
            document.getElementById('eventSubmitBtn').innerHTML = '<span>Update Event</span>';
            document.getElementById('eventId').value = event.id;
            document.getElementById('eventName').value = event.event_name;
            document.getElementById('eventDescription').value = event.description;
            document.getElementById('eventModal').style.display = 'flex';
            document.getElementById('eventName').focus();
        }
        
        // Close event modal
        function closeEventModal() {
            document.getElementById('eventModal').style.display = 'none';
            editingEventId = null;
        }
        
        // Submit event form
        function submitEvent(event) {
            event.preventDefault();
            
            const formData = new FormData(event.target);
            const eventName = formData.get('event_name');
            const description = formData.get('description');
            
            if (!eventName || !description) {
                showToast('Please fill in all required fields', 'error');
                return;
            }
            
            const action = editingEventId ? 'update_event' : 'add_event';
            formData.append('action', action);
            
            fetch('event-settings', {
                method: 'POST',
                body: formData
            })
            .then(response => response.json())
            .then(data => {
                if (data.status === 'success') {
                    showToast(data.message, 'success');
                    closeEventModal();
                    loadEvents();
                } else {
                    showToast(data.message, 'error');
                }
            })
            .catch(error => {
                console.error('Error submitting event:', error);
                showToast('Failed to save event', 'error');
            });
        }
        
        // Delete event
        function deleteEvent(eventId, eventName, registrationCount) {
            let message = `Are you sure you want to delete the event "${eventName}"?`;
            let warning = '';
            
            if (registrationCount > 0) {
                warning = `Warning: This event has ${registrationCount} registration(s). Deleting it may not be allowed.`;
            }
            
            showConfirmationModal(
                'Delete Event',
                message,
                warning,
                () => {
                    fetch('event-settings', {
                        method: 'POST',
                        body: new URLSearchParams({
                            action: 'delete_event',
                            id: eventId
                        })
                    })
                    .then(response => response.json())
                    .then(data => {
                        if (data.status === 'success') {
                            showToast(data.message, 'success');
                            loadEvents();
                        } else {
                            showToast(data.message, 'error');
                        }
                    })
                    .catch(error => {
                        console.error('Error deleting event:', error);
                        showToast('Failed to delete event', 'error');
                    });
                }
            );
        }
        
        // Refresh events
        function refreshEvents() {
            showToast('Refreshing events...', 'info');
            loadEvents();
        }
        
        // Show confirmation modal with optional warning
        function showConfirmationModal(title, message, warning, action) {
            document.getElementById('confirmationTitle').textContent = title;
            document.getElementById('confirmationMessage').textContent = message;
            
            const warningElement = document.getElementById('confirmationWarning');
            if (warning) {
                warningElement.textContent = warning;
                warningElement.style.display = 'block';
            } else {
                warningElement.style.display = 'none';
            }
            
            pendingAction = action;
            document.getElementById('confirmationModal').style.display = 'flex';
        }
        
        // Close confirmation modal
        function closeConfirmationModal() {
            document.getElementById('confirmationModal').style.display = 'none';
            pendingAction = null;
        }
        
        // Confirm action
        function confirmAction() {
            if (pendingAction) {
                pendingAction();
                closeConfirmationModal();
            }
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
        
        // Helper function to escape HTML
        function escapeHtml(text) {
            if (!text) return '';
            const div = document.createElement('div');
            div.textContent = text;
            return div.innerHTML;
        }
    </script>
</body>
</html>
