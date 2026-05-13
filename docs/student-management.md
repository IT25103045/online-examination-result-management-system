# Student Management Module

## Responsible Member

IT25200142 - Kavya W.L.Y

## Module Overview

The Student Management module allows the admin to manage student records in the Nextexam system. It supports adding, viewing, updating, and deleting student information.

## Main Features

- Add new student records
- View student list
- Update student details
- Delete student records
- Manage student academic details

## CRUD Operations

| Operation | Description |
|---|---|
| Create | Add a new student record |
| Read | View student details and student list |
| Update | Modify student personal and academic details |
| Delete | Remove student records from the system |

## OOP Concepts Used

| Concept | Usage |
|---|---|
| Encapsulation | Student attributes are stored using private fields with getters and setters |
| Inheritance | Student records are connected with user role-based access |
| Abstraction | DAO layer hides file read/write operations |
| Information Hiding | JSP pages do not directly access text files |

## Related Files

- Student.java
- StudentDAO.java
- StudentServlet.java
- students/index.jsp

## Testing Performed

| Test Case | Expected Result | Status |
|---|---|---|
| Add student | New student should be saved | Passed |
| View students | Student list should display | Passed |
| Update student | Student details should update | Passed |
| Delete student | Student record should be removed | Passed |

## Contribution Summary

I reviewed the student management module, added documentation, checked CRUD operations, and improved student-related interface text.

