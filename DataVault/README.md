# EventTech - TARUNYAM Tech Event 2025

A comprehensive full-stack Java web application for managing tech event registrations, certificates, and admin workflows. Built with modern dark-themed UI featuring animations, gradients, and responsive design.

![EventTech Dashboard](https://img.shields.io/badge/Platform-Java%208-orange) ![Database](https://img.shields.io/badge/Database-MySQL%208-blue) ![Server](https://img.shields.io/badge/Server-Tomcat%208-green) ![Build](https://img.shields.io/badge/Build-Maven-red)

## 🚀 Features

### User Features
- **Event Registration**: Register for 6 tech events (Code Rush, Tech Quiz, Hackathon, Web Master, Debug Dash, AI Challenge)
- **Instant Certificates**: Auto-generated participation certificates with download functionality
- **Modern UI**: Dark theme with blue/purple gradients, animations, and glow effects
- **Responsive Design**: Optimized for desktop and mobile devices
- **Toast Notifications**: Custom animated notifications for all actions

### Admin Features
- **Dashboard**: Overview with statistics and participant management
- **Winner Selection**: Select winners and generate winner certificates
- **Event Management**: Add, edit, and delete events dynamically
- **Data Export**: Export participant data as CSV, HTML, or JSON
- **Pagination**: Efficient data browsing with filtering and sorting
- **Audit Logging**: Track all admin actions for security

### Technical Features
- **Full-Stack Java**: Servlets, JSP, JDBC with modular DAO pattern
- **JSON API**: All servlets return JSON responses for seamless frontend integration
- **Certificate Generation**: html2canvas integration for high-quality certificate downloads
- **Security**: Input validation, SQL injection prevention, session management
- **Performance**: Optimized database queries with indexes and views

## 🛠️ Tech Stack

### Backend
- **Java 8** (JDK 8) with Servlets and JSP
- **Apache Tomcat 8.0.30** for deployment
- **MySQL 8** with JDBC connectivity
- **Maven** for dependency management
- **JSON** for API responses

### Frontend
- **JSP pages** with HTML5 and CSS3
- **Vanilla JavaScript** for interactivity
- **Feather Icons** for modern iconography
- **html2canvas** for certificate downloads
- **Custom CSS** with animations and gradients

### Development Tools
- **NetBeans 8** and **Replit** compatible
- **WAR packaging** for deployment
- **Environment variables** for configuration

## 📋 Prerequisites

- Java 8 (JDK 8) or higher
- Apache Tomcat 8.0.30 or higher
- MySQL 8.0 or higher
- Maven 3.6 or higher
- Web browser with JavaScript enabled

## ⚡ Quick Start

### 1. Clone and Setup

```bash
# Clone the repository
git clone <repository-url>
cd EventTech

# Create environment variables file
cp .env.example .env
