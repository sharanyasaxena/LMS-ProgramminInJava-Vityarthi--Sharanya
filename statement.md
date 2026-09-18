# Problem Statement

## Problem Statement

College and school libraries with a small collection are still very often run
using a physical register: books are logged in a notebook, members sign their
name when they borrow a book, and fines are calculated by hand from memory.
This approach is slow, error-prone, and makes it almost impossible to answer
simple questions quickly — "How many copies of this book are available right
now?", "Which books are overdue?", "How much fine does this member owe?".

The **Library Management System** is a Java console application that digitises
this workflow. It lets a librarian manage the book catalogue, register and
track members, and issue/return books, while the system automatically tracks
availability, due dates, and overdue fines.

## Scope of the Project

In scope:
- Single-librarian (admin) desktop/console application
- Book catalogue management (add, update, delete, search)
- Member registration and management
- Issuing and returning books with automatic due-date and fine calculation
- Overdue tracking and basic inventory reporting
- Persistent storage using CSV files (no external database server required)

Out of scope (possible future work, see report Section 14):
- Multi-user concurrent access / networked client-server mode
- A graphical or web-based user interface
- Email/SMS notifications for due dates
- Barcode/RFID scanning integration

## Target Users

- **Librarian / Administrator** — the primary user, who logs in to manage the
  catalogue, register members, and process issue/return transactions.
- **Library Members** (students/staff) — indirectly served: their records are
  maintained by the librarian, and this project can be extended with a
  self-service member view in the future.

## High-Level Features

1. **Book Management** — add, update, delete and search books; each book
   tracks total vs. available copies.
2. **Member Management** — register members with validated email/phone,
   enforce a maximum of 3 books borrowed per member, and view a member's
   complete borrowing history.
3. **Transaction Management** — issue a book (14-day loan period), return a
   book (automatic fine calculation at Rs. 5/day overdue), and list all
   currently overdue transactions.
4. **Reporting** — a live inventory and activity summary (total titles,
   copies available/issued, registered members, overdue count).
5. **Security** — admin login with SHA-256 hashed passwords instead of plain
   text, with a limited number of login attempts.
6. **Logging** — every significant action (login, issue, return, errors) is
   appended to `data/app.log` for auditing and troubleshooting.

---
Submitted by: **Sharanya Saxena** | Registration No: **24BAC10008**
