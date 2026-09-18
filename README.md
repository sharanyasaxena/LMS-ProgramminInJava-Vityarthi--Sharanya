# 📚 Library Management System (Java)

A console-based Library Management System built in core Java, developed as
the "Build Your Own Project" submission for the Java Programming course.

**Author:** Sharanya Saxena
**Registration No:** 24BAC10008

---

## Overview

This application replaces the manual, register-based way a small library
tracks its books and members with a menu-driven Java program. A librarian
logs in and can manage the book catalogue, register members, issue and
return books, and view live inventory reports — all backed by simple,
human-readable CSV files so no external database installation is needed.

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

## Features

- 🔐 **Secure admin login** — SHA-256 hashed credentials, limited login attempts
- 📖 **Book management** — add / update / delete / search books, tracks total vs. available copies
- 🧑‍🤝‍🧑 **Member management** — register members with email/phone validation, 3-book borrowing limit
- 🔄 **Issue & return workflow** — automatic 14-day due date and Rs. 5/day overdue fine calculation
- ⏰ **Overdue tracking** — instantly list every book that is past its due date
- 📊 **Live reporting** — inventory summary (titles, copies, members, overdue count)
- 💾 **Persistent storage** — plain CSV files under `data/`, no database server required
- 🪵 **Activity logging** — every login, issue, return and error is logged to `data/app.log`
- ✅ **Input validation & error handling** — custom checked exceptions for every failure case

## Technologies / Tools Used

| Category            | Technology                                   |
|----------------------|-----------------------------------------------|
| Language             | Java 17+ (core Java, no external libraries)   |
| Persistence          | Flat-file CSV storage (`java.io` / `java.nio`)|
| Security             | `java.security.MessageDigest` (SHA-256)       |
| Build                | `javac` (shell scripts provided)              |
| Version Control      | Git / GitHub                                  |

## Project Structure

```
LibraryManagementSystem/
├── README.md
├── statement.md
├── build.sh                     # compiles the project
├── run.sh                       # runs the compiled project
├── .gitignore
├── data/                        # CSV data files + app.log (created at runtime)
├── docs/                        # design diagrams used in the project report
└── src/com/library/
    ├── main/
    │   └── Main.java            # console UI / entry point
    ├── model/
    │   ├── Book.java
    │   ├── Member.java
    │   └── Transaction.java
    ├── service/
    │   ├── Library.java         # core business logic (3 functional modules)
    │   └── AuthService.java     # admin authentication (security)
    ├── util/
    │   ├── FileStorage.java     # CSV read/write persistence layer
    │   ├── InputValidator.java  # centralised validation rules
    │   └── Logger.java          # simple file-based logging
    └── exception/
        ├── BookNotFoundException.java
        ├── BookNotAvailableException.java
        ├── MemberNotFoundException.java
        └── DuplicateEntryException.java
```

9 meaningful source files across 4 packages, following a layered
(model / service / util / exception) architecture.

## Steps to Install & Run

### Prerequisites
- JDK 17 or later installed (`javac -version` to check)
- Git (to clone the repository)

### 1. Clone the repository
```bash
git clone <your-repo-url>
cd LibraryManagementSystem
```

### 2. Compile
```bash
./build.sh
```
This compiles every file under `src/` into a `bin/` directory.

*(Windows / manual alternative)*
```bash
mkdir bin
javac -d bin $(find src -name "*.java")
```

### 3. Run
```bash
./run.sh
```
*(or manually: `java -cp bin com.library.main.Main`)*

### 4. Log in
On first run, a default administrator account is created automatically:

```
Username: admin
Password: admin123
```

You can then use the main menu to manage books, members and transactions.
All data is saved automatically to CSV files in `data/` after every change,
so your work is preserved the next time you run the program.

## Instructions for Testing

Manual/functional test checklist (also covered in the project report,
Section 11 — Testing Approach):

1. **Login** — try an incorrect password 3 times → application exits;
   correct `admin` / `admin123` → main menu appears.
2. **Add book** — add a book with a duplicate ISBN → should show a
   duplicate-entry error; add a valid new book → confirmation message.
3. **Register member** — try an invalid email (e.g. `abc@`) or a 9-digit
   phone number → validation error; valid data → member ID assigned.
4. **Issue book** — issue a book to a member, then try issuing it again when
   `availableCopies = 0` → `BookNotAvailableException` message shown.
5. **Borrowing limit** — issue 4 books to the same member → the 4th attempt
   is rejected once the 3-book limit is reached.
6. **Return + fine** — return a book after its due date → a fine of
   Rs. 5/day overdue is calculated and shown.
7. **Overdue report** — list overdue books and confirm only books past their
   due date appear.
8. **Persistence** — close and restart the application → previously added
   books/members/transactions are still present (loaded from `data/*.csv`).
9. **Logging** — check `data/app.log` after a session to confirm logins,
   issues, returns and errors were recorded with timestamps.

## Screenshots

See the `docs/` folder and the project report PDF for the system
architecture, workflow, UML and ER diagrams, along with sample console
output.

## License

This project was created for academic submission purposes as part of a
university course assignment.
