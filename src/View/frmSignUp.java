/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package View;
import Database.Connect;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.swing.*;
import java.sql.*;
/**
 *
 * @author mSoSm
 */
public class frmSignUp extends javax.swing.JInternalFrame {

    /**
     * Creates new form frmSignUp
     */
    public frmSignUp() {
        initComponents();
    }
    
    private String hashPassword(String password) {
    try {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();

        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.toString();
    } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
        return null;
    }
}
    
    private void signUp() {
    String name = txtName.getText().trim();
    String username = txtUserName.getText().trim();
    String password = new String(txtPass.getPassword()).trim();
    String confirmPassword = new String(txtCfPass.getPassword()).trim();
    String email = txtEmail.getText().trim();
    String phone = txtPhone.getText().trim();
    java.util.Date birthDate = jDateChooser1.getDate(); // Lấy ngày sinh (có thể null)

    // Validate input fields
    if (name.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || email.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please fill in all required fields.", "Warning", JOptionPane.WARNING_MESSAGE);
        return;
    }
    if (password.length() < 6 || confirmPassword.length()< 6){
        JOptionPane.showMessageDialog(this, "Passwords must be at least 6 characters.", "Warning", JOptionPane.WARNING_MESSAGE);
        return;
    }
    if (!password.equals(confirmPassword)) {
        JOptionPane.showMessageDialog(this, "Passwords do not match.", "Warning", JOptionPane.WARNING_MESSAGE);
        return;
    }

    try {
        Connect dbConnection = new Connect();
        // Check if username already exists for role 'Customer'
        String checkUsernameSQL = "SELECT COUNT(*) AS count FROM Users WHERE Username = ?";
        Object[] checkUsernameParams = {username};
        ResultSet usernameResult = dbConnection.selectQuery(checkUsernameSQL, checkUsernameParams);
        int usernameCount = 0;
        if (usernameResult.next()) {
            usernameCount = usernameResult.getInt("count");
        }
        if (usernameCount > 0) {
            JOptionPane.showMessageDialog(this, "Username already exists. Please choose a different one.", "Warning", JOptionPane.WARNING_MESSAGE);
            dbConnection.close();
            return;
        }

        // Check if email already exists for role 'Customer'
        String checkEmailSQL = "SELECT COUNT(*) AS count FROM Users WHERE Email = ? AND Role = 'Customer'";
        Object[] checkEmailParams = {email};
        ResultSet emailResult = dbConnection.selectQuery(checkEmailSQL, checkEmailParams);
        int emailCount = 0;
        if (emailResult.next()) {
            emailCount = emailResult.getInt("count");
        }
        if (emailCount > 0) {
            JOptionPane.showMessageDialog(this, "Email already exists. Please use a different email.", "Warning", JOptionPane.WARNING_MESSAGE);
            dbConnection.close();
            return;
        }
        
        password= hashPassword(password);
        
        // Insert into Users table
        String insertUserSQL = "INSERT INTO Users (Username, Password, Role, Email, Phone) VALUES (?, ?, 'Customer', ?, ?)";
        Object[] userParams = {username, password, email, phone};
        dbConnection.executeQuery(insertUserSQL, userParams);

        // Retrieve UserID for the newly created user
        String selectUserSQL = "SELECT UserID FROM Users WHERE Username = ?";
        Object[] selectParams = {username};
        ResultSet rs = dbConnection.selectQuery(selectUserSQL, selectParams);
        int userId = -1;
        if (rs.next()) {
            userId = rs.getInt("UserID");
        }

        // Insert into Customers table
        if (userId != -1) {
            String insertCustomerSQL = "INSERT INTO Customers (UserID, Name, Phone, Birthday) VALUES (?, ?, ?, ?)";
            Object[] customerParams;

            if (birthDate != null) {
                customerParams = new Object[]{userId, name, phone, new java.sql.Date(birthDate.getTime())}; // Lưu ngày sinh vào cơ sở dữ liệu
            } else {
                customerParams = new Object[]{userId, name, phone, null}; // Ngày sinh là null
            }
            
            dbConnection.executeQuery(insertCustomerSQL, customerParams);

            JOptionPane.showMessageDialog(this, "Registration successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
            dbConnection.close();
            this.dispose(); // Đóng frmSignUp sau khi đăng ký thành công
        }
        dbConnection.close();
    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Registration failed. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
    } 
}




    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jComboBox1 = new javax.swing.JComboBox<>();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtUserName = new javax.swing.JTextField();
        txtPass = new javax.swing.JPasswordField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        txtCfPass = new javax.swing.JPasswordField();
        btnConfirm = new javax.swing.JButton();
        btnExit = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        txtEmail = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txtPhone = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jDateChooser1 = new com.toedter.calendar.JDateChooser();

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setText("Sign Up");

        jLabel2.setText("Full Name:");

        jLabel3.setText("UserName:");

        txtUserName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtUserNameActionPerformed(evt);
            }
        });

        jLabel4.setText("Password:");

        jLabel5.setText("Confirm Password:");

        btnConfirm.setText("Confirm");
        btnConfirm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConfirmActionPerformed(evt);
            }
        });

        btnExit.setText("Exit");
        btnExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExitActionPerformed(evt);
            }
        });

        jLabel6.setText("Email:");

        jLabel7.setText("Phone:");

        jLabel8.setText("Birth Day:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(75, 75, 75)
                        .addComponent(btnConfirm)
                        .addGap(109, 109, 109)
                        .addComponent(btnExit))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                    .addGap(52, 52, 52)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jLabel2)
                                        .addGroup(layout.createSequentialGroup()
                                            .addComponent(jLabel6)
                                            .addGap(3, 3, 3))))
                                .addGroup(layout.createSequentialGroup()
                                    .addContainerGap()
                                    .addComponent(jLabel5))
                                .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.TRAILING))
                            .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(txtUserName, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(txtPhone, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(txtEmail, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(txtName, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 276, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jDateChooser1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 248, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(txtPass, javax.swing.GroupLayout.PREFERRED_SIZE, 276, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtCfPass, javax.swing.GroupLayout.PREFERRED_SIZE, 276, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(165, 165, 165)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(txtPhone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel7)))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(jLabel2)
                                    .addGap(40, 40, 40))
                                .addComponent(jLabel6)))
                        .addGap(26, 26, 26)
                        .addComponent(jDateChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtUserName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtPass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addGap(21, 21, 21)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(txtCfPass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(39, 39, 39)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnExit)
                    .addComponent(btnConfirm))
                .addGap(18, 65, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnConfirmActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConfirmActionPerformed
        // TODO add your handling code here:
        signUp();
    }//GEN-LAST:event_btnConfirmActionPerformed

    private void btnExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExitActionPerformed
        // TODO add your handling code here:
        this.dispose();
    }//GEN-LAST:event_btnExitActionPerformed

    private void txtUserNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtUserNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtUserNameActionPerformed

    /**
     * @param args the command line arguments
     */

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnConfirm;
    private javax.swing.JButton btnExit;
    private javax.swing.JComboBox<String> jComboBox1;
    private com.toedter.calendar.JDateChooser jDateChooser1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPasswordField txtCfPass;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtName;
    private javax.swing.JPasswordField txtPass;
    private javax.swing.JTextField txtPhone;
    private javax.swing.JTextField txtUserName;
    // End of variables declaration//GEN-END:variables
}
