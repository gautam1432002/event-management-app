/**
 * Admin JavaScript file for EventTech application
 * Handles admin dashboard functionality, participant management, and event settings
 */

// Global admin variables
let currentAdminUser = null;
let participantsCache = [];
let eventsCache = [];
let currentFilters = {
    event: '',
    status: '',
    search: ''
};

// Initialize admin functionality
function initializeAdmin() {
    loadAdminUser();
    initializeAdminEventListeners();
    setupAdminKeybindings();
    
    // Check authentication status
    checkAdminAuthentication();
}

// Load admin user information
function loadAdminUser() {
    const adminUsername = document.querySelector('.admin-username');
    if (adminUsername) {
        currentAdminUser = {
            username: adminUsername.textContent.trim()
        };
    }
}

// Initialize admin event listeners
function initializeAdminEventListeners() {
    // Confirmation modal listeners
    setupConfirmationModal();
    
    // Search functionality
    setupSearchFunctionality();
    
    // Keyboard shortcuts
    document.addEventListener('keydown', handleAdminKeybindings);
    
    // Auto-refresh functionality
    setupAutoRefresh();
}

// Setup confirmation modal
function setupConfirmationModal() {
    const modal = document.getElementById('confirmationModal');
    if (modal) {
        // Close on outside click
        modal.addEventListener('click', function(event) {
            if (event.target === modal) {
                closeConfirmationModal();
            }
        });
    }
}

// Setup search functionality
function setupSearchFunctionality() {
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        let searchTimeout;
        
        searchInput.addEventListener('input', function(event) {
            clearTimeout(searchTimeout);
            searchTimeout = setTimeout(() => {
                currentFilters.search = event.target.value.trim();
                applyFilters();
            }, 300);
        });
    }
}

// Setup auto-refresh
function setupAutoRefresh() {
    // Auto-refresh every 5 minutes for dashboard
    if (window.location.pathname.includes('dashboard.jsp')) {
        setInterval(() => {
            if (document.visibilityState === 'visible') {
                refreshDashboardData();
            }
        }, 300000); // 5 minutes
    }
}

// Setup admin keybindings
function setupAdminKeybindings() {
    document.addEventListener('keydown', handleAdminKeybindings);
}

// Handle admin keyboard shortcuts
function handleAdminKeybindings(event) {
    // Ctrl/Cmd + R: Refresh dashboard
    if ((event.ctrlKey || event.metaKey) && event.key === 'r' && !event.shiftKey) {
        event.preventDefault();
        if (typeof refreshDashboard === 'function') {
            refreshDashboard();
        }
    }
    
    // Ctrl/Cmd + E: Export data
    if ((event.ctrlKey || event.metaKey) && event.key === 'e') {
        event.preventDefault();
        if (typeof exportData === 'function') {
            exportData('csv');
        }
    }
    
    // Ctrl/Cmd + N: Add new event (on event settings page)
    if ((event.ctrlKey || event.metaKey) && event.key === 'n') {
        event.preventDefault();
        if (typeof openAddEventModal === 'function') {
            openAddEventModal();
        }
    }
    
    // Escape: Close any open modal
    if (event.key === 'Escape') {
        closeAllModals();
    }
}

// Close all modals
function closeAllModals() {
    const modals = document.querySelectorAll('.modal');
    modals.forEach(modal => {
        if (modal.style.display === 'flex') {
            modal.style.display = 'none';
        }
    });
    
    // Call specific close functions if they exist
    if (typeof closeConfirmationModal === 'function') {
        closeConfirmationModal();
    }
    if (typeof closeEventModal === 'function') {
        closeEventModal();
    }
}

// Check admin authentication
function checkAdminAuthentication() {
    // Check if we're on an admin page and user is not authenticated
    if (!currentAdminUser && window.location.pathname.includes('dashboard.jsp')) {
        showToast('Session expired. Please login again.', 'warning');
        setTimeout(() => {
            window.location.href = 'admin-login.jsp';
        }, 2000);
    }
}

