/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package View;
import Database.Connect;
import Process.Order;
import Process.User;
import java.sql.ResultSet;
import java.util.Vector;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
/**
 *
 * @author mSoSm
 */
public final class frmTables extends javax.swing.JInternalFrame {

    /**
     * Creates new form Tables
     */
    public Order or;
    public User us;
    public frmTables(User data) {
        initComponents();
        btnAdd.setVisible(false);
        btnDetail.setVisible(false);
        getTable();
        us = data;
    }
    
    public void getTable() {
    try {
        Connect cn = new Connect();
        System.out.println("Connected to database successfully");

        // Gọi thủ tục UpdateTableStatus trước khi lấy dữ liệu
        String sqlProcedure = "EXEC UpdateTableStatus"; // Câu lệnh gọi thủ tục
        cn.executeQuery(sqlProcedure, new Object[0]); // Thực hiện thủ tục

        // Lấy giá trị từ jComboBox1 để xác định trạng thái bàn cần tìm
        String selectedStatus = (String) jComboBox1.getSelectedItem();
        String sql;
        Object[] argv;

        // Xây dựng câu truy vấn SQL dựa trên trạng thái được chọn
        if ("All".equals(selectedStatus)) {
            sql = "SELECT * FROM Tables WHERE Condition = 1";
            argv = new Object[0]; // Không cần điều kiện, lấy tất cả các bàn có Condition = true
        } else {
            // Lấy trạng thái bàn tương ứng
            int status;
            switch (selectedStatus) {
                case "Occupied":
                    status = 1;
                    break;
                case "Reserved":
                    status = 2;
                    break;
                default: // "Available"
                    status = 0;
                    break;
            }
            sql = "SELECT * FROM Tables WHERE Condition = 1 AND Status = ?";
            argv = new Object[]{status}; // Truyền trạng thái làm tham số cho câu truy vấn
        }

        // Thực hiện truy vấn
        try (ResultSet resultSet = cn.selectQuery(sql, argv)) {
            System.out.println("Query executed successfully");

            DefaultTableModel dt = (DefaultTableModel) tbTables.getModel();
            dt.setRowCount(0); // Xóa dữ liệu cũ trong bảng trước khi thêm dữ liệu mới

            while (resultSet.next()) {
                Vector v = new Vector();
                v.add(resultSet.getInt("TableID"));             // Mã bàn
                v.add(resultSet.getInt("TableNumber"));          // Số bàn
                v.add(resultSet.getInt("SeatingCapacity"));      // Sức chứa

                int status = resultSet.getInt("Status");         // Trạng thái bàn
                String statusText;

                // Chuyển đổi trạng thái từ số thành chuỗi để dễ hiển thị
                switch (status) {
                    case 1:
                        statusText = "Occupied";                // Có khách
                        break;
                    case 2:
                        statusText = "Reserved";                // Đã đặt trước
                        break;
                    default:
                        statusText = "Available";              // Đang trống
                        break;
                }
                v.add(statusText);                              // Thêm trạng thái bàn

                // Thêm Condition dưới dạng boolean
                v.add(resultSet.getBoolean("Condition"));       // Tình trạng bàn dưới dạng boolean (true/false)

                dt.addRow(v); // Thêm dòng vào mô hình bảng
            }

            cn.close(); // Đóng kết nối sau khi truy vấn xong
        }

    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Error retrieving tables: " + e);
        System.out.println(e);
    }
}

