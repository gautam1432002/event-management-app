/**
 * Main JavaScript file for EventTech application
 * Handles registration forms, certificate generation, modals, and toast notifications
 */

// Global variables
let currentCertificateData = null;
let registrationModal = null;
let certificateModal = null;

// Initialize application
function initializeApp() {
    initializeModals();
    initializeEventListeners();
    loadAvailableEvents();
    
    // Add global error handler for fetch requests
    window.addEventListener('unhandledrejection', function(event) {
        console.error('Unhandled promise rejection:', event.reason);
        showToast('An unexpected error occurred. Please try again.', 'error');
    });
}

// Initialize modals
function initializeModals() {
    registrationModal = document.getElementById('registrationModal');
    certificateModal = document.getElementById('certificateModal');
    
    // Close modals when clicking outside
    window.addEventListener('click', function(event) {
        if (event.target === registrationModal) {
            closeRegistrationModal();
        }
        if (event.target === certificateModal) {
            closeCertificateModal();
        }
    });
    
    // Close modals with Escape key
    document.addEventListener('keydown', function(event) {
        if (event.key === 'Escape') {
            if (registrationModal && registrationModal.style.display === 'flex') {
                closeRegistrationModal();
            }
            if (certificateModal && certificateModal.style.display === 'flex') {
                closeCertificateModal();
            }
        }
    });
}

// Initialize event listeners
function initializeEventListeners() {
    // Form validation on input
    const form = document.getElementById('registrationForm');
    if (form) {
        const inputs = form.querySelectorAll('input, select');
        inputs.forEach(input => {
            input.addEventListener('blur', validateInput);
            input.addEventListener('input', clearInputError);
        });
    }
}

// Load available events for the dropdown
function loadAvailableEvents() {
    fetch('event-settings?action=get_events')
        .then(response => response.json())
        .then(data => {
            if (data.status === 'success' && data.events.length > 0) {
                updateEventDropdown(data.events);
            }
        })
        .catch(error => {
            console.error('Error loading events:', error);
            // Fall back to default events if fetch fails
        });
}

// Update event dropdown with dynamic events
function updateEventDropdown(events) {
    const eventSelect = document.getElementById('event');
    if (!eventSelect) return;
    
    // Keep the first option (placeholder)
    const placeholder = eventSelect.querySelector('option[value=""]');
    eventSelect.innerHTML = '';
    if (placeholder) {
        eventSelect.appendChild(placeholder);
    }
    
    // Add dynamic events
    events.forEach(event => {
        const option = document.createElement('option');
        option.value = event.event_name;
        option.textContent = event.event_name;
        eventSelect.appendChild(option);
    });
}

// Open registration modal
function openRegistrationModal(eventName = '') {
    if (!registrationModal) return;
    
    const form = document.getElementById('registrationForm');
    if (form) {
        form.reset();
        clearAllErrors();
    }
    
    // Pre-select event if provided
    if (eventName) {
        const eventSelect = document.getElementById('event');
        if (eventSelect) {
            eventSelect.value = eventName;
        }
    }
    
    registrationModal.style.display = 'flex';
    
    // Focus on first input
    const firstInput = registrationModal.querySelector('input');
    if (firstInput) {
        setTimeout(() => firstInput.focus(), 100);
    }
    
    // Add modal open animation
    registrationModal.style.opacity = '0';
    setTimeout(() => {
        registrationModal.style.opacity = '1';
    }, 10);
}

// Close registration modal
function closeRegistrationModal() {
    if (!registrationModal) return;
    
    registrationModal.style.opacity = '0';
    setTimeout(() => {
        registrationModal.style.display = 'none';
        const form = document.getElementById('registrationForm');
        if (form) {
            form.reset();
            clearAllErrors();
        }
    }, 300);
}

// Submit registration form
function submitRegistration(event) {
    event.preventDefault();
    
    const form = event.target;
    const formData = new FormData(form);
    
    // Validate form before submission
    if (!validateForm(form)) {
        showToast('Please correct the errors and try again.', 'error');
        return;
    }
    
    // Show loading state
    const submitBtn = form.querySelector('button[type="submit"]');
    const originalText = submitBtn.innerHTML;
    submitBtn.innerHTML = '<div class="spinner"></div><span>Registering...</span>';
    submitBtn.disabled = true;
    
    // Submit registration
    fetch('register', {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        if (data.status === 'success') {
            showToast(data.message, 'success');
            closeRegistrationModal();
            
            // Show certificate if available
            if (data.certificate_data) {
                currentCertificateData = data.certificate_data;
                setTimeout(() => {
                    showCertificate(data.certificate_data);
                }, 1000);
            }
        } else {
            showToast(data.message, 'error');
        }
    })
    .catch(error => {
        console.error('Registration error:', error);
        showToast('Registration failed. Please check your connection and try again.', 'error');
    })
    .finally(() => {
        // Restore button state
        submitBtn.innerHTML = originalText;
        submitBtn.disabled = false;
    });
}

