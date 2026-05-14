# Notices, Feedback, and Document Verification

## Responsible Member

IT25101220 - Lakshan H.M.K

## Module Overview

This module manages academic notices, student feedback, and document verification-related workflow in the Nextexam system.

## Main Features

- Create and view notices
- Manage academic announcements
- Submit student feedback
- Reply to feedback
- Track feedback status
- Support document verification workflow

## CRUD Operations

| Operation | Description |
|---|---|
| Create | Add notices and feedback records |
| Read | View notices and feedback |
| Update | Update feedback status or reply |
| Delete | Remove old notices or feedback records |

## OOP Concepts Used

| Concept | Usage |
|---|---|
| Encapsulation | Notice and Feedback details are protected in model classes |
| Abstraction | DAO layer handles file operations |
| Information Hiding | JSP pages do not directly manipulate data files |
| Modularity | Notice and feedback logic is separated into specific components |

## Related Files

- Notice.java
- Feedback.java
- NoticeDAO.java
- FeedbackDAO.java
- NoticeServlet.java
- FeedbackServlet.java
- notices/index.jsp
- feedback/index.jsp

## Testing Performed

| Test Case | Expected Result | Status |
|---|---|---|
| Add notice | Notice should be saved | Passed |
| View notices | Notices should display | Passed |
| Submit feedback | Feedback should be saved | Passed |
| Update feedback status | Status should update | Passed |
| Delete notice/feedback | Record should be removed | Passed |

## Contribution Summary

I reviewed the notices and feedback workflow, added documentation, tested CRUD actions, and documented feedback/status handling.