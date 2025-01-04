package com.mycompany.librarycatalogmanagement;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

class Book {
    protected int id;
    protected String title, author, summary, date;
    protected double price;

    // assign details for the book object
    Book (int id, String title, String author, String summary, String date, double price) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.summary = summary;
        this.date = date;
        this.price = price;
    }
}

public class LibraryCatalogManagement {
    private static Boolean createPageActive = false;
    private static final String dates[]
        = { "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "10",
            "11", "12", "13", "14", "15",
            "16", "17", "18", "19", "20",
            "21", "22", "23", "24", "25",
            "26", "27", "28", "29", "30",
            "31" };
    private static final String months[]
        = { "Jan", "feb", "Mar", "Apr",
            "May", "Jun", "July", "Aug",
            "Sup", "Oct", "Nov", "Dec" };
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
    
    private static DefaultTableModel populateTableModel(String[] columns) {
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        for (Book book : bookArray) {
            model.addRow(new Object[] { book.id, book.title, book.author, book.summary, book.date, book.price, "Delete" });
        }
        return model;
    }
    
    private static void saveIntoFile() {
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
    
    private static ArrayList<Book> readFromFile() {
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
            JOptionPane.showMessageDialog(
                null,
                "Failed to read file: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }

        return books;
    }
    
    private static ArrayList<Book> searchArray(String searchValue) {
        ArrayList<Book> filteredArray = new ArrayList<>();
        Pattern pattern = Pattern.compile(searchValue, Pattern.CASE_INSENSITIVE);
        
        for (Book book : bookArray) {
            if (pattern.matcher(book.title).find() || pattern.matcher(book.author).find() || pattern.matcher(book.summary).find()) {
                filteredArray.add(book);
            }
        }
        
        return filteredArray;
    }
    
    // Renderer for delete button
    static class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText("Delete");
            return this;
        }
    }
    
    static class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private JTable table; // Reference to the JTable
        private int row;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton("Delete");
            button.setOpaque(true);
            button.addActionListener(e -> {
                // Remove the row from bookArray and update the model
                bookArray.remove(row); // Remove from ArrayList
                ((DefaultTableModel) table.getModel()).removeRow(row); // Remove from table model
                saveIntoFile(); // Save changes to file
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.table = table; // Capture the reference to the JTable
            this.row = row; // Capture the row index
            return button;
        }
    }
    