    private void createOrder() {
        try {
            // Lấy TableNumber từ LabelNumber
            String tableNumberStr = LabelNumber.getText().replace("Table Number:", "").trim();
            int tableNumber = Integer.parseInt(tableNumberStr); // Chuyển đổi sang số nguyên

            // Tạo câu lệnh SQL để thêm đơn hàng mới
            String sqlInsertOrder = "INSERT INTO Orders (UserID, TableID, OrderDate, Status) VALUES (?, ?, GETDATE(), ?)";
            Object[] paramsInsertOrder = new Object[]{null, tableNumber, 0}; // UserID là null và Status là 0

            // Kết nối tới cơ sở dữ liệu
            Connect cn = new Connect();
            int result = cn.executeQuery(sqlInsertOrder, paramsInsertOrder); // Thực hiện truy vấn thêm đơn hàng

            // Kiểm tra kết quả
            if (result > 0) {
                // Hiển thị thông báo tạo đơn hàng thành công
                JOptionPane.showMessageDialog(this, "Order created successfully.");

                // Lấy OrderID của đơn hàng mới tạo
                String sqlGetOrderID = "SELECT SCOPE_IDENTITY() AS OrderID";
                Object[] emptyParams = new Object[0]; // Mảng tham số trống
                ResultSet rsOrderID = cn.selectQuery(sqlGetOrderID, emptyParams);

                int orderID = -1;
                if (rsOrderID.next()) {
                    orderID = rsOrderID.getInt("OrderID");

                    // Truy vấn để lấy dữ liệu đơn hàng mới tạo
                    String sqlSelectOrder = "SELECT * FROM Orders WHERE OrderID = ?";
                    Object[] paramsSelectOrder = new Object[]{orderID};
                    ResultSet rs = cn.selectQuery(sqlSelectOrder, paramsSelectOrder);

                    // Gán dữ liệu vào biến or nếu kết quả truy vấn tồn tại
                    if (rs.next()) {
                        or = new Order();
                        or.OrderID = rs.getInt("OrderID");
                        or.UserID = rs.getInt("UserID");
                        or.TableNumber = rs.getInt("TableNumber");
                        or.TotalAmount = rs.getFloat("TotalAmount");
                        or.OrderDate = rs.getDate("OrderDate");
                        or.OrderTime = rs.getTime("OrderTime");
                        or.Status = rs.getInt("Status");
                        or.PaymentTime = rs.getTimestamp("PaymentTime");
                    }
                }

                // Cập nhật trạng thái của bàn thành 1
                String sqlUpdateTable = "UPDATE Tables SET Status = 1 WHERE TableNumber = ?";
                Object[] paramsUpdateTable = new Object[]{tableNumber};
                int updateResult = cn.executeQuery(sqlUpdateTable, paramsUpdateTable); // Thực hiện truy vấn cập nhật

                // Kiểm tra kết quả cập nhật
                if (updateResult > 0) {
                    System.out.println("Table status updated successfully.");
                    getTable();
                } else {
                    System.out.println("Failed to update table status.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Failed to create order.");
            }

            cn.close(); // Đóng kết nối

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error creating order: " + e.getMessage());
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
        jScrollPane1 = new javax.swing.JScrollPane();
        tbTables = new javax.swing.JTable();
        btnRefresh = new javax.swing.JButton();
        btnExit = new javax.swing.JButton();
        jComboBox1 = new javax.swing.JComboBox<>();
        btnAdd = new javax.swing.JButton();
        LabelNumber = new javax.swing.JLabel();
        LabelSeating = new javax.swing.JLabel();
        LabelStatus = new javax.swing.JLabel();
        btnDetail = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setText("Table");

        tbTables.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "ID", "Table Number", "Seating Capacity", "Status"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        tbTables.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbTablesMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tbTables);

        btnRefresh.setText("Refresh");
        btnRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshActionPerformed(evt);
            }
        });

        btnExit.setText("Exit");
        btnExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExitActionPerformed(evt);
            }
        });

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All", "Occupied", "Reserved", "Available" }));
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        btnAdd.setText("Create Order");
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        LabelNumber.setText("Table Number:");

        LabelSeating.setText("Seating Capacity:");

        LabelStatus.setText("Status:");

        btnDetail.setText("Detail");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(215, 215, 215))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addComponent(btnAdd)
                        .addGap(44, 44, 44))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(LabelNumber)
                        .addGap(26, 26, 26)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(LabelSeating)
                        .addGap(61, 61, 61)
                        .addComponent(LabelStatus))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnRefresh)
                        .addGap(43, 43, 43)
                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(36, 36, 36)
                        .addComponent(btnExit)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnDetail)
                        .addGap(35, 35, 35))))
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 525, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(47, 47, 47)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 413, Short.MAX_VALUE)
                .addGap(45, 45, 45)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(LabelNumber)
                    .addComponent(LabelSeating)
                    .addComponent(LabelStatus)
                    .addComponent(btnDetail))
                .addGap(56, 56, 56)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnExit)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRefresh)
                    .addComponent(btnAdd))
                .addGap(16, 16, 16))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        // TODO add your handling code here:
        getTable();
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        // TODO add your handling code here:
        LabelNumber.setText("Table Number:");
        LabelSeating.setText("Seating Capacity:");
        LabelStatus.setText("Status:");
        jComboBox1.setSelectedItem("All");
        getTable();
        btnAdd.setVisible(false);
        btnDetail.setVisible(false);
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void tbTablesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbTablesMouseClicked
        btnAdd.setVisible(false);
        btnDetail.setVisible(false);
        int i = tbTables.getSelectedRow(); // Lấy chỉ số hàng được chọn
    // Lấy giá trị từ các cột trong hàng được chọn
        String tableNumber = tbTables.getValueAt(i, 1).toString();      // Lấy TableNumber
        String  seatingCapacity = tbTables.getValueAt(i, 2).toString();  // Lấy SeatingCapacity
        String status = tbTables.getValueAt(i, 3).toString();           // Lấy trạng thái bàn
        LabelNumber.setText("Table Number:" + tableNumber);
        LabelSeating.setText("Seating Capacity:"+ seatingCapacity);
        if (status.equals("Reserved")){
            btnDetail.setVisible(true);
        }
        if (status.equals("Available")){
            btnAdd.setVisible(true);
        }
        LabelStatus.setText("Status:" + status);    
    }//GEN-LAST:event_tbTablesMouseClicked

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        // TODO add your handling code here:
        createOrder();
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExitActionPerformed
        // TODO add your handling code here:
        this.dispose();
    }//GEN-LAST:event_btnExitActionPerformed

    /**
     * @param args the command line arguments
     */

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel LabelNumber;
    private javax.swing.JLabel LabelSeating;
    private javax.swing.JLabel LabelStatus;
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnDetail;
    private javax.swing.JButton btnExit;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tbTables;
    // End of variables declaration//GEN-END:variables
}
