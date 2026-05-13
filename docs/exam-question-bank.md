# Exam Management and Question Bank

## Responsible Member

IT25100107 - Abeygunawardhana S N J

## Module Overview

The Exam Management and Question Bank module allows lecturers/admin users to create exams and manage exam questions. It supports both MCQ and essay-style questions.

## Main Features

- Create exams
- View exam list
- Update exam details
- Delete/cancel exams
- Add MCQ questions
- Add essay questions
- Manage question status

## CRUD Operations

| Operation | Description |
|---|---|
| Create | Add new exams and questions |
| Read | View exam and question details |
| Update | Modify exam and question information |
| Delete | Remove exams or questions |

## OOP Concepts Used

| Concept | Usage |
|---|---|
| Encapsulation | Exam and Question data are managed through model classes |
| Polymorphism | MCQ and essay questions can behave differently during marking |
| Abstraction | DAO classes hide file handling logic |
| Modularity | Exam and question logic is separated into specific classes |

## Related Files

- Exam.java
- Question.java
- ExamDAO.java
- QuestionDAO.java
- ExamServlet.java
- QuestionServlet.java
- exams/index.jsp
- questions/index.jsp

## Testing Performed

| Test Case | Expected Result | Status |
|---|---|---|
| Create exam | Exam should be saved | Passed |
| View exams | Exam list should display | Passed |
| Add question | Question should be saved | Passed |
| Update question | Question should update | Passed |
| Delete question | Question should be removed | Passed |

## Contribution Summary

I reviewed exam management and question bank functionality, added documentation, checked CRUD operations, and documented MCQ/essay handling.