    public static void main(String[] args) {
        // initialize saved book list
        bookArray = readFromFile();
        
        // main frame will be the list of records
        JFrame mainFrame = new JFrame();
        mainFrame.setTitle("Lirary Catalog Management");
        mainFrame.setSize(1280, 720);
        mainFrame.setLayout(new BorderLayout());
        
        // panels for mainframe
        JPanel topPanelMain = new JPanel();
        topPanelMain.setLayout(new BoxLayout(topPanelMain, BoxLayout.X_AXIS));
        
        JPanel centerPanelMain = new JPanel();
        centerPanelMain.setLayout(new BoxLayout(centerPanelMain, BoxLayout.Y_AXIS));
        
        JPanel bottomPanelMain = new JPanel();
        bottomPanelMain.setLayout(new FlowLayout());
        
        // top panel elements
        JLabel headerText = new JLabel("<html><h1>Catalog</h1></html>");
        topPanelMain.add(headerText);
        
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
        JComboBox dateDayInput = new JComboBox();
        for (String day : dates) {
            dateDayInput.addItem(day);
        }
        JComboBox dateMonthInput = new JComboBox();
        for (String month : months) {
            dateMonthInput.addItem(month);
        }
        JComboBox dateYearInput = new JComboBox();
        for (String year : years) {
            dateYearInput.addItem(year);
        }
        dateInput.add(dateDayInput);
        dateInput.add(dateMonthInput);
        dateInput.add(dateYearInput);
        JSpinner priceInput = new JSpinner(new SpinnerNumberModel(0.0, 0.0, Integer.MAX_VALUE, 0.1));
        
        createPanel.add(new JLabel("Id"));
        createPanel.add(idInput);
        createPanel.add(new JLabel("Title"));
        createPanel.add(titleInput);
        createPanel.add(new JLabel("Author"));
        createPanel.add(authorInput);
        createPanel.add(new JLabel("Summary"));
        createPanel.add(summaryInput);
        createPanel.add(new JLabel("Date"));
        createPanel.add(dateInput);
        createPanel.add(new JLabel("Price"));
        createPanel.add(priceInput);
        
        
        JPanel tablePanel = new JPanel();   // panel for table page
        tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
        
        JPanel searchContainer = new JPanel();
        searchContainer.setLayout(new BoxLayout(searchContainer, BoxLayout.X_AXIS));
        searchContainer.setPreferredSize(new Dimension(1280, 30));
        
        JTextField searchInput = new JTextField();
        searchInput.setMaximumSize(new Dimension(980, 25));
        
        JButton clearButton = new JButton("Clear");
        clearButton.setMaximumSize(new Dimension(100, 25));
        
        JButton searchButton = new JButton("Search");
        searchButton.setMaximumSize(new Dimension(200, 25));
        
         // Sample columns
        String[] columns = { "ID", "Title", "Author", "Summary", "Date", "Price", "Action" };
        DefaultTableModel tableModel = populateTableModel(columns);
        JTable bookListTable = new JTable(tableModel);

        // Add button renderer and editor
        bookListTable.getColumn("Action").setCellRenderer(new ButtonRenderer());
        bookListTable.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox()));

        
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bookArray = searchArray(searchInput.getText());
                // update table
                bookListTable.setModel(populateTableModel(columns));
                // Reapply button renderer and editor to the "Action" column
                bookListTable.getColumn("Action").setCellRenderer(new ButtonRenderer());
                bookListTable.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox()));
            }
        });
        
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchInput.setText("");
                bookArray = readFromFile();
                bookListTable.setModel(populateTableModel(columns));
                // Reapply button renderer and editor to the "Action" column
                bookListTable.getColumn("Action").setCellRenderer(new ButtonRenderer());
                bookListTable.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox()));
            }
        });

        searchContainer.add(searchInput);
        searchContainer.add(clearButton);
        searchContainer.add(searchButton); 
        tablePanel.add(searchContainer);
        tablePanel.add(bookListTable);
        centerPanelMain.add(createPanel);
        centerPanelMain.add(tablePanel);
        
        createPanel.setVisible(false);
        tablePanel.setVisible(true);
        
        // bottom panel element
        JButton createPageButton = new JButton("Add new book");
        JButton createBookButton = new JButton("Create");
        JButton backBookButton = new JButton("Back");
        
        createPageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createPageActive = true;
                headerText.setText("<html><h1>Create Book</h1></html>");
                tablePanel.setVisible(false);
                createPanel.setVisible(true);
                createPageButton.setVisible(false);
                createBookButton.setVisible(true);
                backBookButton.setVisible(true);
            }
        });
        
        createBookButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
                    saveIntoFile();

                    // update table to update with current array
                    DefaultTableModel updatedModel = populateTableModel(columns);
                    bookListTable.setModel(updatedModel);
                    // Reapply button renderer and editor to the "Action" column
                    bookListTable.getColumn("Action").setCellRenderer(new ButtonRenderer());
                    bookListTable.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox()));
                    
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
                    createPageActive = false;
                    headerText.setText("<html><h1>Catalog</h1></html>");
                    tablePanel.setVisible(true);
                    createPanel.setVisible(false);
                    createPageButton.setVisible(true);
                    createBookButton.setVisible(false);
                    backBookButton.setVisible(false);
                }
                catch (NullPointerException exception) {
                    JOptionPane.showMessageDialog(null, "Please fill in all inputs", "Warning", JOptionPane.WARNING_MESSAGE);
                } 
                catch (NumberFormatException exception) {
                    JOptionPane.showMessageDialog(null, "Please enter number for id and price", "Warning", JOptionPane.WARNING_MESSAGE);
                } 
                catch (Exception exception) {
                    JOptionPane.showMessageDialog(null, "Error: "+exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        backBookButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // change page state
                    createPageActive = false;
                    headerText.setText("<html><h1>Catalog</h1></html>");
                    tablePanel.setVisible(true);
                    createPanel.setVisible(false);
                    createPageButton.setVisible(true);
                    createBookButton.setVisible(false);
                    backBookButton.setVisible(false);
                }
                catch(Exception exception) {
                    JOptionPane.showMessageDialog(null, "Error occured: " + exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        bottomPanelMain.add(createPageButton);
        bottomPanelMain.add(createBookButton);
        bottomPanelMain.add(backBookButton);
        
        createPageButton.setVisible(true);
        createBookButton.setVisible(false);
        backBookButton.setVisible(false);
        
        mainFrame.add(topPanelMain, BorderLayout.NORTH);
        mainFrame.add(centerPanelMain, BorderLayout.CENTER);
        mainFrame.add(bottomPanelMain, BorderLayout.SOUTH);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setVisible(true);
        
    }
}
