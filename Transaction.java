package com.library.model;

import java.time.LocalDate;

/**
 * Represents a single issue/return event for one copy of a book.
 */
public class Transaction {

    public enum Status {
        ISSUED, RETURNED
    }

    private String transactionId;
    private String isbn;
    private String memberId;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDate returnDate;   // null while the book is still out
    private double fineAmount;
    private Status status;

    public Transaction(String transactionId, String isbn, String memberId,
                        LocalDate issueDate, LocalDate dueDate, LocalDate returnDate,
                        double fineAmount, Status status) {
        this.transactionId = transactionId;
        this.isbn = isbn;
        this.memberId = memberId;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.fineAmount = fineAmount;
        this.status = status;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getMemberId() {
        return memberId;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public double getFineAmount() {
        return fineAmount;
    }

    public void setFineAmount(double fineAmount) {
        this.fineAmount = fineAmount;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public boolean isOverdue(LocalDate today) {
        return status == Status.ISSUED && today.isAfter(dueDate);
    }

    public String toCsv() {
        return String.join(",",
                transactionId, isbn, memberId, issueDate.toString(), dueDate.toString(),
                returnDate == null ? "" : returnDate.toString(),
                String.valueOf(fineAmount), status.name());
    }

    public static Transaction fromCsv(String line) {
        String[] p = line.split(",", -1);
        LocalDate ret = p[5].isEmpty() ? null : LocalDate.parse(p[5]);
        return new Transaction(p[0], p[1], p[2], LocalDate.parse(p[3]), LocalDate.parse(p[4]),
                ret, Double.parseDouble(p[6]), Status.valueOf(p[7]));
    }

    @Override
    public String toString() {
        String ret = returnDate == null ? "-- not returned --" : returnDate.toString();
        return String.format("%-10s | ISBN %-12s | Member %-8s | issued %-10s | due %-10s | returned %-19s | fine Rs.%.2f | %s",
                transactionId, isbn, memberId, issueDate, dueDate, ret, fineAmount, status);
    }
}
