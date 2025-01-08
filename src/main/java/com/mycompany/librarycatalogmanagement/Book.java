package com.mycompany.librarycatalogmanagement;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

class Book {
    protected int id;
    protected String title, author, summary, date;
    protected double price;

    // assign details for the book object
    public Book () {}
    
    public Book (int id, String title, String author, String summary, String date, double price) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.summary = summary;
        this.date = date;
        this.price = price;
    }
    
    public void saveIntoFile(ArrayList<Book> bookArray) {
        Path path = Paths.get(System.getProperty("user.dir"), "books.txt");
        
        try {
            Files.createDirectories(path.getParent());
            
            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                for (Book book : bookArray) {
                    String bookData = String.format(
                        "%d,%s,%s,%s,%s,%.2f",
                        book.id,
                        book.title,
                        book.author,
                        book.summary,
                        book.date,
                        book.price
                    );
                    
                    writer.write(bookData);
                    writer.newLine();
                }
            }
        }
        catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Problem writing into file", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public ArrayList<Book> readFromFile() {
        Path path = Paths.get(System.getProperty("user.dir"), "books.txt");
        ArrayList<Book> books = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 6);
                if (parts.length == 6) {
                    try {
                        int id = Integer.parseInt(parts[0].trim());
                        String title = parts[1].trim();
                        String author = parts[2].trim();
                        String summary = parts[3].trim();
                        String date = parts[4].trim();
                        double price = Double.parseDouble(parts[5].trim());
                        books.add(new Book(id, title, author, summary, date, price));
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid number format in line: " + line + " - " + e.getMessage());
                    }
                } else {
                    System.err.println("Malformed line (missing fields): " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("Failed to read file: " + e.getMessage());
        }

        return books;
    }
    
    public ArrayList<Book> searchArray(String searchValue, ArrayList<Book> bookArray) {
        bookArray = readFromFile(); // reload book array
        ArrayList<Book> filteredArray = new ArrayList<>();
        Pattern pattern = Pattern.compile(searchValue, Pattern.CASE_INSENSITIVE);
        
        for (Book book : bookArray) {
            if (pattern.matcher(String.valueOf(book.id)).find() || pattern.matcher(book.title).find() || pattern.matcher(book.author).find() || pattern.matcher(book.summary).find()) {
                filteredArray.add(book);
            }
        }
        
        return filteredArray;
    }
    
    public DefaultTableModel populateTableModel(String[] columns, User loggedUser, ArrayList<Book> bookArray) {
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        for (Book book : bookArray) {
            if (loggedUser != null) {
                switch (loggedUser.role) {
                    case "staff" -> model.addRow(new Object[] { book.id, book.title, book.author, book.summary, book.date, book.price, "Delete" });
                    case "student" -> model.addRow(new Object[] { book.id, book.title, book.author, book.summary, book.date, book.price, "Book" });
                    default -> model.addRow(new Object[] { book.id, book.title, book.author, book.summary, book.date, book.price });
                }
            } else {
                model.addRow(new Object[] { book.id, book.title, book.author, book.summary, book.date, book.price });
            }
        }

        return model;
    }
}