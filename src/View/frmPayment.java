   /*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package View;

import Database.Connect;
import Process.Order;
import Process.Payments;
import javax.swing.JOptionPane;
import java.sql.ResultSet;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author mSoSm
 */
public final class frmPayment extends javax.swing.JInternalFrame {

    /**
     * Creates new form frmPayment
     */
    public Order or;
    public Payments data;
    public int id;
    public frmPayment(Order order) {
        initComponents();
        or = order;
        data =new Payments();
        data.orderID = or.OrderID;
        hideComponents();
        getpaymentMethod();
        displayOrderDetails();
        txtAmoutGiven.getDocument().addDocumentListener(new DocumentListener() {
        @Override
        public void insertUpdate(DocumentEvent e) {
            updateLabelPayable();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            updateLabelPayable();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            updateLabelPayable();
        }
    });
    }
    
    private void hideComponents() {
    LabelPhone.setVisible(false);
    txtPhone.setVisible(false);
    LabelName.setVisible(false);
    LabelPoint.setVisible(false);
    txtAmoutGiven.setVisible(false);
    LabelAmoutGiven.setVisible(false);
    LabelPayable.setVisible(false);
    btnCheck.setVisible(false);
}
    
    private void updateLabelPayable() {
    try {
        // Lấy tổng số tiền phải thanh toán
        double totalAmount = or.TotalAmount + or.TotalAmount * 0.1; 
        // Lấy số tiền đã đưa từ txtAmoutGiven
        String amountGivenText = txtAmoutGiven.getText().trim();
        double amountGiven = amountGivenText.isEmpty() ? 0 : Double.parseDouble(amountGivenText);

        // Tính số tiền còn lại phải thanh toán
        double payableAmount = amountGiven - totalAmount;
        LabelPayable.setVisible(true);
        // Cập nhật LabelPayable
        LabelPayable.setText("Payable Amount: " + payableAmount);
    } catch (NumberFormatException e) {
        // Xử lý khi giá trị không hợp lệ
        LabelPayable.setText("Payable Amount: Invalid Input");
    }
}
    
    public void displayOrderDetails() {
    // Kiểm tra xem đối tượng Order có dữ liệu hay không
    if (or != null) {
        // Hiển thị các thông tin của Order vào các label
        LabelID.setText("Order ID: " + or.OrderID);
        LabelDate.setText("Date: " + or.OrderDate);
        LabelNumber.setText("Table Number: " + or.TableNumber);
        LabelTime.setText("Time: " + or.OrderTime);
        LabelTotal.setText("Total Amount: " + or.TotalAmount);
        double amountToPay = or.TotalAmount + or.TotalAmount * 0.1;
        LabelPay.setText("Amount to Pay: " + String.valueOf(amountToPay));
        data.totalAmount = or.TotalAmount + or.TotalAmount*0.1;
    }
}
    
