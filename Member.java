package com.library.model;

import java.time.LocalDate;

/**
 * Represents a library member who can borrow books.
 */
public class Member {

    private String memberId;
    private String name;
    private String email;
    private String phone;
    private LocalDate joinDate;
    private int booksCurrentlyIssued;

    public Member(String memberId, String name, String email, String phone,
                  LocalDate joinDate, int booksCurrentlyIssued) {
        this.memberId = memberId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.joinDate = joinDate;
        this.booksCurrentlyIssued = booksCurrentlyIssued;
    }

    public String getMemberId() {
        return memberId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDate getJoinDate() {
        return joinDate;
    }

    public int getBooksCurrentlyIssued() {
        return booksCurrentlyIssued;
    }

    public void incrementIssuedCount() {
        booksCurrentlyIssued++;
    }

    public void decrementIssuedCount() {
        if (booksCurrentlyIssued > 0) {
            booksCurrentlyIssued--;
        }
    }

    /** Business rule: a member may not hold more than 3 books at once. */
    public boolean canBorrowMore() {
        return booksCurrentlyIssued < 3;
    }

    public String toCsv() {
        return String.join(",",
                memberId, sanitize(name), sanitize(email), sanitize(phone),
                joinDate.toString(), String.valueOf(booksCurrentlyIssued));
    }

    public static Member fromCsv(String line) {
        String[] p = line.split(",", -1);
        return new Member(p[0], p[1], p[2], p[3], LocalDate.parse(p[4]),
                Integer.parseInt(p[5]));
    }

    private static String sanitize(String value) {
        return value == null ? "" : value.replace(",", ";");
    }

    @Override
    public String toString() {
        return String.format("%-8s | %-25s | %-25s | %-12s | joined %-10s | %d book(s) issued",
                memberId, name, email, phone, joinDate, booksCurrentlyIssued);
    }
}
