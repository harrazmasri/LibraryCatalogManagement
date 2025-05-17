package com.mycompany.librarycatalogmanagement;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class Booking {
    public String borrower, bookTitle, borrowedDate;
    public int bookId;
    
    public Booking() {}
    
    public Booking(String borrower, int bookId, String bookTitle, String borrowedDate) {
        this.borrower = borrower;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.borrowedDate = borrowedDate;
    }
    
    public void createBooking(Booking booking) {
        Path path = Paths.get(System.getProperty("user.dir"), "booking.txt");

        try {
            Files.createDirectories(path.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                // update book as borrowed
                Book bookObject = new Book();
                bookObject.updateBook(booking.bookId, true);
                
                // save book into booking
                String bookingData = String.format(
                    "%s,%d,%s,%s",
                    booking.borrower,
                    booking.bookId,
                    booking.bookTitle,
                    booking.borrowedDate
                );
                writer.write(bookingData);
                writer.newLine();
                
                JOptionPane.showMessageDialog(null, "You have booked '"+booking.bookTitle+"'");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Problem writing into file", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void saveIntoFile(ArrayList<Booking> bookingArray) {
        Path path = Paths.get(System.getProperty("user.dir"), "booking.txt");
        
        try {
            Files.createDirectories(path.getParent());
            
            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                for (Booking booking : bookingArray) {
                    String bookingData = String.format(
                        "%s,%d,%s,%s",
                        booking.borrower,
                        booking.bookId,
                        booking.bookTitle,
                        booking.borrowedDate
                    );
                    
                    writer.write(bookingData);
                    writer.newLine();
                }
            }
        }
        catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Problem writing into file", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public ArrayList<Booking> getBooking(String username) {
        Path path = Paths.get(System.getProperty("user.dir"), "booking.txt");
        ArrayList<Booking> bookings = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 4);
                if (parts.length == 4) {
                    try {
                        String borrower = parts[0].trim();
                        int bookId = Integer.parseInt(parts[1].trim());
                        String bookTitle = parts[2].trim();
                        String borrowedDate = parts[3].trim();
                        
                        if (username != null && username.equals(borrower) || username == null) {
                            bookings.add(new Booking(borrower, bookId, bookTitle, borrowedDate));
                        }
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

        return bookings;
    }
    
    public ArrayList<Booking> searchArray(String searchValue, ArrayList<Booking> bookingArray, String username) {
        bookingArray = getBooking(username);
        ArrayList<Booking> filteredArray = new ArrayList<>();
        Pattern pattern = Pattern.compile(searchValue, Pattern.CASE_INSENSITIVE);
        
        for (Booking booking : bookingArray) {
            if (pattern.matcher(String.valueOf(booking.bookId)).find() || pattern.matcher(booking.bookTitle).find() || pattern.matcher(booking.borrower).find() || pattern.matcher(booking.borrowedDate).find()) {
                filteredArray.add(booking);
            }
        }
        
        return filteredArray;
    }
    
    public DefaultTableModel populateTableModel(String[] columns, User loggedUser, ArrayList<Booking> bookingArray) {
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        for (Booking booking : bookingArray) {
            if (loggedUser != null) {
                switch (loggedUser.role) {
                    case "staff" -> model.addRow(new Object[] { 
                        booking.bookId, booking.bookTitle, booking.borrower, booking.borrowedDate, "Returned" 
                    });
                    case "student" -> model.addRow(new Object[] { 
                        booking.bookId, booking.bookTitle, booking.borrower, booking.borrowedDate 
                    });
                    default -> model.addRow(new Object[] { 
                        booking.bookId, booking.bookTitle, booking.borrower, booking.borrowedDate 
                    });
                }
            } else {
                model.addRow(new Object[] { 
                    booking.bookId, booking.bookTitle, booking.borrower, booking.borrowedDate 
                });
            }
        }

        return model;
    }
}
