# EventTech - TARUNYAM Tech Event 2025

## Overview

EventTech is a comprehensive full-stack Java web application designed for managing tech event registrations, certificate generation, and administrative workflows for the TARUNYAM Tech Event 2025. The platform allows users to register for various tech events (Code Rush, Tech Quiz, Hackathon, Web Master, Debug Dash, AI Challenge) and automatically generates downloadable participation certificates. It features a modern dark-themed UI with animations and gradients, while providing administrators with comprehensive dashboard capabilities for participant management, winner selection, and data export functionality.

## User Preferences

Preferred communication style: Simple, everyday language.

## System Architecture

### Frontend Architecture
- **Technology Stack**: JSP pages with HTML5, CSS3, and Vanilla JavaScript
- **UI Design**: Dark theme with blue/purple gradients, glow effects, and custom animations
- **Component Structure**: Modular CSS with CSS variables for consistent theming
- **State Management**: Client-side JavaScript for modal handling, form validation, and dynamic content
- **User Experience**: Toast notifications, responsive design, and keyboard shortcuts for enhanced usability

### Backend Architecture
- **Framework**: Java 8 with Servlet API and JSP technology
- **Design Pattern**: Model-View-Controller (MVC) with Data Access Object (DAO) pattern
- **API Structure**: RESTful servlets returning JSON responses for frontend consumption
- **Key Servlets**:
  - RegisterServlet: Handles user registration
  - AdminLoginServlet: Manages admin authentication
  - EventSettingsServlet: Manages event CRUD operations
- **Data Layer**: JDBC-based database connectivity with DAO classes for abstraction

### Data Storage
- **Database**: MySQL 8 for persistent data storage
- **Connection Management**: JDBC with connection pooling
- **Data Models**: 
  - User registration data (name, email, college, event)
  - Event information and settings
  - Admin authentication and audit logs
- **Performance Optimization**: Database indexes and views for efficient querying

### Security Architecture
- **Authentication**: Session-based admin authentication system
- **Input Validation**: Server-side validation to prevent SQL injection and XSS attacks
- **Session Management**: Secure session handling for admin functionality
- **Audit Logging**: Comprehensive logging of admin actions for security monitoring

### Certificate Generation System
- **Technology**: html2canvas library for client-side certificate rendering
- **Design**: Custom certificate templates with animated gradients and shimmer effects
- **Types**: Participation certificates (auto-generated) and winner certificates (admin-generated)
- **Download**: JPG format export functionality

## External Dependencies

### Core Dependencies
- **Apache Tomcat 8.0.30**: Application server for deployment
- **MySQL 8**: Primary database system
- **Maven**: Build tool and dependency management
- **Java 8 JDK**: Runtime environment

### Frontend Libraries
- **html2canvas**: Client-side certificate generation and image export
- **Feather Icons**: Modern iconography system
- **Google Fonts (Poppins)**: Typography enhancement

### Development Tools
- **NetBeans 8**: Primary IDE for development
- **Replit**: Cloud-based development environment support
- **WAR packaging**: Standard Java web application deployment format

### Database Connectivity
- **JDBC Driver**: MySQL Connector/J for database communication
- **Connection Pooling**: Efficient database connection management