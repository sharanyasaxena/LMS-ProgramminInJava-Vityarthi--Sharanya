package com.library.service;

import com.library.exception.*;
import com.library.model.Book;
import com.library.model.Member;
import com.library.model.Transaction;
import com.library.util.FileStorage;
import com.library.util.Logger;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The Library class is the heart of the application. It owns the in-memory
 * catalogue of books, the member registry and the transaction ledger, and
 * exposes the operations the console UI (Main) calls into. It also takes
 * care of loading data from disk at start-up and persisting every change
 * back to disk immediately (a simple but reliable "write-through" strategy).
 */
public class Library {

    private static final String DATA_DIR = "data";
    private static final String BOOKS_FILE = DATA_DIR + "/books.csv";
    private static final String MEMBERS_FILE = DATA_DIR + "/members.csv";
    private static final String TRANSACTIONS_FILE = DATA_DIR + "/transactions.csv";

    private static final int LOAN_PERIOD_DAYS = 14;
    private static final double FINE_PER_DAY = 5.0; // rupees per day overdue

    private final Map<String, Book> books = new LinkedHashMap<>();
    private final Map<String, Member> members = new LinkedHashMap<>();
    private final List<Transaction> transactions = new ArrayList<>();

    private int memberSequence = 1;
    private int transactionSequence = 1;

    public Library() {
        FileStorage.ensureDataDirectory(DATA_DIR);
        loadAll();
    }

    // ---------------------------------------------------------------
    // Persistence
    // ---------------------------------------------------------------

    private void loadAll() {
        for (String line : FileStorage.readLines(BOOKS_FILE)) {
            Book b = Book.fromCsv(line);
            books.put(b.getIsbn(), b);
        }
        for (String line : FileStorage.readLines(MEMBERS_FILE)) {
            Member m = Member.fromCsv(line);
            members.put(m.getMemberId(), m);
            int num = extractNumber(m.getMemberId());
            if (num >= memberSequence) {
                memberSequence = num + 1;
            }
        }
        for (String line : FileStorage.readLines(TRANSACTIONS_FILE)) {
            Transaction t = Transaction.fromCsv(line);
            transactions.add(t);
            int num = extractNumber(t.getTransactionId());
            if (num >= transactionSequence) {
                transactionSequence = num + 1;
            }
        }
        Logger.info(String.format("Loaded %d books, %d members, %d transactions from disk",
                books.size(), members.size(), transactions.size()));
    }

    private int extractNumber(String id) {
        String digits = id.replaceAll("[^0-9]", "");
        return digits.isEmpty() ? 0 : Integer.parseInt(digits);
    }

    private void saveBooks() {
        FileStorage.writeLines(BOOKS_FILE,
                books.values().stream().map(Book::toCsv).collect(Collectors.toList()));
    }

    private void saveMembers() {
        FileStorage.writeLines(MEMBERS_FILE,
                members.values().stream().map(Member::toCsv).collect(Collectors.toList()));
    }

    private void saveTransactions() {
        FileStorage.writeLines(TRANSACTIONS_FILE,
                transactions.stream().map(Transaction::toCsv).collect(Collectors.toList()));
    }

    // ---------------------------------------------------------------
    // Module 1: Book Management (CRUD)
    // ---------------------------------------------------------------

    public void addBook(Book book) throws DuplicateEntryException {
        if (books.containsKey(book.getIsbn())) {
            throw new DuplicateEntryException("A book with ISBN " + book.getIsbn() + " already exists.");
        }
        books.put(book.getIsbn(), book);
        saveBooks();
        Logger.info("Book added: " + book.getIsbn() + " - " + book.getTitle());
    }

    public Book getBook(String isbn) throws BookNotFoundException {
        Book b = books.get(isbn);
        if (b == null) {
            throw new BookNotFoundException("No book found with ISBN " + isbn);
        }
        return b;
    }

    public void updateBook(String isbn, String title, String author, String category,
                            int totalCopies) throws BookNotFoundException {
        Book b = getBook(isbn);
        int issuedCopies = b.getTotalCopies() - b.getAvailableCopies();
        if (totalCopies < issuedCopies) {
            throw new IllegalArgumentException(
                    "Total copies cannot be less than the " + issuedCopies + " copies currently issued.");
        }
        b.setTitle(title);
        b.setAuthor(author);
        b.setCategory(category);
        int delta = totalCopies - b.getTotalCopies();
        b.setTotalCopies(totalCopies);
        b.setAvailableCopies(b.getAvailableCopies() + delta);
        saveBooks();
        Logger.info("Book updated: " + isbn);
    }

    public void deleteBook(String isbn) throws BookNotFoundException {
        Book b = getBook(isbn);
        if (b.getAvailableCopies() != b.getTotalCopies()) {
            throw new IllegalStateException("Cannot delete a book that currently has copies issued out.");
        }
        books.remove(isbn);
        saveBooks();
        Logger.info("Book deleted: " + isbn);
    }