// Validate entire form
function validateForm(form) {
    let isValid = true;
    const inputs = form.querySelectorAll('input[required], select[required]');
    
    inputs.forEach(input => {
        if (!validateInput({ target: input })) {
            isValid = false;
        }
    });
    
    return isValid;
}

// Validate individual input
function validateInput(event) {
    const input = event.target;
    const value = input.value.trim();
    let isValid = true;
    let errorMessage = '';
    
    // Clear previous error
    clearInputError(event);
    
    // Required field validation
    if (input.hasAttribute('required') && !value) {
        errorMessage = `${getFieldLabel(input)} is required`;
        isValid = false;
    }
    // Email validation
    else if (input.type === 'email' && value) {
        const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
        if (!emailRegex.test(value)) {
            errorMessage = 'Please enter a valid email address';
            isValid = false;
        }
    }
    // Name validation
    else if (input.name === 'name' && value) {
        if (value.length < 2) {
            errorMessage = 'Name must be at least 2 characters long';
            isValid = false;
        } else if (!/^[a-zA-Z\s.'-]+$/.test(value)) {
            errorMessage = 'Name can only contain letters, spaces, dots, hyphens, and apostrophes';
            isValid = false;
        }
    }
    // College validation
    else if (input.name === 'college' && value) {
        if (value.length < 2) {
            errorMessage = 'College name must be at least 2 characters long';
            isValid = false;
        }
    }
    
    // Show error if validation failed
    if (!isValid) {
        showInputError(input, errorMessage);
    }
    
    return isValid;
}

// Show input error
function showInputError(input, message) {
    // Add error class to input
    input.classList.add('error');
    
    // Remove existing error message
    const existingError = input.parentElement.querySelector('.error-message');
    if (existingError) {
        existingError.remove();
    }
    
    // Add error message
    const errorElement = document.createElement('div');
    errorElement.className = 'error-message';
    errorElement.textContent = message;
    errorElement.style.color = 'var(--error-color)';
    errorElement.style.fontSize = '0.8rem';
    errorElement.style.marginTop = '5px';
    
    input.parentElement.appendChild(errorElement);
}

// Clear input error
function clearInputError(event) {
    const input = event.target;
    input.classList.remove('error');
    
    const errorMessage = input.parentElement.querySelector('.error-message');
    if (errorMessage) {
        errorMessage.remove();
    }
}

// Clear all form errors
function clearAllErrors() {
    const form = document.getElementById('registrationForm');
    if (!form) return;
    
    const errorInputs = form.querySelectorAll('.error');
    errorInputs.forEach(input => input.classList.remove('error'));
    
    const errorMessages = form.querySelectorAll('.error-message');
    errorMessages.forEach(msg => msg.remove());
}

// Get field label for validation messages
function getFieldLabel(input) {
    const label = input.parentElement.querySelector('label');
    if (label) {
        return label.textContent.replace('*', '').trim();
    }
    
    // Fallback to placeholder or name
    return input.placeholder || input.name || 'Field';
}

// Show certificate modal
function showCertificate(certificateData) {
    if (!certificateModal) return;
    
    currentCertificateData = certificateData;
    const certificateHtml = generateCertificateHTML(certificateData);
    
    const container = document.getElementById('certificateContainer');
    if (container) {
        container.innerHTML = certificateHtml;
    }
    
    certificateModal.style.display = 'flex';
    
    // Add modal open animation
    certificateModal.style.opacity = '0';
    setTimeout(() => {
        certificateModal.style.opacity = '1';
    }, 10);
}

// Close certificate modal
function closeCertificateModal() {
    if (!certificateModal) return;
    
    certificateModal.style.opacity = '0';
    setTimeout(() => {
        certificateModal.style.display = 'none';
    }, 300);
}

// Generate certificate HTML
function generateCertificateHTML(certificateData, isWinner = false) {
    const isWinnerCert = isWinner || certificateData.certificate_type === 'winner';
    const certClass = isWinnerCert ? 'certificate winner-cert' : 'certificate';
    
    const participantName = certificateData.name || 'Participant';
    const eventName = certificateData.event || 'Tech Event';
    const collegeName = certificateData.college || 'Institution';
    const issueDate = formatCertificateDate(certificateData.issue_date || new Date());
    const certificateId = certificateData.certificate_id || 'CERT-' + Date.now();
    
    return `
        <div class="${certClass}">
            <h1 class="cert-title">${isWinnerCert ? 'WINNER CERTIFICATE' : 'CERTIFICATE OF PARTICIPATION'}</h1>
            <h2>TARUNYAM - Tech Event 2025</h2>
            
            <p style="margin: 30px 0 20px 0; font-size: 1.4rem; color: #333;">This is to certify that</p>
            
            <h3 style="border-bottom: 2px solid #1e3a8a; padding-bottom: 10px; margin-bottom: 20px;">${participantName}</h3>
            
            <p style="margin: 20px 0;">from <strong>${collegeName}</strong></p>
            
            <p style="margin: 20px 0; font-size: 1.3rem;">
                has ${isWinnerCert ? '<strong>WON</strong>' : 'successfully participated in'}
            </p>
            
            <h3 style="color: ${isWinnerCert ? '#b45309' : '#1e3a8a'}; margin: 20px 0; font-size: 2.2rem;">
                ${eventName}
            </h3>
            
            <p style="margin: 30px 0 20px 0;">
                ${isWinnerCert ? 
                    'in recognition of outstanding performance and achievement' : 
                    'demonstrating technical skills and innovation'}
            </p>
            
            <div style="display: flex; justify-content: space-between; align-items: end; margin-top: 50px; width: 100%;">
                <div style="text-align: left;">
                    <p style="margin: 0; font-size: 0.9rem; color: #666;">Certificate ID</p>
                    <p style="margin: 5px 0 0 0; font-family: 'Courier New', monospace; font-size: 0.8rem; color: #333;">
                        ${certificateId}
                    </p>
                </div>
                
                <div style="text-align: center;">
                    <div style="border-top: 2px solid #333; width: 200px; margin-bottom: 5px;"></div>
                    <p style="margin: 0; font-weight: 600; color: #333;">Event Organizer</p>
                    <p style="margin: 0; font-size: 0.9rem; color: #666;">TARUNYAM Team</p>
                </div>
                
                <div style="text-align: right;">
                    <p style="margin: 0; font-size: 0.9rem; color: #666;">Date of Issue</p>
                    <p style="margin: 5px 0 0 0; font-weight: 600; color: #333;">${issueDate}</p>
                </div>
            </div>
        </div>
    `;
}

// Format date for certificate
function formatCertificateDate(date) {
    const options = { 
        year: 'numeric', 
        month: 'long', 
        day: 'numeric' 
    };
    return new Date(date).toLocaleDateString('en-US', options);
}

// Download certificate
function downloadCertificate() {
    const certificate = document.querySelector('#certificateContainer .certificate');
    if (!certificate) {
        showToast('Certificate not found. Please try again.', 'error');
        return;
    }
    
    // Show loading state
    const downloadBtn = event.target.closest('button');
    const originalText = downloadBtn.innerHTML;
    downloadBtn.innerHTML = '<div class="spinner"></div><span>Generating...</span>';
    downloadBtn.disabled = true;
    
    // Generate and download certificate
    html2canvas(certificate, {
        scale: 2,
        backgroundColor: '#ffffff',
        useCORS: true,
        allowTaint: true,
        width: certificate.offsetWidth,
        height: certificate.offsetHeight
    }).then(canvas => {
        // Create download link
        const link = document.createElement('a');
        const timestamp = new Date().toISOString().slice(0, 10);
        const participantName = currentCertificateData?.name || 'participant';
        const eventName = currentCertificateData?.event || 'event';
        const isWinner = currentCertificateData?.certificate_type === 'winner';
        
        const filename = `${isWinner ? 'winner' : 'participation'}-certificate-${participantName.replace(/\s+/g, '-').toLowerCase()}-${eventName.replace(/\s+/g, '-').toLowerCase()}-${timestamp}.jpg`;
        
        link.download = filename;
        link.href = canvas.toDataURL('image/jpeg', 0.95);
        
        // Trigger download
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        
        showToast('Certificate downloaded successfully!', 'success');
        
        // Track download (optional - could send to server for analytics)
        if (currentCertificateData?.certificate_id) {
            trackCertificateDownload(currentCertificateData.certificate_id);
        }
    }).catch(error => {
        console.error('Error generating certificate:', error);
        showToast('Failed to generate certificate. Please try again.', 'error');
    }).finally(() => {
        // Restore button state
        downloadBtn.innerHTML = originalText;
        downloadBtn.disabled = false;
    });
}

// Track certificate download (optional analytics)
function trackCertificateDownload(certificateId) {
    // This could be enhanced to send analytics data to the server
    console.log('Certificate downloaded:', certificateId);
}

// Toast notification system
function showToast(message, type = 'info', duration = 5000) {
    const container = document.getElementById('toast-container');
    if (!container) {
        console.warn('Toast container not found');
        return;
    }
    
    // Create toast element
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    
    // Add icon based on type
    const icons = {
        success: '✓',
        error: '✕',
        warning: '⚠',
        info: 'ℹ'
    };
    
    toast.innerHTML = `
        <span class="toast-icon">${icons[type] || icons.info}</span>
        <span class="toast-message">${message}</span>
        <button class="toast-close" onclick="removeToast(this.parentElement)">&times;</button>
    `;
    
    // Add CSS for toast styling
    toast.style.cssText = `
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 15px 20px;
        background: var(--card-bg);
        backdrop-filter: blur(20px);
        border: 1px solid var(--border-color);
        border-radius: 10px;
        color: var(--text-primary);
        box-shadow: 0 10px 25px var(--shadow-color);
        min-width: 300px;
        max-width: 400px;
        animation: toastSlideIn 0.3s ease;
        position: relative;
        overflow: hidden;
        border-left: 4px solid var(--${type === 'success' ? 'success' : type === 'error' ? 'error' : type === 'warning' ? 'warning' : 'primary'}-color);
        margin-bottom: 10px;
    `;
    
    // Style the close button
    const closeBtn = toast.querySelector('.toast-close');
    closeBtn.style.cssText = `
        background: none;
        border: none;
        color: var(--text-secondary);
        cursor: pointer;
        font-size: 18px;
        padding: 0;
        width: 20px;
        height: 20px;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 50%;
        transition: var(--transition);
        margin-left: auto;
    `;
    
    // Add hover effect for close button
    closeBtn.addEventListener('mouseenter', () => {
        closeBtn.style.backgroundColor = 'rgba(255, 255, 255, 0.1)';
        closeBtn.style.color = 'var(--text-primary)';
    });
    
    closeBtn.addEventListener('mouseleave', () => {
        closeBtn.style.backgroundColor = 'transparent';
        closeBtn.style.color = 'var(--text-secondary)';
    });
    
    // Add to container
    container.appendChild(toast);
    
    // Auto remove after duration
    if (duration > 0) {
        setTimeout(() => {
            removeToast(toast);
        }, duration);
    }
    
    // Limit number of toasts
    const maxToasts = 5;
    const toasts = container.querySelectorAll('.toast');
    if (toasts.length > maxToasts) {
        removeToast(toasts[0]);
    }
}

// Remove toast notification
function removeToast(toast) {
    if (!toast || !toast.parentElement) return;
    
    toast.classList.add('slide-out');
    setTimeout(() => {
        if (toast.parentElement) {
            toast.parentElement.removeChild(toast);
        }
    }, 300);
}

// Utility functions
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

// Network status monitoring
function checkNetworkStatus() {
    if (!navigator.onLine) {
        showToast('You appear to be offline. Please check your connection.', 'warning');
    }
}

// Add network event listeners
window.addEventListener('online', () => {
    showToast('Connection restored!', 'success', 3000);
});

window.addEventListener('offline', () => {
    showToast('Connection lost. Some features may not work.', 'warning');
});

// Enhanced error handling for fetch requests
function fetchWithErrorHandling(url, options = {}) {
    return fetch(url, {
        ...options,
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            ...options.headers
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        return response.json();
    })
    .catch(error => {
        console.error('Fetch error:', error);
        
        if (error.name === 'TypeError' && error.message.includes('Failed to fetch')) {
            throw new Error('Network error. Please check your connection and try again.');
        }
        
        throw error;
    });
}

// Initialize app when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    initializeApp();
    checkNetworkStatus();
});

// Export functions for use in other files or inline scripts
window.EventTech = {
    openRegistrationModal,
    closeRegistrationModal,
    submitRegistration,
    showCertificate,
    closeCertificateModal,
    downloadCertificate,
    showToast,
    removeToast,
    initializeApp
};