// Enhanced confirmation modal
function showConfirmationModal(title, message, warning, action) {
    const modal = document.getElementById('confirmationModal');
    const titleElement = document.getElementById('confirmationTitle');
    const messageElement = document.getElementById('confirmationMessage');
    const warningElement = document.getElementById('confirmationWarning');
    const confirmBtn = document.getElementById('confirmActionBtn');
    
    if (!modal || !titleElement || !messageElement || !confirmBtn) return;
    
    titleElement.textContent = title;
    messageElement.textContent = message;
    
    // Handle warning text
    if (warning && warningElement) {
        warningElement.textContent = warning;
        warningElement.style.display = 'block';
    } else if (warningElement) {
        warningElement.style.display = 'none';
    }
    
    // Store action for later execution
    confirmBtn.onclick = () => {
        if (action && typeof action === 'function') {
            action();
        }
        closeConfirmationModal();
    };
    
    modal.style.display = 'flex';
    
    // Focus on confirm button for keyboard navigation
    setTimeout(() => confirmBtn.focus(), 100);
}

// Close confirmation modal
function closeConfirmationModal() {
    const modal = document.getElementById('confirmationModal');
    if (modal) {
        modal.style.display = 'none';
    }
}

// Confirm action (called by confirmation modal)
function confirmAction() {
    // This will be overridden by the action set in showConfirmationModal
    closeConfirmationModal();
}

// Load event filters for dashboard
function loadEventFilters() {
    fetch('event-settings?action=get_events')
        .then(response => response.json())
        .then(data => {
            if (data.status === 'success') {
                updateEventFilterDropdown(data.events);
                eventsCache = data.events;
            }
        })
        .catch(error => {
            console.error('Error loading event filters:', error);
        });
}

// Update event filter dropdown
function updateEventFilterDropdown(events) {
    const eventFilter = document.getElementById('eventFilter');
    if (!eventFilter) return;
    
    // Save current selection
    const currentValue = eventFilter.value;
    
    // Clear existing options (except "All Events")
    const options = eventFilter.querySelectorAll('option[value!=""]');
    options.forEach(option => option.remove());
    
    // Add events to filter
    events.forEach(event => {
        const option = document.createElement('option');
        option.value = event.event_name;
        option.textContent = event.event_name;
        eventFilter.appendChild(option);
    });
    
    // Restore selection if it still exists
    if (currentValue && events.some(e => e.event_name === currentValue)) {
        eventFilter.value = currentValue;
    }
}

// Apply filters to participants table
function applyFilters() {
    const eventFilter = document.getElementById('eventFilter');
    const statusFilter = document.getElementById('statusFilter');
    
    if (eventFilter) {
        currentFilters.event = eventFilter.value;
    }
    if (statusFilter) {
        currentFilters.status = statusFilter.value;
    }
    
    // Reload participants with filters
    if (typeof loadParticipants === 'function') {
        loadParticipants(1); // Reset to first page
    }
}

// Clear all filters
function clearFilters() {
    const eventFilter = document.getElementById('eventFilter');
    const statusFilter = document.getElementById('statusFilter');
    const searchInput = document.getElementById('searchInput');
    
    if (eventFilter) eventFilter.value = '';
    if (statusFilter) statusFilter.value = '';
    if (searchInput) searchInput.value = '';
    
    currentFilters = {
        event: '',
        status: '',
        search: ''
    };
    
    // Reload participants
    if (typeof loadParticipants === 'function') {
        loadParticipants(1);
    }
    
    showToast('Filters cleared', 'info', 2000);
}

// Refresh dashboard data
function refreshDashboardData() {
    if (typeof loadStatistics === 'function') {
        loadStatistics();
    }
    if (typeof loadParticipants === 'function') {
        loadParticipants(1);
    }
    
    showToast('Dashboard refreshed', 'success', 2000);
}

// Enhanced participant management functions
function bulkSelectWinners() {
    const selectedParticipants = getSelectedParticipants();
    
    if (selectedParticipants.length === 0) {
        showToast('Please select participants to mark as winners', 'warning');
        return;
    }
    
    showConfirmationModal(
        'Bulk Select Winners',
        `Are you sure you want to mark ${selectedParticipants.length} participant(s) as winners?`,
        'This action will generate winner certificates for all selected participants.',
        () => {
            processBulkWinnerSelection(selectedParticipants);
        }
    );
}

