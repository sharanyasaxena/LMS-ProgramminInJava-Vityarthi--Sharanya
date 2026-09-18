package com.library.main;

import com.library.exception.*;
import com.library.model.Book;
import com.library.model.Member;
import com.library.model.Transaction;
import com.library.service.AuthService;
import com.library.service.Library;
import com.library.util.InputValidator;
import com.library.util.Logger;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Console entry point for the Library Management System.
 * Presents a simple text menu: the admin must log in first, then can
 * manage books, members and issue/return transactions.
 */
public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final Library library = new Library();
    private static final AuthService authService = new AuthService();

    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("   LIBRARY MANAGEMENT SYSTEM (Java)");
        System.out.println("=========================================");

        if (!login()) {
            System.out.println("Too many failed attempts. Exiting.");
            return;
        }

        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1": bookManagementMenu(); break;
                    case "2": memberManagementMenu(); break;
                    case "3": transactionMenu(); break;
                    case "4": reportsMenu(); break;
                    case "0":
                        running = false;
                        System.out.println("Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid option. Please choose again.");
                }
            } catch (Exception e) {
                // Top-level safety net: the application should never crash on bad input.
                Logger.error("Unexpected error: " + e.getMessage());
                System.out.println("Something went wrong: " + e.getMessage());
            }
        }
    }

    // ---------------------------------------------------------------
    // Authentication
    // ---------------------------------------------------------------

    private static boolean login() {
        for (int attempt = 1; attempt <= 3; attempt++) {
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();
            System.out.print("Password: ");
            String password = scanner.nextLine().trim();
            if (authService.login(username, password)) {
                System.out.println("Login successful. Welcome, " + username + "!\n");
                return true;
            }
            System.out.println("Invalid credentials. Attempt " + attempt + " of 3.\n");
        }
        return false;
    }

    // ---------------------------------------------------------------
    // Menus
    // ---------------------------------------------------------------

    private static void printMainMenu() {
        System.out.println("\n----------------- MAIN MENU -----------------");
        System.out.println("1. Book Management");
        System.out.println("2. Member Management");
        System.out.println("3. Issue / Return Books");
        System.out.println("4. Reports");
        System.out.println("0. Exit");
        System.out.print("Choose an option: ");
    }

    private static void bookManagementMenu() {
        System.out.println("\n--- Book Management ---");
        System.out.println("1. Add book");
        System.out.println("2. Update book");
        System.out.println("3. Delete book");
        System.out.println("4. Search books");
        System.out.println("5. List all books");
        System.out.println("0. Back");
        System.out.print("Choose an option: ");
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1": addBookFlow(); break;
            case "2": updateBookFlow(); break;
            case "3": deleteBookFlow(); break;
            case "4": searchBookFlow(); break;
            case "5": listAllBooksFlow(); break;
            case "0": break;
            default: System.out.println("Invalid option.");
        }
    }

    private static void memberManagementMenu() {
        System.out.println("\n--- Member Management ---");
        System.out.println("1. Register member");
        System.out.println("2. Remove member");
        System.out.println("3. List all members");
        System.out.println("4. View member's transaction history");
        System.out.println("0. Back");
        System.out.print("Choose an option: ");
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1": registerMemberFlow(); break;
            case "2": removeMemberFlow(); break;
            case "3": listAllMembersFlow(); break;
            case "4": memberHistoryFlow(); break;
            case "0": break;
            default: System.out.println("Invalid option.");
        }
    }

    private static void transactionMenu() {
        System.out.println("\n--- Issue / Return Books ---");
        System.out.println("1. Issue a book");
        System.out.println("2. Return a book");
        System.out.println("3. List overdue books");
        System.out.println("4. List all transactions");
        System.out.println("0. Back");
        System.out.print("Choose an option: ");
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1": issueBookFlow(); break;
            case "2": returnBookFlow(); break;
            case "3": overdueFlow(); break;
            case "4": allTransactionsFlow(); break;
            case "0": break;
            default: System.out.println("Invalid option.");
        }
    }

    private static void reportsMenu() {
        System.out.println("\n--- Inventory & Library Report ---");
        Map<String, Integer> summary = library.inventorySummary();
        for (Map.Entry<String, Integer> entry : summary.entrySet()) {
            System.out.printf("%-25s : %d%n", entry.getKey(), entry.getValue());
        }
    }

    // ---------------------------------------------------------------
    // Book Management flows
    // ---------------------------------------------------------------

    private static void addBookFlow() {
        System.out.print("ISBN: ");
        String isbn = scanner.nextLine().trim();
        if (!InputValidator.isValidIsbn(isbn)) {
            System.out.println("Invalid ISBN format. Use 5-20 alphanumeric characters/hyphens.");
            return;
        }
        System.out.print("Title: ");
        String title = scanner.nextLine().trim();
        System.out.print("Author: ");
        String author = scanner.nextLine().trim();
        System.out.print("Category: ");
        String category = scanner.nextLine().trim();
        System.out.print("Number of copies: ");
        String copiesStr = scanner.nextLine().trim();

        if (!InputValidator.isNonEmpty(title) || !InputValidator.isNonEmpty(author)
                || !InputValidator.isPositiveInteger(copiesStr)) {
            System.out.println("All fields are required and copies must be a positive number.");
            return;
        }
        int copies = Integer.parseInt(copiesStr);
        try {
            library.addBook(new Book(isbn, title, author, category, copies, copies));
            System.out.println("Book added successfully.");
        } catch (DuplicateEntryException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void updateBookFlow() {
        System.out.print("ISBN of book to update: ");
        String isbn = scanner.nextLine().trim();
        try {
            Book existing = library.getBook(isbn);
            System.out.println("Current details: " + existing);
            System.out.print("New title [" + existing.getTitle() + "]: ");
            String title = orDefault(scanner.nextLine().trim(), existing.getTitle());
            System.out.print("New author [" + existing.getAuthor() + "]: ");
            String author = orDefault(scanner.nextLine().trim(), existing.getAuthor());
            System.out.print("New category [" + existing.getCategory() + "]: ");
            String category = orDefault(scanner.nextLine().trim(), existing.getCategory());
            System.out.print("New total copies [" + existing.getTotalCopies() + "]: ");
            String copiesStr = scanner.nextLine().trim();
            int copies = copiesStr.isEmpty() ? existing.getTotalCopies() : Integer.parseInt(copiesStr);

            library.updateBook(isbn, title, author, category, copies);
            System.out.println("Book updated successfully.");
        } catch (BookNotFoundException | IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void deleteBookFlow() {
        System.out.print("ISBN of book to delete: ");
        String isbn = scanner.nextLine().trim();
        try {
            library.deleteBook(isbn);
            System.out.println("Book deleted successfully.");
        } catch (BookNotFoundException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void searchBookFlow() {
        System.out.print("Enter keyword (title / author / category / ISBN): ");
        String keyword = scanner.nextLine().trim();
        List<Book> results = library.searchBooks(keyword);
        if (results.isEmpty()) {
            System.out.println("No matching books found.");
        } else {
            results.forEach(System.out::println);
        }
    }

    private static void listAllBooksFlow() {
        List<Book> all = library.listAllBooks();
        if (all.isEmpty()) {
            System.out.println("No books in the catalogue yet.");
        } else {
            all.forEach(System.out::println);
        }
    }

    // ---------------------------------------------------------------
    // Member Management flows
    // ---------------------------------------------------------------

    private static void registerMemberFlow() {
        System.out.print("Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Phone (10 digits): ");
        String phone = scanner.nextLine().trim();

        if (!InputValidator.isNonEmpty(name) || !InputValidator.isValidEmail(email)
                || !InputValidator.isValidPhone(phone)) {
            System.out.println("Please provide a valid name, email and 10-digit phone number.");
            return;
        }
        Member m = library.registerMember(name, email, phone);
        System.out.println("Member registered with ID: " + m.getMemberId());
    }

    private static void removeMemberFlow() {
        System.out.print("Member ID to remove: ");
        String id = scanner.nextLine().trim();
        try {
            library.deleteMember(id);
            System.out.println("Member removed successfully.");
        } catch (MemberNotFoundException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void listAllMembersFlow() {
        List<Member> all = library.listAllMembers();
        if (all.isEmpty()) {
            System.out.println("No members registered yet.");
        } else {
            all.forEach(System.out::println);
        }
    }

    private static void memberHistoryFlow() {
        System.out.print("Member ID: ");
        String id = scanner.nextLine().trim();
        try {
            library.getMember(id); // validates existence
            List<Transaction> history = library.historyForMember(id);
            if (history.isEmpty()) {
                System.out.println("No transaction history for this member.");
            } else {
                history.forEach(System.out::println);
            }
        } catch (MemberNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // Transaction flows
    // ---------------------------------------------------------------

    private static void issueBookFlow() {
        System.out.print("ISBN: ");
        String isbn = scanner.nextLine().trim();
        System.out.print("Member ID: ");
        String memberId = scanner.nextLine().trim();
        try {
            Transaction txn = library.issueBook(isbn, memberId);
            System.out.println("Book issued. Transaction ID: " + txn.getTransactionId()
                    + " | Due date: " + txn.getDueDate());
        } catch (BookNotFoundException | BookNotAvailableException
                | MemberNotFoundException | DuplicateEntryException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void returnBookFlow() {
        System.out.print("Transaction ID: ");
        String txnId = scanner.nextLine().trim();
        try {
            Transaction txn = library.returnBook(txnId);
            if (txn.getFineAmount() > 0) {
                System.out.printf("Book returned. Fine due: Rs.%.2f (returned %d day(s) late)%n",
                        txn.getFineAmount(), (long) (txn.getFineAmount() / 5.0));
            } else {
                System.out.println("Book returned on time. No fine due.");
            }
        } catch (BookNotFoundException | MemberNotFoundException | IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void overdueFlow() {
        List<Transaction> overdue = library.listOverdueTransactions();
        if (overdue.isEmpty()) {
            System.out.println("No overdue books. Great job!");
        } else {
            overdue.forEach(System.out::println);
        }
    }

    private static void allTransactionsFlow() {
        List<Transaction> all = library.listAllTransactions();
        if (all.isEmpty()) {
            System.out.println("No transactions recorded yet.");
        } else {
            all.forEach(System.out::println);
        }
    }

    // ---------------------------------------------------------------
    // Small helpers
    // ---------------------------------------------------------------

    private static String orDefault(String input, String fallback) {
        return input.isEmpty() ? fallback : input;
    }
}