    public void getpaymentMethod() {
    try {
        DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) ComboBoxMethod.getModel();
        model.removeAllElements();
        
        // Thêm một lựa chọn mặc định
        model.addElement("Choice");
        // Thêm các loại thực phẩm có thể chọn
        model.addElement("Cash");
        model.addElement("Credit Card");
        model.addElement("E-Wallet");
        ComboBoxMethod.setModel(model);
    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Lỗi khi lấy phương thức thanh toán: " + e);
        System.out.println(e);
    }
    }
    
    public void createPayment() {
    try {
        // Kiểm tra phương thức thanh toán đã chọn
        String paymentMethod = (String) ComboBoxMethod.getSelectedItem();
        if (paymentMethod == null || paymentMethod.equals("Choice")) {
            JOptionPane.showMessageDialog(null, "Please select a payment method.");
            return;
        }

        // Xử lý amountGiven cho phương thức thanh toán
        if ("Cash".equals(paymentMethod)) {
            String amountGivenText = txtAmoutGiven.getText().trim();
            if (amountGivenText.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please enter the amount given.");
                return;
            }
            data.amountGiven = Double.parseDouble(amountGivenText);
        } else {
            data.amountGiven = data.totalAmount; // Nếu không phải tiền mặt, sử dụng tổng số tiền
        }

        // Chuẩn bị câu lệnh SQL cho việc chèn dữ liệu
        String sqlInsertPayment;
        Object[] params;

        if (data.userID == null) {
            sqlInsertPayment = "INSERT INTO Payments (OrderID, AmountGiven, PaymentMethod, TotalAmount) VALUES (?, ?, ?, ?)";
            params = new Object[]{or.OrderID, data.amountGiven, paymentMethod, data.totalAmount};
        } else {
            sqlInsertPayment = "INSERT INTO Payments (OrderID, AmountGiven, PaymentMethod, UserID, TotalAmount) VALUES (?, ?, ?, ?, ?)";
            params = new Object[]{or.OrderID, data.amountGiven, paymentMethod, data.userID, data.totalAmount};
        }

        Connect cn = new Connect();
        int result = cn.executeQuery(sqlInsertPayment, params);

        if (result > 0) {
            // Cập nhật trạng thái của hóa đơn thành 1
            String sqlUpdateOrderStatus = "UPDATE Orders SET Status = 1, PaymentTime = GETDATE() WHERE OrderID = ?";
            cn.executeQuery(sqlUpdateOrderStatus, new Object[]{or.OrderID});

            // Cập nhật trạng thái bàn thành 0
            String sqlUpdateTableStatus = "UPDATE Tables SET Status = 0 WHERE TableNumber = ?";
            cn.executeQuery(sqlUpdateTableStatus, new Object[]{or.TableNumber});
            
            JOptionPane.showMessageDialog(null, "Thanh toán thành công!");
            cn.close();
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(null, "Thanh toán thất bại. Vui lòng thử lại.");
            cn.close();
        }
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(null, "Định dạng số không hợp lệ: " + e.getMessage());
    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Lỗi khi tạo thanh toán: " + e.getMessage());
    }
}

    public void getCustomerInfoAndUpdateOrder(String phone) {
    // Câu lệnh SQL để lấy thông tin khách hàng
    String sqlGetCustomerInfo = "SELECT Name, MemberPoint, UserID FROM Customers WHERE Phone = ?";
    Object[] params = new Object[]{phone}; // Tham số cho câu lệnh SQL
    try {
        Connect cn = new Connect();
        // Thực hiện truy vấn
        ResultSet rs = cn.selectQuery(sqlGetCustomerInfo, params);

        if (rs.next()) {
            // Lấy thông tin khách hàng
            String name = rs.getString("Name");
            int memberPoint = rs.getInt("MemberPoint");
            int userID = rs.getInt("UserID"); // Lấy UserID

            // Cập nhật giao diện
            LabelName.setText("Name: " + name); // Hiển thị tên khách hàng
            LabelPoint.setText("Memeber Point: "+String.valueOf(memberPoint)); // Hiển thị điểm thành viên

            // Gán UserID vào đơn hàng
            data.userID = userID; // Gán UserID vào đơn hàng
            
            // Câu lệnh SQL để cập nhật UserID trong bảng Orders
            String sqlUpdateOrder = "UPDATE Orders SET UserID = ? WHERE OrderID = ?";
            Object[] updateParams = new Object[]{userID, or.OrderID}; // Tham số cho câu lệnh SQL

            // Thực hiện truy vấn cập nhật
            int result = cn.executeQuery(sqlUpdateOrder, updateParams);

            // Kiểm tra kết quả cập nhật
            if (result > 0) {
                System.out.println("UserID assigned to Order successfully.");
                LabelName.setVisible(true);
                LabelPoint.setVisible(true);
            } else {
                System.out.println("Failed to assign UserID to Order.");
            }
        } else {
            JOptionPane.showMessageDialog(null, "No customer found with this phone number.");
        }
        cn.close();
    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Error retrieving customer info: " + e.getMessage());
        System.out.println(e);
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

        jLabel1 = new javax.swing.JLabel();
        CheckBoxAccount = new javax.swing.JCheckBox();
        LabelPhone = new javax.swing.JLabel();
        txtPhone = new javax.swing.JTextField();
        LabelTotal = new javax.swing.JLabel();
        ComboBoxMethod = new javax.swing.JComboBox<>();
        btnExit = new javax.swing.JButton();
        btnComplete = new javax.swing.JButton();
        LabelAmoutGiven = new javax.swing.JLabel();
        txtAmoutGiven = new javax.swing.JTextField();
        btnCheck = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        LabelName = new javax.swing.JLabel();
        LabelPoint = new javax.swing.JLabel();
        LabelPay = new javax.swing.JLabel();
        LabelID = new javax.swing.JLabel();
        LabelDate = new javax.swing.JLabel();
        LabelNumber = new javax.swing.JLabel();
        LabelTime = new javax.swing.JLabel();
        LabelPayable = new javax.swing.JLabel();

        setClosable(true);
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);

        jLabel1.setText("Payment");

        CheckBoxAccount.setText("Have Account");
        CheckBoxAccount.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                CheckBoxAccountItemStateChanged(evt);
            }
        });

        LabelPhone.setText("Phone:");

        LabelTotal.setText("Total Amount:");

        ComboBoxMethod.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        ComboBoxMethod.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                ComboBoxMethodPopupMenuWillBecomeVisible(evt);
            }
        });
        ComboBoxMethod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ComboBoxMethodActionPerformed(evt);
            }
        });

        btnExit.setText("Exit");
        btnExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExitActionPerformed(evt);
            }
        });

        btnComplete.setText("Payment");
        btnComplete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCompleteActionPerformed(evt);
            }
        });

        LabelAmoutGiven.setText("Amount Given:");

        btnCheck.setText("Check");
        btnCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCheckActionPerformed(evt);
            }
        });

        jLabel5.setText("Payment Method:");

        LabelName.setText("Name:");

        LabelPoint.setText("Member Point:");

        LabelPay.setText("Amount to Pay:");

        LabelID.setText("Order ID:");

        LabelDate.setText("Date:");

        LabelNumber.setText("Table Number:");

        LabelTime.setText("Time:");

        LabelPayable.setText("Payable Amount:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(72, 72, 72)
                .addComponent(btnComplete)
                .addGap(91, 91, 91)
                .addComponent(btnExit))
            .addGroup(layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(LabelNumber)
                        .addGap(124, 124, 124)
                        .addComponent(LabelTime))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(LabelID)
                        .addGap(155, 155, 155)
                        .addComponent(LabelDate))))
            .addGroup(layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(LabelPayable)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(LabelTotal)
                            .addComponent(LabelAmoutGiven)
                            .addComponent(jLabel5))
                        .addGap(31, 31, 31)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtAmoutGiven, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(ComboBoxMethod, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(CheckBoxAccount)
                        .addGap(61, 61, 61)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(LabelName)
                            .addComponent(LabelPoint)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(LabelPhone)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtPhone, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnCheck, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(LabelPay, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(LabelID)
                    .addComponent(LabelDate))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(LabelNumber)
                    .addComponent(LabelTime))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(CheckBoxAccount)
                    .addComponent(LabelPhone)
                    .addComponent(txtPhone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCheck))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(LabelName)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(LabelPoint)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(LabelTotal)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(LabelPay)
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ComboBoxMethod, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(LabelAmoutGiven)
                    .addComponent(txtAmoutGiven, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(LabelPayable)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 39, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnExit)
                    .addComponent(btnComplete))
                .addGap(19, 19, 19))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void CheckBoxAccountItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_CheckBoxAccountItemStateChanged
        // TODO add your handling code here:
         if (CheckBoxAccount.isSelected()) {
        // Nếu checkbox được chọn, hiển thị các thành phần
        LabelPhone.setVisible(true);
        txtPhone.setVisible(true);
        btnCheck.setVisible(true);
        } else {
            // Nếu checkbox không được chọn, ẩn các thành phần
            LabelPhone.setVisible(false);
            txtPhone.setVisible(false);
            btnCheck.setVisible(false);
        }
    }//GEN-LAST:event_CheckBoxAccountItemStateChanged

    private void btnCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCheckActionPerformed
        // TODO add your handling code here:
        String phone = txtPhone.getText().trim();
        getCustomerInfoAndUpdateOrder(phone);
    }//GEN-LAST:event_btnCheckActionPerformed

    private void ComboBoxMethodPopupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_ComboBoxMethodPopupMenuWillBecomeVisible
        // TODO add your handling code here:
        DefaultComboBoxModel model = (DefaultComboBoxModel) ComboBoxMethod.getModel();

        // Kiểm tra nếu "Choice" tồn tại và loại bỏ nó
        int index = model.getIndexOf("Choice");
        if (index != -1) {
            model.removeElementAt(index);
        }

        // Đặt lại mô hình cho ComboBoxMethod
        ComboBoxMethod.setModel(model);
    }//GEN-LAST:event_ComboBoxMethodPopupMenuWillBecomeVisible

    private void ComboBoxMethodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ComboBoxMethodActionPerformed
        // TODO add your handling code here:
        String selectedMethod = (String) ComboBoxMethod.getSelectedItem();
        if ("Cash".equals(selectedMethod)) {
        LabelAmoutGiven.setVisible(true);
        txtAmoutGiven.setVisible(true);
    }
        else{
        LabelAmoutGiven.setVisible(false);
        txtAmoutGiven.setVisible(false);
        }
    }//GEN-LAST:event_ComboBoxMethodActionPerformed

    private void btnCompleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCompleteActionPerformed
        // TODO add your handling code here:
        createPayment();
    }//GEN-LAST:event_btnCompleteActionPerformed

    private void btnExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExitActionPerformed
        // TODO add your handling code here:
        this.dispose();
    }//GEN-LAST:event_btnExitActionPerformed

    /**
     * @param args the command line arguments
     */
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox CheckBoxAccount;
    private javax.swing.JComboBox<String> ComboBoxMethod;
    private javax.swing.JLabel LabelAmoutGiven;
    private javax.swing.JLabel LabelDate;
    private javax.swing.JLabel LabelID;
    private javax.swing.JLabel LabelName;
    private javax.swing.JLabel LabelNumber;
    private javax.swing.JLabel LabelPay;
    private javax.swing.JLabel LabelPayable;
    private javax.swing.JLabel LabelPhone;
    private javax.swing.JLabel LabelPoint;
    private javax.swing.JLabel LabelTime;
    private javax.swing.JLabel LabelTotal;
    private javax.swing.JButton btnCheck;
    private javax.swing.JButton btnComplete;
    private javax.swing.JButton btnExit;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JTextField txtAmoutGiven;
    private javax.swing.JTextField txtPhone;
    // End of variables declaration//GEN-END:variables
}
