package com.event.servlets;

import com.event.dao.EventDAO;
import com.event.dao.AdminDAO;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/**
 * Servlet for event management operations
 * Handles adding, editing, and deleting events
 */
@WebServlet("/event-settings")
public class EventSettingsServlet extends HttpServlet {
    
    private EventDAO eventDAO;
    private AdminDAO adminDAO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        eventDAO = new EventDAO();
        adminDAO = new AdminDAO();
    }
    
    /**
     * Handle GET requests for fetching events
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Check admin authentication
        if (!AdminLoginServlet.isAdminAuthenticated(request)) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "Unauthorized access. Please login first.");
            
            PrintWriter out = response.getWriter();
            out.print(jsonResponse.toString());
            return;
        }
        
        String action = request.getParameter("action");
        
        if ("get_events".equals(action)) {
            getEvents(request, response);
        } else if ("get_event".equals(action)) {
            getEvent(request, response);
        } else {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "Invalid action specified");
            
            PrintWriter out = response.getWriter();
            out.print(jsonResponse.toString());
        }
    }
    
    /**
     * Handle POST requests for event management
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Check admin authentication
        if (!AdminLoginServlet.isAdminAuthenticated(request)) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "Unauthorized access. Please login first.");
            
            PrintWriter out = response.getWriter();
            out.print(jsonResponse.toString());
            return;
        }
        
        String action = request.getParameter("action");
        
        if ("add_event".equals(action)) {
            addEvent(request, response);
        } else if ("update_event".equals(action)) {
            updateEvent(request, response);
        } else if ("delete_event".equals(action)) {
            deleteEvent(request, response);
        } else {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "Invalid action specified");
            
            PrintWriter out = response.getWriter();
            out.print(jsonResponse.toString());
        }
    }
    
    /**
     * Get all events
     */
    private void getEvents(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        JSONObject jsonResponse = new JSONObject();
        
        try {
            List<Map<String, Object>> events = eventDAO.getAllEvents();
            Map<String, Integer> eventCounts = eventDAO.getEventRegistrationCounts();
            
            JSONArray eventsArray = new JSONArray();
            for (Map<String, Object> event : events) {
                JSONObject eventObj = new JSONObject(event);
                String eventName = (String) event.get("event_name");
                int registrationCount = eventCounts.getOrDefault(eventName, 0);
                eventObj.put("registration_count", registrationCount);
                eventsArray.put(eventObj);
            }
            
            jsonResponse.put("status", "success");
            jsonResponse.put("events", eventsArray);
            
        } catch (Exception e) {
            System.err.println("Error fetching events: " + e.getMessage());
            e.printStackTrace();
            
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "Failed to fetch events");
        } finally {
            out.print(jsonResponse.toString());
            out.flush();
        }
    }
    
    /**
     * Get a specific event by ID
     */
    private void getEvent(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        JSONObject jsonResponse = new JSONObject();
        
        try {
            String idParam = request.getParameter("id");
            
            if (idParam == null || idParam.trim().isEmpty()) {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Event ID is required");
                out.print(jsonResponse.toString());
                return;
            }
            
            int eventId;
            try {
                eventId = Integer.parseInt(idParam);
            } catch (NumberFormatException e) {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Invalid event ID format");
                out.print(jsonResponse.toString());
                return;
            }
            
            Map<String, Object> event = eventDAO.getEventById(eventId);
            
            if (event != null) {
                jsonResponse.put("status", "success");
                jsonResponse.put("event", new JSONObject(event));
            } else {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Event not found");
            }
            
        } catch (Exception e) {
            System.err.println("Error fetching event: " + e.getMessage());
            e.printStackTrace();
            
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "Failed to fetch event");
        } finally {
            out.print(jsonResponse.toString());
            out.flush();
        }
    }
    
    /**
     * Add a new event
     */
    private void addEvent(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        JSONObject jsonResponse = new JSONObject();
        
        try {
            String eventName = request.getParameter("event_name");
            String description = request.getParameter("description");
            
            // Validate input
            if (eventName == null || eventName.trim().isEmpty()) {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Event name is required");
                out.print(jsonResponse.toString());
                return;
            }
            
            if (description == null || description.trim().isEmpty()) {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Event description is required");
                out.print(jsonResponse.toString());
                return;
            }
            
            // Check if event name already exists
            if (eventDAO.eventNameExists(eventName)) {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Event name already exists");
                out.print(jsonResponse.toString());
                return;
            }
            
            // Add event
            int eventId = eventDAO.addEvent(eventName, description);
            
            if (eventId > 0) {
                // Log admin action
                Integer adminId = AdminLoginServlet.getAdminId(request);
                if (adminId != null) {
                    adminDAO.logAdminAction(adminId, "Added new event: " + eventName);
                }
                
                jsonResponse.put("status", "success");
                jsonResponse.put("message", "Event added successfully");
                jsonResponse.put("event_id", eventId);
            } else {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Failed to add event");
            }
            
        } catch (Exception e) {
            System.err.println("Error adding event: " + e.getMessage());
            e.printStackTrace();
            
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "An unexpected error occurred");
        } finally {
            out.print(jsonResponse.toString());
            out.flush();
        }
    }
    
    /**
     * Update an existing event
     */
    private void updateEvent(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        JSONObject jsonResponse = new JSONObject();
        
        try {
            String idParam = request.getParameter("id");
            String eventName = request.getParameter("event_name");
            String description = request.getParameter("description");
            
            // Validate input
            if (idParam == null || idParam.trim().isEmpty()) {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Event ID is required");
                out.print(jsonResponse.toString());
                return;
            }
            
            int eventId;
            try {
                eventId = Integer.parseInt(idParam);
            } catch (NumberFormatException e) {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Invalid event ID format");
                out.print(jsonResponse.toString());
                return;
            }
            
            if (eventName == null || eventName.trim().isEmpty()) {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Event name is required");
                out.print(jsonResponse.toString());
                return;
            }
            
            if (description == null || description.trim().isEmpty()) {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Event description is required");
                out.print(jsonResponse.toString());
                return;
            }
            
            // Update event
            boolean updated = eventDAO.updateEvent(eventId, eventName, description);
            
            if (updated) {
                // Log admin action
                Integer adminId = AdminLoginServlet.getAdminId(request);
                if (adminId != null) {
                    adminDAO.logAdminAction(adminId, "Updated event ID " + eventId + ": " + eventName);
                }
                
                jsonResponse.put("status", "success");
                jsonResponse.put("message", "Event updated successfully");
            } else {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Failed to update event");
            }
            
        } catch (Exception e) {
            System.err.println("Error updating event: " + e.getMessage());
            e.printStackTrace();
            
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "An unexpected error occurred");
        } finally {
            out.print(jsonResponse.toString());
            out.flush();
        }
    }
    
    /**
     * Delete an event
     */
    private void deleteEvent(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        JSONObject jsonResponse = new JSONObject();
        
        try {
            String idParam = request.getParameter("id");
            
            if (idParam == null || idParam.trim().isEmpty()) {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Event ID is required");
                out.print(jsonResponse.toString());
                return;
            }
            
            int eventId;
            try {
                eventId = Integer.parseInt(idParam);
            } catch (NumberFormatException e) {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Invalid event ID format");
                out.print(jsonResponse.toString());
                return;
            }
            
            // Delete event
            boolean deleted = eventDAO.deleteEvent(eventId);
            
            if (deleted) {
                // Log admin action
                Integer adminId = AdminLoginServlet.getAdminId(request);
                if (adminId != null) {
                    adminDAO.logAdminAction(adminId, "Deleted event ID: " + eventId);
                }
                
                jsonResponse.put("status", "success");
                jsonResponse.put("message", "Event deleted successfully");
            } else {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Failed to delete event. Event may have registrations.");
            }
            
        } catch (Exception e) {
            System.err.println("Error deleting event: " + e.getMessage());
            e.printStackTrace();
            
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "An unexpected error occurred");
        } finally {
            out.print(jsonResponse.toString());
            out.flush();
        }
    }
}
