package com.mycompany.librarycatalogmanagement;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import javax.swing.JOptionPane;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class User {
    public String username;
    public String password;
    public String role;
    
    public User() {
        //
    }
    
    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }
    
    private String hashPassword(String password) throws NoSuchAlgorithmException {
        // Use SHA-256 to hash the password
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashedBytes = digest.digest(password.getBytes());

        // Convert the hashed bytes to a Base64 string for readability
        return Base64.getEncoder().encodeToString(hashedBytes);
    }
    
    public void registerUser(String inputUsername, String inputPassword, String inputRole) {
        Path path = Paths.get(System.getProperty("user.dir"), "users.txt");

        try {
            Files.createDirectories(path.getParent()); // Ensure parent directories exist
            // Open the file in append mode
            try (BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                String hashedPassword = hashPassword(inputPassword); // Replace with actual hashing if required
                String userData = String.format(
                    "%s,%s,%s",
                    inputUsername,
                    hashedPassword,
                    inputRole.toLowerCase()
                );
                writer.write(userData);
                writer.newLine(); // Add a new line for each user
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Problem writing into file", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (NoSuchAlgorithmException e) {
            JOptionPane.showMessageDialog(
                null,
                "Error hashing password: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    public User validateUser(String inputUsername, String inputPassword) {
        Path path = Paths.get(System.getProperty("user.dir"), "users.txt");

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;

            while ((line = reader.readLine()) != null) {
                // Split the line into parts (username, password, role)
                String[] parts = line.split(",", 3);
                if (parts.length < 3) continue; // Skip invalid lines

                String username = parts[0].trim();
                String password = parts[1].trim();
                String role = parts[2].trim();

                // Hash the entered password
                String hashedEnteredPassword = hashPassword(inputPassword);

                // Compare username and password
                if (username.equals(inputUsername) && password.equals(hashedEnteredPassword)) {
                    // Return a new User object on successful validation
                    return new User(username, password, role);
                }
            }
        } catch (IOException e) {
            System.out.println("Failed to read file: " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            JOptionPane.showMessageDialog(
                null,
                "Error hashing password: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }

        return null; // Return null if user not found or invalid credentials
    }

}
