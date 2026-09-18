package com.library.model;

/**
 * Represents a single book title in the library catalogue.
 * A Book keeps track of how many total copies the library owns
 * and how many of those copies are currently available for issue.
 */
public class Book {

    private String isbn;
    private String title;
    private String author;
    private String category;
    private int totalCopies;
    private int availableCopies;

    public Book(String isbn, String title, String author, String category,
                int totalCopies, int availableCopies) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.category = category;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getTotalCopies() {
        return totalCopies;
    }

    public void setTotalCopies(int totalCopies) {
        this.totalCopies = totalCopies;
    }

    public int getAvailableCopies() {
        return availableCopies;
    }

    public void setAvailableCopies(int availableCopies) {
        this.availableCopies = availableCopies;
    }

    public boolean isAvailable() {
        return availableCopies > 0;
    }

    public void decrementAvailable() {
        if (availableCopies > 0) {
            availableCopies--;
        }
    }

    public void incrementAvailable() {
        if (availableCopies < totalCopies) {
            availableCopies++;
        }
    }

    /**
     * Serialises this book into a single CSV row for persistence.
     * Commas inside free-text fields are replaced with semicolons so the
     * simple CSV format is never broken by user input.
     */
    public String toCsv() {
        return String.join(",",
                isbn,
                sanitize(title),
                sanitize(author),
                sanitize(category),
                String.valueOf(totalCopies),
                String.valueOf(availableCopies));
    }

    public static Book fromCsv(String line) {
        String[] p = line.split(",", -1);
        return new Book(p[0], p[1], p[2], p[3],
                Integer.parseInt(p[4]), Integer.parseInt(p[5]));
    }

    private static String sanitize(String value) {
        return value == null ? "" : value.replace(",", ";");
    }

    @Override
    public String toString() {
        return String.format("%-12s | %-30s | %-20s | %-12s | %3d/%3d copies available",
                isbn, title, author, category, availableCopies, totalCopies);
    }
}
