# Lecturer and Module Management

## Responsible Member

IT25101661 - Handapangoda H.G.S.B

## Module Overview

This module supports academic staff and module-related management in the Nextexam system. It allows the system to organize lecturers, faculties, and academic modules for exam management.

## Main Features

- Manage lecturer/faculty-related records
- View academic module information
- Support lecturer-module organization
- Provide academic structure for exams and results

## CRUD Operations

| Operation | Description |
|---|---|
| Create | Add lecturer or academic records |
| Read | View lecturer/module information |
| Update | Modify lecturer/module details |
| Delete | Remove inactive or incorrect records |

## OOP Concepts Used

| Concept | Usage |
|---|---|
| Encapsulation | Lecturer/faculty details are protected using private fields |
| Abstraction | DAO classes handle file operations separately |
| Information Hiding | Data storage logic is hidden from JSP pages |
| Modularity | Lecturer and module-related logic is separated from other components |

## Related Files

- Faculty.java
- FacultyDAO.java
- FacultyServlet.java
- faculties/index.jsp

## Testing Performed

| Test Case | Expected Result | Status |
|---|---|---|
| Add academic record | Record should be saved | Passed |
| View records | Records should display | Passed |
| Update record | Updated details should save | Passed |
| Delete record | Record should be removed | Passed |

## Contribution Summary

I reviewed the lecturer/module management area, added documentation, checked CRUD behavior, and improved academic management explanation.