    public List<Book> searchBooks(String keyword) {
        String k = keyword.toLowerCase();
        return books.values().stream()
                .filter(b -> b.getTitle().toLowerCase().contains(k)
                        || b.getAuthor().toLowerCase().contains(k)
                        || b.getIsbn().toLowerCase().contains(k)
                        || b.getCategory().toLowerCase().contains(k))
                .collect(Collectors.toList());
    }

    public List<Book> listAllBooks() {
        return new ArrayList<>(books.values());
    }

    // ---------------------------------------------------------------
    // Module 2: Member Management
    // ---------------------------------------------------------------

    public Member registerMember(String name, String email, String phone) {
        String id = "M" + String.format("%03d", memberSequence++);
        Member m = new Member(id, name, email, phone, LocalDate.now(), 0);
        members.put(id, m);
        saveMembers();
        Logger.info("Member registered: " + id + " - " + name);
        return m;
    }

    public Member getMember(String memberId) throws MemberNotFoundException {
        Member m = members.get(memberId);
        if (m == null) {
            throw new MemberNotFoundException("No member found with ID " + memberId);
        }
        return m;
    }

    public void deleteMember(String memberId) throws MemberNotFoundException {
        Member m = getMember(memberId);
        if (m.getBooksCurrentlyIssued() > 0) {
            throw new IllegalStateException("Cannot remove a member who still has books issued.");
        }
        members.remove(memberId);
        saveMembers();
        Logger.info("Member removed: " + memberId);
    }

    public List<Member> listAllMembers() {
        return new ArrayList<>(members.values());
    }

    // ---------------------------------------------------------------
    // Module 3: Transaction Management (issue / return / fines)
    // ---------------------------------------------------------------

    public Transaction issueBook(String isbn, String memberId)
            throws BookNotFoundException, BookNotAvailableException, MemberNotFoundException, DuplicateEntryException {
        Book book = getBook(isbn);
        Member member = getMember(memberId);

        if (!book.isAvailable()) {
            throw new BookNotAvailableException("All copies of \"" + book.getTitle() + "\" are currently issued out.");
        }
        if (!member.canBorrowMore()) {
            throw new DuplicateEntryException(member.getName() + " has already reached the 3-book borrowing limit.");
        }

        String txnId = "T" + String.format("%04d", transactionSequence++);
        LocalDate issueDate = LocalDate.now();
        LocalDate dueDate = issueDate.plusDays(LOAN_PERIOD_DAYS);
        Transaction txn = new Transaction(txnId, isbn, memberId, issueDate, dueDate, null, 0.0, Transaction.Status.ISSUED);

        book.decrementAvailable();
        member.incrementIssuedCount();
        transactions.add(txn);

        saveBooks();
        saveMembers();
        saveTransactions();
        Logger.info("Book issued: " + isbn + " to " + memberId + " (txn " + txnId + ")");
        return txn;
    }

    public Transaction returnBook(String transactionId) throws BookNotFoundException, MemberNotFoundException {
        Transaction txn = transactions.stream()
                .filter(t -> t.getTransactionId().equals(transactionId) && t.getStatus() == Transaction.Status.ISSUED)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No active (unreturned) transaction found with ID " + transactionId));

        Book book = getBook(txn.getIsbn());
        Member member = getMember(txn.getMemberId());

        LocalDate today = LocalDate.now();
        long overdueDays = Math.max(0, ChronoUnit.DAYS.between(txn.getDueDate(), today));
        double fine = overdueDays * FINE_PER_DAY;

        txn.setReturnDate(today);
        txn.setFineAmount(fine);
        txn.setStatus(Transaction.Status.RETURNED);

        book.incrementAvailable();
        member.decrementIssuedCount();

        saveBooks();
        saveMembers();
        saveTransactions();
        Logger.info("Book returned: txn " + transactionId + " (fine Rs." + fine + ")");
        return txn;
    }

    public List<Transaction> listOverdueTransactions() {
        LocalDate today = LocalDate.now();
        return transactions.stream()
                .filter(t -> t.isOverdue(today))
                .collect(Collectors.toList());
    }

    public List<Transaction> listAllTransactions() {
        return new ArrayList<>(transactions);
    }

    public List<Transaction> historyForMember(String memberId) {
        return transactions.stream()
                .filter(t -> t.getMemberId().equals(memberId))
                .collect(Collectors.toList());
    }

    // ---------------------------------------------------------------
    // Reporting
    // ---------------------------------------------------------------

    public Map<String, Integer> inventorySummary() {
        Map<String, Integer> summary = new LinkedHashMap<>();
        summary.put("Total titles", books.size());
        summary.put("Total copies", books.values().stream().mapToInt(Book::getTotalCopies).sum());
        summary.put("Copies available", books.values().stream().mapToInt(Book::getAvailableCopies).sum());
        summary.put("Copies issued out", books.values().stream()
                .mapToInt(b -> b.getTotalCopies() - b.getAvailableCopies()).sum());
        summary.put("Registered members", members.size());
        summary.put("Overdue transactions", listOverdueTransactions().size());
        return summary;
    }
}
