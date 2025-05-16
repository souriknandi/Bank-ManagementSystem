import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class BankingManagementSystem extends JFrame {
    private Connection con;
    private Statement stmt;

    public BankingManagementSystem() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/bank", "root", "sourik");
            stmt = con.createStatement();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database Connection Failed: " + e.getMessage());
            return;
        }

        setTitle("Banking Management System");
        setSize(600, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(11, 1, 10, 10));

        JButton[] buttons = new JButton[] {
            new JButton("1. Show Customer Records"),
            new JButton("2. Add Customer Record"),
            new JButton("3. Delete Customer Record"),
            new JButton("4. Update Customer Information"),
            new JButton("5. Show Account Details"),
            new JButton("6. Show Loan Details"),
            new JButton("7. Deposit Money"),
            new JButton("8. Withdraw Money"),
            new JButton("9. Create Account"),
            new JButton("10. Apply for Loan"),
            new JButton("11. Exit")
        };

        buttons[0].addActionListener(e -> showCustomerRecords());
        buttons[1].addActionListener(e -> addCustomerRecord());
        buttons[2].addActionListener(e -> deleteCustomerRecord());
        buttons[3].addActionListener(e -> updateCustomerInfo());
        buttons[4].addActionListener(e -> showAccountDetails());
        buttons[5].addActionListener(e -> showLoanDetails());
        buttons[6].addActionListener(e -> depositMoney());
        buttons[7].addActionListener(e -> withdrawMoney());
        buttons[8].addActionListener(e -> createAccount());
        buttons[9].addActionListener(e -> applyForLoan());
        buttons[10].addActionListener(e -> System.exit(0));

        for (JButton btn : buttons) panel.add(btn);

        add(panel);
        setVisible(true);
    }

    private void showCustomerRecords() {
        try {
            ResultSet rs = stmt.executeQuery("SELECT * FROM customer");
            showTable("Customer Records", rs);
        } catch (SQLException e) {
            showError(e);
        }
    }

    private void addCustomerRecord() {
        JTextField id = new JTextField(), name = new JTextField(), phone = new JTextField(), city = new JTextField();
        Object[] fields = {"Customer No:", id, "Name:", name, "Phone:", phone, "City:", city};
        if (JOptionPane.showConfirmDialog(this, fields, "Add Customer", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                PreparedStatement ps = con.prepareStatement("INSERT INTO customer VALUES (?, ?, ?, ?)");
                ps.setString(1, id.getText()); ps.setString(2, name.getText());
                ps.setString(3, phone.getText()); ps.setString(4, city.getText());
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Customer Added!");
                showCustomerRecords();
            } catch (SQLException e) { showError(e); }
        }
    }

    private void deleteCustomerRecord() {
        String id = JOptionPane.showInputDialog("Enter Customer No to delete:");
        try {
            int deleted = stmt.executeUpdate("DELETE FROM customer WHERE cust_no='" + id + "'");
            JOptionPane.showMessageDialog(this, deleted > 0 ? "Customer deleted!" : "Customer not found!");
            showCustomerRecords();
        } catch (SQLException e) { showError(e); }
    }

    private void updateCustomerInfo() {
        String id = JOptionPane.showInputDialog("Enter Customer No to update:");
        String[] fields = {"name", "phone", "city"};
        String field = (String) JOptionPane.showInputDialog(this, "Field to update:", "Update", JOptionPane.QUESTION_MESSAGE, null, fields, fields[0]);
        if (field != null) {
            String newVal = JOptionPane.showInputDialog("Enter new value for " + field + ":");
            try {
                stmt.executeUpdate("UPDATE customer SET " + field + "='" + newVal + "' WHERE cust_no='" + id + "'");
                JOptionPane.showMessageDialog(this, "Customer Updated!");
                showCustomerRecords();
            } catch (SQLException e) { showError(e); }
        }
    }

    private void showAccountDetails() {
        String custId = JOptionPane.showInputDialog("Enter Customer No:");
        try {
            ResultSet rs = stmt.executeQuery("SELECT * FROM account NATURAL JOIN branch WHERE cust_no='" + custId + "'");
            showTable("Account Details", rs);
        } catch (SQLException e) { showError(e); }
    }

    private void showLoanDetails() {
        String custId = JOptionPane.showInputDialog("Enter Customer No:");
        try {
            ResultSet rs = stmt.executeQuery("SELECT * FROM loan NATURAL JOIN branch WHERE cust_no='" + custId + "'");
            showTable("Loan Details", rs);
        } catch (SQLException e) { showError(e); }
    }

    private void depositMoney() {
        String accNo = JOptionPane.showInputDialog("Enter Account No:");
        String amount = JOptionPane.showInputDialog("Enter Amount to Deposit:");
        try {
            stmt.executeUpdate("UPDATE account SET balance = balance + " + amount + " WHERE account_no='" + accNo + "'");
            JOptionPane.showMessageDialog(this, "Amount Deposited!");
            showAccountDetails();
        } catch (SQLException e) { showError(e); }
    }

    private void withdrawMoney() {
        String accNo = JOptionPane.showInputDialog("Enter Account No:");
        String amount = JOptionPane.showInputDialog("Enter Amount to Withdraw:");
        try {
            ResultSet rs = stmt.executeQuery("SELECT balance FROM account WHERE account_no='" + accNo + "'");
            if (rs.next()) {
                double balance = rs.getDouble("balance");
                double amt = Double.parseDouble(amount);
                if (amt > balance) {
                    JOptionPane.showMessageDialog(this, "Insufficient Balance!");
                    return;
                }
                stmt.executeUpdate("UPDATE account SET balance = balance - " + amt + " WHERE account_no='" + accNo + "'");
                JOptionPane.showMessageDialog(this, "Amount Withdrawn!");
                showAccountDetails();
            }
        } catch (SQLException e) { showError(e); }
    }

    private void createAccount() {
        JTextField accNo = new JTextField(), custNo = new JTextField(), type = new JTextField(), balance = new JTextField(), branchCode = new JTextField();
        Object[] fields = {"Account No:", accNo, "Customer No:", custNo, "Type:", type, "Initial Balance:", balance, "Branch Code:", branchCode};
        if (JOptionPane.showConfirmDialog(this, fields, "Create Account", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                PreparedStatement ps = con.prepareStatement("INSERT INTO account VALUES (?, ?, ?, ?, ?)");
                ps.setString(1, accNo.getText());
                ps.setString(2, custNo.getText());
                ps.setString(3, type.getText());
                ps.setDouble(4, Double.parseDouble(balance.getText()));
                ps.setString(5, branchCode.getText());
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Account Created!");
                showAccountDetails();
            } catch (SQLException e) { showError(e); }
        }
    }

    private void applyForLoan() {
        JTextField loanNo = new JTextField(), custNo = new JTextField(), amount = new JTextField(), branchCode = new JTextField();
        Object[] fields = {"Loan No:", loanNo, "Customer No:", custNo, "Amount:", amount, "Branch Code:", branchCode};
        if (JOptionPane.showConfirmDialog(this, fields, "Apply for Loan", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                PreparedStatement ps = con.prepareStatement("INSERT INTO loan VALUES (?, ?, ?, ?)");
                ps.setString(1, loanNo.getText());
                ps.setString(2, custNo.getText());
                ps.setDouble(3, Double.parseDouble(amount.getText()));
                ps.setString(4, branchCode.getText());
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Loan Applied Successfully!");
                showLoanDetails();
            } catch (SQLException e) { showError(e); }
        }
    }

    private void showTable(String title, ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int cols = meta.getColumnCount();
        DefaultTableModel model = new DefaultTableModel();
        for (int i = 1; i <= cols; i++) model.addColumn(meta.getColumnName(i));
        while (rs.next()) {
            Object[] row = new Object[cols];
            for (int i = 1; i <= cols; i++) row[i - 1] = rs.getObject(i);
            model.addRow(row);
        }
        JTable table = new JTable(model);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(500, 300));
        JOptionPane.showMessageDialog(this, scroll, title, JOptionPane.PLAIN_MESSAGE);
    }

    private void showError(Exception e) {
        JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(BankingManagementSystem::new);
    }
}

