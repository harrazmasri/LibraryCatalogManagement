package com.mycompany.librarycatalogmanagement;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public class LibraryCatalogManagement {
    @SuppressWarnings("unused")
    private static final Book bookObject = new Book();
    private static final Booking bookingObject = new Booking();
    private static final String dates[]
        = { "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "10",
            "11", "12", "13", "14", "15",
            "16", "17", "18", "19", "20",
            "21", "22", "23", "24", "25",
            "26", "27", "28", "29", "30",
            "31" };
    private static final String months[]
        = { "Jan", "Feb", "Mar", "Apr",
            "May", "Jun", "July", "Aug",
            "Sep", "Oct", "Nov", "Dec" };
    private static final String years[]
        = { "1995", "1996", "1997", "1998",
            "1999", "2000", "2001", "2002",
            "2003", "2004", "2005", "2006",
            "2007", "2008", "2009", "2010",
            "2011", "2012", "2013", "2014",
            "2015", "2016", "2017", "2018",
            "2019", "2020", "2021", "2022",
            "2023", "2024"};
    
    private static ArrayList<Book> bookArray = new ArrayList<>(Arrays.asList(
        new Book(
            101,
            "The Surprise And Wonder Of Skyscrapers",
            "David Martinez",
            "This book uncovers the secrets and complex engineering measures took to build a skyscraper.",
            "20-08-2010",
            23.25
        ),
        new Book(
            102,
            "Fallen Grace",
            "July Haleem",
            "A story of a unbeknowned kid born out of poverty rising up into dictatorship.",
            "20-08-2010",
            89.90
        )
    ));
    
    private static JFrame mainFrame;    
    private static int bookListIndex; // used for referencing which book in array to update book frame
    
    private static ArrayList<Booking> bookingArray = new ArrayList<>(Arrays.asList(
        new Booking(
            "Halim",
            101,
            "Terselamat Daripadanya",
            "30-09-2010"
        )
    ));
    
    private static User loggedUser = null;
    private static String currentPage = "login";

    public static Booking getBookingObject() {
        return bookingObject;
    }
    
    // Renderer for action buttons
    static class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String columnName = table.getColumnName(column);
            if (loggedUser != null && "staff".equals(loggedUser.role)) {
                if (columnName.equals("Edit")) {
                    setText("Edit");
                } else if (columnName.equals("Action")) {
                    setText("Delete");
                }
            } else {
                setText("Book");
            }
            return this;
        }
    }

    static class ButtonEditor extends DefaultCellEditor {
        private final JButton button;
        private JTable table;
        private int row;
        private int column;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);

            button.addActionListener((ActionEvent e) -> {
                if (table != null) {
                    String columnName = table.getColumnName(column);
                    if (loggedUser != null && "staff".equals(loggedUser.role)) {
                        if (columnName.equals("Edit")) {
                            // Handle "Edit" action
                            bookListIndex = row;
                            currentPage = "update";
                            displayFrame();
                        }
                        if (columnName.equals("Action")) {
                            // Handle "Delete" action
                            bookArray.remove(row);
                            ((DefaultTableModel) table.getModel()).removeRow(row);
                            bookObject.saveIntoFile(bookArray);
                        }
                    } else {
                        Book bookSelected = bookArray.get(row);
                        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                        String today = formatter.format(new Date());
                        Booking newBooking = new Booking(loggedUser.username, bookSelected.id, bookSelected.title, today);
                        bookingArray.add(newBooking);
                        bookingObject.createBooking(newBooking);
                    }
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.table = table;
            this.row = row;
            this.column = column;

            String columnName = table.getColumnName(column);
            if (loggedUser != null && "staff".equals(loggedUser.role)) {
                if (columnName.equals("Edit")) {
                    button.setText("Edit");
                } 
                if (columnName.equals("Action")) {
                    button.setText("Delete");
                }
            } else {
                button.setText("Book");
            }
            return button;
        }
    }

    
    static class BookingButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public BookingButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (loggedUser != null && loggedUser.role.equals("staff")) {
                setText("Returned");
            }
            return this;
        }
    }

    static class BookingButtonEditor extends DefaultCellEditor {
        private final JButton button;
        private JTable table; // Reference to the JTable
        private int row;

        public BookingButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener((@SuppressWarnings("unused") ActionEvent e) -> {
                if (loggedUser != null && loggedUser.role.equals("staff")) {
                    bookingArray.remove(row); // Remove from ArrayList
                    ((DefaultTableModel) table.getModel()).removeRow(row); // Remove from table model
                    bookingObject.saveIntoFile(bookingArray); // Save changes to file
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.table = table; // Capture the reference to the JTable
            this.row = row; // Capture the row index
            if (loggedUser != null && loggedUser.role.equals("staff")) {
                button.setText("Returned");
            }
            return button;
        }
    }

    
    private static void displayFrame() {
        mainFrame.getContentPane().removeAll();

        switch (currentPage) {
            case "login" -> setupLoginPanel(mainFrame);
            case "table" -> setupTablePanel(mainFrame);
            case "create" -> setupCreatePanel(mainFrame);
            case "update" -> setupUpdatePanel(mainFrame);
            case "booking" -> setupBookingPanel(mainFrame);
            default -> {
            }
        }

        // Revalidate and repaint to ensure components are displayed properly after the update
        mainFrame.revalidate();
        mainFrame.repaint();
    }
    
    @SuppressWarnings({ "unchecked", "unused" })
    private static void setupLoginPanel(JFrame frame) {
        JPanel loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        //Title Label
        JLabel titleLabel = new JLabel("Library Catalog Management", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        loginPanel.add(titleLabel, gbc);

        //Username Row
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        loginPanel.add(new JLabel("Username: "), gbc);

        JTextField username = new JTextField(15);
        gbc.gridx = 1;
        loginPanel.add(username, gbc);

        //Password Row
        gbc.gridy = 2;
        gbc.gridx = 0;
        loginPanel.add(new JLabel("Password: "), gbc);

        JPasswordField password = new JPasswordField(15);
        gbc.gridx = 1;
        loginPanel.add(password, gbc);

        //Register Prompt Row
        gbc.gridy = 3;
        gbc.gridx = 0;
        JPanel noAccountPanel = new JPanel(new GridBagLayout());
        noAccountPanel.add(new JLabel("Don't have an account? "), gbc);

        JButton changeRegisterButton = new JButton("Register now!");
        gbc.gridx = 1;
        noAccountPanel.add(changeRegisterButton, gbc);
        loginPanel.add(noAccountPanel, gbc);

        //Register Elements Row
        JPanel registerElements = new JPanel(new GridBagLayout());
        JLabel roleLabel = new JLabel("I am a: ");
        @SuppressWarnings("rawtypes")
        JComboBox role = new JComboBox();
        role.addItem("Student");
        role.addItem("Staff");

        GridBagConstraints regGbc = new GridBagConstraints();
        regGbc.insets = new Insets(5,5,5,5);
        regGbc.gridx = 0;
        regGbc.gridy = 0;
        registerElements.add(roleLabel, regGbc);

        regGbc.gridx = 1;
        registerElements.add(role, regGbc);

        registerElements.setVisible(false);
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        loginPanel.add(registerElements, gbc);

        //Buttons Row

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton loginButton = new JButton("Login");
        JButton guestButton = new JButton("Guest");
        JButton registerButton = new JButton("Register");
        JButton backtoLoginButton = new JButton("Back");

        buttonPanel.add(loginButton);
        buttonPanel.add(guestButton);
        buttonPanel.add(registerButton);
        buttonPanel.add(backtoLoginButton);

        registerButton.setVisible(false);
        backtoLoginButton.setVisible(false);

        gbc.gridy = 5;
        loginPanel.add(buttonPanel, gbc);

        // Action Listeners
        changeRegisterButton.addActionListener(e -> {
            registerElements.setVisible(true);
            registerButton.setVisible(true);
            backtoLoginButton.setVisible(true);

            loginButton.setVisible(false);
            guestButton.setVisible(false);
            noAccountPanel.setVisible(false);
        });

        backtoLoginButton.addActionListener(e -> {
            registerElements.setVisible(false);
            registerButton.setVisible(false);
            backtoLoginButton.setVisible(false);

            loginButton.setVisible(true);
            guestButton.setVisible(true);
            noAccountPanel.setVisible(true);
        });

        registerButton.addActionListener(e -> {
            try {
                User userObject = new User();
                String usernameText = username.getText();
                String passwordText = new String(password.getPassword());
                String roleText = role.getSelectedItem().toString();
                userObject.registerUser(usernameText, passwordText, roleText);

                JOptionPane.showMessageDialog(null, "You have been registered, please login");

                registerElements.setVisible(false);
                registerButton.setVisible(false);
                backtoLoginButton.setVisible(false);

                loginButton.setVisible(true);
                guestButton.setVisible(true);
                noAccountPanel.setVisible(true);
            } catch (NullPointerException ex) {
                JOptionPane.showMessageDialog(null, "Please fill in all details", "Warning", JOptionPane.WARNING_MESSAGE);
            } catch (HeadlessException ex) {
                JOptionPane.showMessageDialog(null, "Error occurred", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        loginButton.addActionListener(e -> {
            try {
                User userObject = new User();
                String usernameText = username.getText();
                String passwordText = new String(password.getPassword());
                loggedUser = userObject.validateUser(usernameText, passwordText);
    
                if (loggedUser != null) {
                    currentPage = "table";
                    displayFrame();
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid username or password", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (HeadlessException ex) {
                JOptionPane.showMessageDialog(null, "An error occurred", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    
        guestButton.addActionListener(e -> {
            currentPage = "table";
            displayFrame();
        });
    
        // Add login panel to frame
        frame.add(loginPanel);
        frame.revalidate();
        frame.repaint();
    };

    private static void setupTablePanel(JFrame frame) {
        // panels for mainframe
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Add padding between components

        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBackground(Color.LIGHT_GRAY);

        JLabel headerText = new JLabel("<html><h1 style='text-align: center;'>Library Catalog</h1></html>");
        headerText.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        topPanel.add(headerText, gbc);

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JLabel welcomeText = new JLabel(loggedUser != null ? "Welcome, " + loggedUser.username : "Logged as guest");
        JButton loginButton = new JButton("Login");
        JButton logoutButton = new JButton("Logout");

        loginButton.addActionListener((@SuppressWarnings("unused") ActionEvent e) -> {
            currentPage = "login";
            displayFrame();
        });

        logoutButton.addActionListener((@SuppressWarnings("unused") ActionEvent e) -> {
            loggedUser = null;
            currentPage = "login";
            displayFrame();
        });

        if(loggedUser != null) {
            loginButton.setVisible(false);
            welcomeText = new JLabel("<html>Welcome, <span style='color:blue;'>" + loggedUser.username + "</span></html>");
        } else {
            logoutButton.setVisible(false);
        }

        userPanel.add(welcomeText);
        userPanel.add(loginButton);
        userPanel.add(logoutButton);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        topPanel.add(userPanel, gbc);

        // Add top panel to main panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(topPanel, gbc);

        // Search panel
        JPanel searchPanel = new JPanel(new GridBagLayout());
        JTextField searchInput = new JTextField(30);
        JButton clearButton = new JButton("Clear");
        JButton searchButton = new JButton("Search");

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 2.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        searchPanel.add(searchInput, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        searchPanel.add(clearButton, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        searchPanel.add(searchButton, gbc);

        // Add search panel to main panel
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(searchPanel, gbc);

        // Table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        String[] columns = loggedUser == null
            ? new String[]{"ID", "Title", "Author", "Summary", "Date", "Price"}
            : loggedUser.role.equals("student")
                ? new String[]{"ID", "Title", "Author", "Summary", "Date", "Price", "Action"}
                : new String[]{"ID", "Title", "Author", "Summary", "Date", "Price", "Edit", "Action"};
        DefaultTableModel tableModel = bookObject.populateTableModel(columns, loggedUser, bookArray);
        JTable bookListTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(bookListTable);

        if (loggedUser != null) {
            bookListTable.getColumn("Action").setCellRenderer(new ButtonRenderer());
            bookListTable.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox()));
            if (loggedUser.role.equals("staff")) {
                bookListTable.getColumn("Edit").setCellRenderer(new ButtonRenderer());
                bookListTable.getColumn("Edit").setCellEditor(new ButtonEditor(new JCheckBox()));
            }
        }

        tablePanel.add(scrollPane, BorderLayout.CENTER);

        searchButton.addActionListener( (@SuppressWarnings("unused") ActionEvent e) -> {
            bookArray = bookObject.searchArray(searchInput.getText(), bookArray);
            bookListTable.setModel(bookObject.populateTableModel(columns, loggedUser, bookArray));
            if (loggedUser != null) {
                bookListTable.getColumn("Action").setCellRenderer(new ButtonRenderer());
                bookListTable.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox()));
                if (loggedUser.role.equals("staff")) {
                    bookListTable.getColumn("Edit").setCellRenderer(new ButtonRenderer());
                    bookListTable.getColumn("Edit").setCellEditor(new ButtonEditor(new JCheckBox()));
                }
            }
        });

        clearButton.addActionListener((@SuppressWarnings("unused") ActionEvent e) -> {
            searchInput.setText("");
            bookArray = bookObject.readFromFile();
            bookListTable.setModel(bookObject.populateTableModel(columns, loggedUser, bookArray));
            if (loggedUser != null) {
                bookListTable.getColumn("Action").setCellRenderer(new ButtonRenderer());
                bookListTable.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox()));
                if (loggedUser.role.equals("staff")) {
                    bookListTable.getColumn("Edit").setCellRenderer(new ButtonRenderer());
                    bookListTable.getColumn("Edit").setCellEditor(new ButtonEditor(new JCheckBox()));
                }
            }
        });

        // Add table panel to main panel
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        mainPanel.add(tablePanel, gbc);

        // Bottom panel with buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton createPageButton = new JButton("Add New Book");
        JButton bookingListButton = new JButton("Booking List");
        
        createPageButton.setVisible(loggedUser != null && loggedUser.role.equals("staff"));
        bookingListButton.setVisible(loggedUser != null);

        createPageButton.addActionListener(_ -> {
            currentPage = "create";
            displayFrame();
        });

        bookingListButton.addActionListener(_ -> {
            try {
                currentPage = "booking";
                displayFrame();
            } catch (Exception exception) {
                JOptionPane.showMessageDialog(null, "Error: " + exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        bottomPanel.add(createPageButton);
        bottomPanel.add(bookingListButton);

        // Add bottom panel to main panel
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;
        gbc.weighty = 0;
        mainPanel.add(bottomPanel, gbc);

        // Add main panel to frame
        frame.setLayout(new BorderLayout());
        frame.add(mainPanel, BorderLayout.CENTER);
    }

    @SuppressWarnings("unchecked")
    private static void setupCreatePanel(JFrame frame) {
        // panels for mainframe
        JPanel topPanelMain = new JPanel();
        topPanelMain.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel centerPanelMain = new JPanel();
        centerPanelMain.setLayout(new BoxLayout(centerPanelMain, BoxLayout.Y_AXIS));

        JPanel bottomPanelMain = new JPanel();
        bottomPanelMain.setLayout(new FlowLayout());

        // top panel elements
        JLabel headerText = new JLabel("<html><h1 style='text-align: center;'>Create book</h1></html>");
        topPanelMain.add(headerText);
        topPanelMain.setBackground(Color.LIGHT_GRAY);
        topPanelMain.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // center panel elements
        JPanel createPanel = new JPanel();  // panel for create page
        createPanel.setLayout(new BoxLayout(createPanel, BoxLayout.Y_AXIS));

        // form
        JTextField idInput = new JTextField();
        JTextField titleInput = new JTextField();
        JTextField authorInput = new JTextField();
        JTextArea summaryInput = new JTextArea();
        JPanel dateInput = new JPanel();
        dateInput.setLayout(new BoxLayout(dateInput, BoxLayout.X_AXIS));
        @SuppressWarnings("rawtypes")
        JComboBox dateDayInput = new JComboBox();
        for (String day : dates) {
            dateDayInput.addItem(day);
        }
        @SuppressWarnings("rawtypes")
        JComboBox dateMonthInput = new JComboBox();
        for (String month : months) {
            dateMonthInput.addItem(month);
        }
        @SuppressWarnings("rawtypes")
        JComboBox dateYearInput = new JComboBox();
        for (String year : years) {
            dateYearInput.addItem(year);
        }
        dateInput.add(dateDayInput);
        dateInput.add(dateMonthInput);
        dateInput.add(dateYearInput);
        JSpinner priceInput = new JSpinner(new SpinnerNumberModel(0.0, 0.0, Integer.MAX_VALUE, 0.1));

        centerPanelMain.add(createPanel);
        
        // bottom panel elements
        JButton createBookButton = new JButton("Create");
        JButton backBookButton = new JButton("Back");

        createBookButton.addActionListener((@SuppressWarnings("unused") ActionEvent e) -> {
            try {
                priceInput.commitEdit();
                Double priceValue = (Double) priceInput.getValue();
                
                // append into book array list
                bookArray.add(new Book(
                        Integer.parseInt(idInput.getText()),
                        titleInput.getText(),
                        authorInput.getText(),
                        summaryInput.getText(),
                        dateDayInput.getSelectedItem().toString() +"-"+ dateMonthInput.getSelectedItem().toString() +"-"+ dateYearInput.getSelectedItem().toString(),
                        Math.round(priceValue * 100.0) / 100.0
                ));
                
                // save current array into file
                bookObject.saveIntoFile(bookArray);
                
                // reset form inputs
                idInput.setText("");
                titleInput.setText("");
                authorInput.setText("");
                summaryInput.setText("");
                dateDayInput.setSelectedIndex(-1);
                dateMonthInput.setSelectedIndex(-1);
                dateYearInput.setSelectedIndex(-1);
                priceInput.setValue(0.0);
                
                // change page state
                currentPage = "table";
                displayFrame();
            }
            catch (NullPointerException exception) {
                JOptionPane.showMessageDialog(null, "Please fill in all inputs", "Warning", JOptionPane.WARNING_MESSAGE);
            }
            catch (NumberFormatException exception) {
                JOptionPane.showMessageDialog(null, "Please enter number for id and price", "Warning", JOptionPane.WARNING_MESSAGE);
            }
            catch (ParseException exception) {
                JOptionPane.showMessageDialog(null, "Error: "+exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        backBookButton.addActionListener((@SuppressWarnings("unused") ActionEvent e) -> {
            try {
                currentPage = "table";
                displayFrame();
            }
            catch (Exception exception) {
                JOptionPane.showMessageDialog(null, "Error: "+exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        gbc.gridx = 1;
        bottomPanelMain.add(createBookButton, gbc);

        gbc.gridx = 2;
        bottomPanelMain.add(backBookButton, gbc);
        
        bottomPanelMain.add(createBookButton);
        bottomPanelMain.add(backBookButton);
        
        idInput.setPreferredSize(new Dimension(400, 25));
        titleInput.setPreferredSize(new Dimension(400, 25));
        authorInput.setPreferredSize(new Dimension(400, 25));
        summaryInput.setPreferredSize(new Dimension(400, 100)); // Larger for multiline text
        dateDayInput.setPreferredSize(new Dimension(60, 25));
        dateMonthInput.setPreferredSize(new Dimension(100, 25));
        dateYearInput.setPreferredSize(new Dimension(80, 25));
        priceInput.setPreferredSize(new Dimension(100, 25));

        // Add components for user-friendly and improved UI layout
        createPanel.setLayout(new GridBagLayout());
        gbc.insets = new Insets(10, 10, 10, 10); // Add spacing around components

        gbc.fill = GridBagConstraints.HORIZONTAL;

        // ID Label and Input
        gbc.gridx = 0;
        gbc.gridy = 0;
        createPanel.add(new JLabel("ID:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        createPanel.add(idInput, gbc);

        // Title Label and Input
        gbc.gridx = 0;
        gbc.gridy = 1;
        createPanel.add(new JLabel("Title:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        createPanel.add(titleInput, gbc);

        // Author Label and Input
        gbc.gridx = 0;
        gbc.gridy = 2;
        createPanel.add(new JLabel("Author:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        createPanel.add(authorInput, gbc);

        // Summary Label and Input
        gbc.gridx = 0;
        gbc.gridy = 3;
        createPanel.add(new JLabel("Summary:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        createPanel.add(summaryInput, gbc);
        gbc.gridwidth = 1;

        // Date Label and Inputs
        gbc.gridx = 0;
        gbc.gridy = 4;
        createPanel.add(new JLabel("Date:"), gbc);

        // Day Dropdown
        gbc.gridx = 1;
        createPanel.add(dateDayInput, gbc);

        // Month Dropdown
        gbc.gridx = 2;
        createPanel.add(dateMonthInput, gbc);

        // Year Dropdown
        gbc.gridx = 3;
        createPanel.add(dateYearInput, gbc);

        // Price Label and Input
        gbc.gridx = 0;
        gbc.gridy = 5;
        createPanel.add(new JLabel("Price:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        createPanel.add(priceInput, gbc);
        gbc.gridwidth = 1;

        // Buttons (Create and Back)
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 10, 5, 10);
        buttonPanel.add(createBookButton, gbc);

        gbc.gridx = 1;
        buttonPanel.add(backBookButton, gbc);
       
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 4;
        createPanel.add(buttonPanel, gbc);
        bottomPanelMain.add(createBookButton);
        bottomPanelMain.add(backBookButton);
        
        frame.add(topPanelMain, BorderLayout.NORTH);
        frame.add(centerPanelMain, BorderLayout.CENTER);
        frame.add(bottomPanelMain, BorderLayout.SOUTH);
    }
    
    @SuppressWarnings("unchecked")
    private static void setupUpdatePanel(JFrame frame) {
        // panels for mainframe
        JPanel topPanelMain = new JPanel();
        topPanelMain.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel centerPanelMain = new JPanel();
        centerPanelMain.setLayout(new BoxLayout(centerPanelMain, BoxLayout.Y_AXIS));

        JPanel bottomPanelMain = new JPanel();
        bottomPanelMain.setLayout(new FlowLayout());

        // top panel elements
        JLabel headerText = new JLabel("<html><h1 style='text-align: center;'>Update book</h1></html>");
        topPanelMain.add(headerText);
        topPanelMain.setBackground(Color.LIGHT_GRAY);
        topPanelMain.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // center panel elements
        JPanel createPanel = new JPanel();  // panel for create page
        createPanel.setLayout(new BoxLayout(createPanel, BoxLayout.Y_AXIS));

        // form
        Book book = bookArray.get(bookListIndex);
        
        JTextField idInput = new JTextField(); 
        JTextField titleInput = new JTextField();
        JTextField authorInput = new JTextField();
        JTextArea summaryInput = new JTextArea();
        JPanel dateInput = new JPanel();
        dateInput.setLayout(new BoxLayout(dateInput, BoxLayout.X_AXIS));
        
        @SuppressWarnings("rawtypes")
        JComboBox dateDayInput = new JComboBox();
        for (String day : dates) {
            dateDayInput.addItem(day);
        }
        @SuppressWarnings("rawtypes")
        JComboBox dateMonthInput = new JComboBox();
        for (String month : months) {
            dateMonthInput.addItem(month);
        }
        @SuppressWarnings("rawtypes")
        JComboBox dateYearInput = new JComboBox();
        for (String year : years) {
            dateYearInput.addItem(year);
        }
        dateInput.add(dateDayInput);
        dateInput.add(dateMonthInput);
        dateInput.add(dateYearInput);
        JSpinner priceInput = new JSpinner(new SpinnerNumberModel(0.0, 0.0, Integer.MAX_VALUE, 0.1));
        
        // provide existing data
        idInput.setText(String.valueOf(book.id));
        titleInput.setText(book.title);
        authorInput.setText(book.author);
        summaryInput.setText(book.summary);
        String[] dateParts = book.date.split("-"); // split day,month,year from string
        dateDayInput.setSelectedItem(dateParts[0]);
        dateMonthInput.setSelectedItem(dateParts[1]);
        dateYearInput.setSelectedItem(dateParts[2]);
        priceInput.setValue(book.price);

        centerPanelMain.add(createPanel);
        
        // bottom panel elements
        JButton updateBookButton = new JButton("Update");
        JButton backBookButton = new JButton("Back");

        updateBookButton.addActionListener((@SuppressWarnings("unused") ActionEvent e) -> {
            try {
                priceInput.commitEdit();
                Double priceValue = (Double) priceInput.getValue();
                
                // append into book array list
                if (
                    !idInput.getText().equals("") &&
                    !titleInput.getText().equals("") &&
                    !authorInput.getText().equals("") &&
                    !summaryInput.getText().equals("")
                ) {
                    bookArray.set(bookListIndex, new Book(
                        Integer.parseInt(idInput.getText()),
                        titleInput.getText(),
                        authorInput.getText(),
                        summaryInput.getText(),
                        dateDayInput.getSelectedItem().toString() + "-" + dateMonthInput.getSelectedItem().toString() + "-" + dateYearInput.getSelectedItem().toString(),
                        Math.round(priceValue * 100.0) / 100.0
                    ));
                    
                    // save current array into file
                    bookObject.saveIntoFile(bookArray);

                    // reset form inputs
                    idInput.setText("");
                    titleInput.setText("");
                    authorInput.setText("");
                    summaryInput.setText("");
                    dateDayInput.setSelectedIndex(-1);
                    dateMonthInput.setSelectedIndex(-1);
                    dateYearInput.setSelectedIndex(-1);
                    priceInput.setValue(0.0);

                    // change page state
                    currentPage = "table";
                    displayFrame();
                }
                else {
                    JOptionPane.showMessageDialog(null, "Please do not leave empty input.", "Warning", JOptionPane.WARNING_MESSAGE);
                }
            }
            catch (NullPointerException exception) {
                JOptionPane.showMessageDialog(null, "Please fill in all inputs", "Warning", JOptionPane.WARNING_MESSAGE);
            }
            catch (NumberFormatException exception) {
                JOptionPane.showMessageDialog(null, "Please enter number for id and price", "Warning", JOptionPane.WARNING_MESSAGE);
            }
            catch (ParseException exception) {
                JOptionPane.showMessageDialog(null, "Error: "+exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        backBookButton.addActionListener((@SuppressWarnings("unused") ActionEvent e) -> {
            try {
                currentPage = "table";
                displayFrame();
            }
            catch (Exception exception) {
                JOptionPane.showMessageDialog(null, "Error: "+exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        gbc.gridx = 1;
        bottomPanelMain.add(updateBookButton, gbc);

        gbc.gridx = 2;
        bottomPanelMain.add(backBookButton, gbc);
        
        bottomPanelMain.add(updateBookButton);
        bottomPanelMain.add(backBookButton);
        
        idInput.setPreferredSize(new Dimension(400, 25));
        titleInput.setPreferredSize(new Dimension(400, 25));
        authorInput.setPreferredSize(new Dimension(400, 25));
        summaryInput.setPreferredSize(new Dimension(400, 100)); // Larger for multiline text
        dateDayInput.setPreferredSize(new Dimension(60, 25));
        dateMonthInput.setPreferredSize(new Dimension(100, 25));
        dateYearInput.setPreferredSize(new Dimension(80, 25));
        priceInput.setPreferredSize(new Dimension(100, 25));

        // Add components for user-friendly and improved UI layout
        createPanel.setLayout(new GridBagLayout());
        gbc.insets = new Insets(10, 10, 10, 10); // Add spacing around components

        gbc.fill = GridBagConstraints.HORIZONTAL;

        // ID Label and Input
        gbc.gridx = 0;
        gbc.gridy = 0;
        createPanel.add(new JLabel("ID:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        createPanel.add(idInput, gbc);

        // Title Label and Input
        gbc.gridx = 0;
        gbc.gridy = 1;
        createPanel.add(new JLabel("Title:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        createPanel.add(titleInput, gbc);

        // Author Label and Input
        gbc.gridx = 0;
        gbc.gridy = 2;
        createPanel.add(new JLabel("Author:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        createPanel.add(authorInput, gbc);

        // Summary Label and Input
        gbc.gridx = 0;
        gbc.gridy = 3;
        createPanel.add(new JLabel("Summary:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        createPanel.add(summaryInput, gbc);
        gbc.gridwidth = 1;

        // Date Label and Inputs
        gbc.gridx = 0;
        gbc.gridy = 4;
        createPanel.add(new JLabel("Date:"), gbc);

        // Day Dropdown
        gbc.gridx = 1;
        createPanel.add(dateDayInput, gbc);

        // Month Dropdown
        gbc.gridx = 2;
        createPanel.add(dateMonthInput, gbc);

        // Year Dropdown
        gbc.gridx = 3;
        createPanel.add(dateYearInput, gbc);

        // Price Label and Input
        gbc.gridx = 0;
        gbc.gridy = 5;
        createPanel.add(new JLabel("Price:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        createPanel.add(priceInput, gbc);
        gbc.gridwidth = 1;

        // Buttons (Create and Back)
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 10, 5, 10);
        buttonPanel.add(updateBookButton, gbc);

        gbc.gridx = 1;
        buttonPanel.add(backBookButton, gbc);
       
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 4;
        createPanel.add(buttonPanel, gbc);
        bottomPanelMain.add(updateBookButton);
        bottomPanelMain.add(backBookButton);
        
        frame.add(topPanelMain, BorderLayout.NORTH);
        frame.add(centerPanelMain, BorderLayout.CENTER);
        frame.add(bottomPanelMain, BorderLayout.SOUTH);
    }
    
    private static void setupBookingPanel(JFrame frame) {
        bookingArray = bookingObject.getBooking(loggedUser!=null && loggedUser.role.equals("student")? loggedUser.username : null);
        
        // panels for mainframe
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Add padding between components

        // Top Panel
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBackground(Color.LIGHT_GRAY);

        // Header
        JLabel headerText = new JLabel("<html><h1 style='text-align: center;'>" + (loggedUser != null && loggedUser.role.equals("student") ? "My " : "") + "Booking List</h1></html>");
        headerText.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        topPanel.add(headerText, gbc);

        // User info and buttons
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JLabel welcomeText = new JLabel(loggedUser != null ? "Welcome, " + loggedUser.username : "Logged as guest");
        JButton loginButton = new JButton("Login");
        JButton logoutButton = new JButton("Logout");

        loginButton.addActionListener(_ -> {
            currentPage = "login";
            displayFrame();
        });
    
        logoutButton.addActionListener(_ -> {
            loggedUser = null; // Reset logged user
            currentPage = "login";
            displayFrame();
        });
    
        if (loggedUser == null) {
            logoutButton.setVisible(false);
        } else {
            loginButton.setVisible(false);
        }
    
        userPanel.add(welcomeText);
        userPanel.add(loginButton);
        userPanel.add(logoutButton);
    
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        topPanel.add(userPanel, gbc);
    
        // Add top panel to main panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(topPanel, gbc);
    
        // Search panel
        JPanel searchPanel = new JPanel(new GridBagLayout());
        JTextField searchInput = new JTextField(30);
        JButton clearButton = new JButton("Clear");
        JButton searchButton = new JButton("Search");
    
        // Search input field
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 2.0; // Allow the search field to grow
        gbc.fill = GridBagConstraints.HORIZONTAL;
        searchPanel.add(searchInput, gbc);
    
        // Clear button
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0; // Prevent button from growing
        gbc.fill = GridBagConstraints.NONE;
        searchPanel.add(clearButton, gbc);
    
        // Search button
        gbc.gridx = 2;
        gbc.gridy = 0;
        searchPanel.add(searchButton, gbc);
    
        // Add search panel to main panel
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(searchPanel, gbc);
    
        // Table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        List<String> columnsList = new ArrayList<>(Arrays.asList("ID", "Title", "Borrower", "Borrowed Date"));
        if (loggedUser != null && loggedUser.role.equals("staff")) {
            columnsList.add("Action");
        }
        String[] columns = columnsList.toArray(String[]::new);
        DefaultTableModel tableModel = bookingObject.populateTableModel(columns, loggedUser, bookingArray);
        JTable bookingListTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(bookingListTable);
    
        if (loggedUser != null && loggedUser.role.equals("staff")) {
            bookingListTable.getColumn("Action").setCellRenderer(new BookingButtonRenderer());
            bookingListTable.getColumn("Action").setCellEditor(new BookingButtonEditor(new JCheckBox()));
        }
    
        searchButton.addActionListener(_ -> {
            bookingArray = bookingObject.searchArray(searchInput.getText(), bookingArray,
                loggedUser != null && loggedUser.role.equals("student") ? loggedUser.username : null);
            bookingListTable.setModel(bookingObject.populateTableModel(columns, loggedUser, bookingArray));
            if (loggedUser != null && loggedUser.role.equals("staff")) {
                bookingListTable.getColumn("Action").setCellRenderer(new BookingButtonRenderer());
                bookingListTable.getColumn("Action").setCellEditor(new BookingButtonEditor(new JCheckBox()));
            }
        });
    
        clearButton.addActionListener(_ -> {
            searchInput.setText("");
            bookingArray = bookingObject.getBooking(loggedUser != null && loggedUser.role.equals("student") ? loggedUser.username : null);
            bookingListTable.setModel(bookingObject.populateTableModel(columns, loggedUser, bookingArray));
            if (loggedUser != null && loggedUser.role.equals("staff")) {
                bookingListTable.getColumn("Action").setCellRenderer(new BookingButtonRenderer());
                bookingListTable.getColumn("Action").setCellEditor(new BookingButtonEditor(new JCheckBox()));
            }
        });
    
        tablePanel.add(scrollPane, BorderLayout.CENTER);
    
        // Add table panel to main panel
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        mainPanel.add(tablePanel, gbc);
    
        // Bottom panel with back button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton backPageButton = new JButton("Back");
    
        backPageButton.addActionListener(_ -> {
            currentPage = "table";
            displayFrame();
        });
    
        bottomPanel.add(backPageButton);
    
        // Add bottom panel to main panel
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;
        gbc.weighty = 0;
        mainPanel.add(bottomPanel, gbc);
    
        // Add main panel to frame
        frame.setLayout(new BorderLayout());
        frame.add(mainPanel, BorderLayout.CENTER);
    }

    
    @SuppressWarnings({"Convert2Lambda"})
    public static void main(String[] args) {
        // initialize saved book list
        bookArray = bookObject.readFromFile();
        
        // main frame will be the list of records
        mainFrame = new JFrame("Library Catalog Management");
        mainFrame.setSize(1280, 720);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLayout(new BorderLayout());
        
        displayFrame();
        
        mainFrame.getContentPane().setBackground(Color.WHITE);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setVisible(true);
        
    }
};   