// Process bulk winner selection
function processBulkWinnerSelection(participants) {
    let completed = 0;
    let failed = 0;
    
    const updateProgress = () => {
        const total = participants.length;
        const progress = ((completed + failed) / total * 100).toFixed(1);
        showToast(`Processing: ${progress}% complete`, 'info', 1000);
    };
    
    // Process participants one by one to avoid overwhelming the server
    participants.forEach((participantId, index) => {
        setTimeout(() => {
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
                    completed++;
                } else {
                    failed++;
                    console.error(`Failed to select winner ${participantId}:`, data.message);
                }
                
                updateProgress();
                
                // If this is the last participant, refresh the dashboard
                if (completed + failed === participants.length) {
                    const message = `Bulk operation completed. ${completed} successful, ${failed} failed.`;
                    showToast(message, failed === 0 ? 'success' : 'warning');
                    
                    if (typeof loadParticipants === 'function') {
                        loadParticipants(1);
                    }
                    if (typeof loadStatistics === 'function') {
                        loadStatistics();
                    }
                }
            })
            .catch(error => {
                failed++;
                console.error(`Error processing participant ${participantId}:`, error);
                updateProgress();
            });
        }, index * 200); // Stagger requests by 200ms
    });
}

// Get selected participants (if checkboxes are implemented)
function getSelectedParticipants() {
    const checkboxes = document.querySelectorAll('.participant-checkbox:checked');
    return Array.from(checkboxes).map(cb => parseInt(cb.value));
}

// Enhanced table sorting
function sortTable(column, direction = 'asc') {
    if (!participantsCache.length) return;
    
    const sortedData = [...participantsCache].sort((a, b) => {
        let aVal = a[column];
        let bVal = b[column];
        
        // Handle different data types
        if (typeof aVal === 'string') {
            aVal = aVal.toLowerCase();
            bVal = bVal.toLowerCase();
        }
        
        if (aVal < bVal) return direction === 'asc' ? -1 : 1;
        if (aVal > bVal) return direction === 'asc' ? 1 : -1;
        return 0;
    });
    
    displayParticipants(sortedData);
    updateSortIndicators(column, direction);
}

// Update sort indicators in table headers
function updateSortIndicators(activeColumn, direction) {
    const headers = document.querySelectorAll('.sortable-header');
    headers.forEach(header => {
        const column = header.dataset.column;
        const indicator = header.querySelector('.sort-indicator');
        
        if (indicator) {
            if (column === activeColumn) {
                indicator.textContent = direction === 'asc' ? '↑' : '↓';
                indicator.style.opacity = '1';
            } else {
                indicator.textContent = '↕';
                indicator.style.opacity = '0.3';
            }
        }
    });
}

// Advanced export functionality
function exportDataAdvanced(format, options = {}) {
    const params = new URLSearchParams({
        format: format,
        ...currentFilters,
        ...options
    });
    
    // Show loading state
    showToast(`Preparing ${format.toUpperCase()} export...`, 'info');
    
    // Create hidden iframe for download
    const iframe = document.createElement('iframe');
    iframe.style.display = 'none';
    iframe.src = `export?${params.toString()}`;
    
    document.body.appendChild(iframe);
    
    // Clean up iframe after download
    setTimeout(() => {
        document.body.removeChild(iframe);
    }, 5000);
    
    // Track export
    trackAdminAction('export_data', { format, filters: currentFilters });
}

// Track admin actions for analytics
function trackAdminAction(action, data = {}) {
    const trackingData = {
        action,
        timestamp: new Date().toISOString(),
        user: currentAdminUser?.username,
        page: window.location.pathname,
        ...data
    };
    
    // Log locally (could be sent to analytics service)
    console.log('Admin action tracked:', trackingData);
    
    // Could send to server for audit logging
    // fetch('admin-analytics', { method: 'POST', body: JSON.stringify(trackingData) });
}

