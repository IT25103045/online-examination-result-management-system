# Project Lead Contribution - Nextexam

## Responsible Member

**IT25103045 - De Silva H.L.D.C.P.C**

## Role

Project Lead, Main Developer, Authentication Developer, Dashboard Integration, GitHub Repository Manager, Final System Integrator

## Contribution Overview

I contributed to the main development and integration of the Nextexam online examination and academic management system. My responsibilities included setting up the project structure, implementing the main authentication flow, managing role-based access, integrating dashboards, coordinating GitHub workflow, and preparing the project for final submission.

## Main Contribution Areas

### 1. Project Setup and Structure

I organized the project using a Java web application structure with separate packages for models, DAOs, servlets, filters, utilities, and JSP pages.

Main folders:

- `src/main/java/lk/nextexam/model`
- `src/main/java/lk/nextexam/dao`
- `src/main/java/lk/nextexam/servlet`
- `src/main/java/lk/nextexam/filter`
- `src/main/webapp`
- `data`
- `docs`

### 2. Authentication and Role-Based Access

I worked on the login, logout, session handling, and role-based access flow. The system supports different user roles such as Admin, Lecturer, and Student.

Main related files:

- `LoginServlet.java`
- `LogoutServlet.java`
- `AuthFilter.java`
- `User.java`
- `UserDAO.java`
- `login.jsp`

### 3. Dashboard and Navigation Integration

I reviewed and integrated the main dashboard layout, top navigation, sidebar navigation, and role-based dashboard sections.

Main related files:

- `dashboard.jsp`
- `sidebar.jsp`
- `topbar.jsp`
- `head.jsp`
- `style.css`
- `app.js`

### 4. GitHub Repository Management

I initialized the Git repository, configured the academic Git identity, pushed the project to the academic GitHub repository, created a branch-based workflow, and prepared the repository for team collaboration.

GitHub workflow used:

- Main branch for stable code
- Feature branches for individual member contributions
- Pull requests for review and merging
- Clear commit messages for contribution tracking

### 5. Final Integration

I am responsible for reviewing all member contributions, merging approved pull requests, checking project consistency, and preparing the final version for demonstration and viva.

## OOP Concepts Related to My Contribution

| OOP Concept | Usage |
|---|---|
| Encapsulation | User data is managed using private fields and getter/setter methods |
| Abstraction | DAO classes hide file handling logic from servlets and JSP pages |
| Information Hiding | Authentication logic is handled in servlets and filters, not directly in JSP pages |
| Modularity | Login, dashboard, user management, and role access are separated into different classes |

## Testing Performed

| Test Case | Expected Result | Status |
|---|---|---|
| Admin login | Admin dashboard should open | Passed |
| Lecturer login | Lecturer dashboard should open | Passed |
| Student login | Student dashboard should open | Passed |
| Invalid login | Error message should display | Passed |
| Logout | Session should be destroyed | Passed |
| Protected route access | Unauthorized users should be redirected | Passed |
| Dashboard navigation | Correct links should appear according to role | Passed |

## Contribution Summary

As project lead, I developed and integrated the main system structure, authentication flow, dashboard layout, role-based access, GitHub setup, and final project coordination.