// Enhanced error handling for admin operations
function handleAdminError(error, context = '') {
    console.error(`Admin error${context ? ` in ${context}` : ''}:`, error);
    
    let message = 'An unexpected error occurred.';
    
    if (error.message) {
        if (error.message.includes('Network error')) {
            message = 'Network error. Please check your connection and try again.';
        } else if (error.message.includes('Unauthorized')) {
            message = 'Session expired. Please login again.';
            setTimeout(() => {
                window.location.href = 'admin-login.jsp';
            }, 2000);
        } else {
            message = error.message;
        }
    }
    
    showToast(message, 'error');
}

// Utility function for debouncing
function debounce(func, wait, immediate = false) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            timeout = null;
            if (!immediate) func(...args);
        };
        const callNow = immediate && !timeout;
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
        if (callNow) func(...args);
    };
}

// Enhanced data validation for admin forms
function validateAdminForm(form) {
    const errors = [];
    const requiredFields = form.querySelectorAll('[required]');
    
    requiredFields.forEach(field => {
        if (!field.value.trim()) {
            errors.push(`${getFieldLabel(field)} is required`);
            field.classList.add('error');
        } else {
            field.classList.remove('error');
        }
    });
    
    // Custom validations
    const eventNameField = form.querySelector('[name="event_name"]');
    if (eventNameField && eventNameField.value.trim()) {
        const eventName = eventNameField.value.trim();
        if (eventName.length < 3) {
            errors.push('Event name must be at least 3 characters long');
            eventNameField.classList.add('error');
        }
        if (eventName.length > 100) {
            errors.push('Event name must be less than 100 characters');
            eventNameField.classList.add('error');
        }
    }
    
    const descriptionField = form.querySelector('[name="description"]');
    if (descriptionField && descriptionField.value.trim()) {
        const description = descriptionField.value.trim();
        if (description.length < 10) {
            errors.push('Event description must be at least 10 characters long');
            descriptionField.classList.add('error');
        }
        if (description.length > 500) {
            errors.push('Event description must be less than 500 characters');
            descriptionField.classList.add('error');
        }
    }
    
    if (errors.length > 0) {
        showToast(errors[0], 'error');
        return false;
    }
    
    return true;
}

// Get field label for validation
function getFieldLabel(field) {
    const label = field.previousElementSibling;
    if (label && label.tagName === 'LABEL') {
        return label.textContent.replace('*', '').trim();
    }
    return field.name || field.placeholder || 'Field';
}

// Session management
function checkSessionValidity() {
    fetch('admin-dashboard?action=check_session', {
        method: 'GET',
        credentials: 'same-origin'
    })
    .then(response => response.json())
    .then(data => {
        if (data.status === 'error' && data.message.includes('Unauthorized')) {
            showToast('Session expired. Redirecting to login...', 'warning');
            setTimeout(() => {
                window.location.href = 'admin-login.jsp';
            }, 2000);
        }
    })
    .catch(error => {
        console.error('Session check failed:', error);
    });
}

// Check session every 10 minutes
setInterval(checkSessionValidity, 600000);

// Page visibility API to pause updates when tab is not active
document.addEventListener('visibilitychange', function() {
    if (document.visibilityState === 'visible') {
        // Page became visible, refresh data if it's been more than 5 minutes
        const lastRefresh = localStorage.getItem('lastAdminRefresh');
        const now = Date.now();
        
        if (!lastRefresh || now - parseInt(lastRefresh) > 300000) {
            refreshDashboardData();
            localStorage.setItem('lastAdminRefresh', now.toString());
        }
    }
});

// Initialize admin functionality when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    initializeAdmin();
});

// Export admin functions for global access
window.AdminEventTech = {
    showConfirmationModal,
    closeConfirmationModal,
    confirmAction,
    applyFilters,
    clearFilters,
    refreshDashboardData,
    bulkSelectWinners,
    sortTable,
    exportDataAdvanced,
    trackAdminAction,
    handleAdminError,
    validateAdminForm,
    checkSessionValidity,
    initializeAdmin
};
