    /*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package View;
import Database.Connect;
import Process.User;
import Process.Order;
import java.sql.SQLException;
import java.util.Vector;
import java.sql.ResultSet;
import java.util.Date;
import java.sql.Timestamp;
import javax.swing.table.DefaultTableModel;
import javax.swing.RowFilter;
import javax.swing.table.TableRowSorter;
import javax.swing.JOptionPane;

/**
 *
 * @author mSoSm
 */
public class frmOrder extends javax.swing.JInternalFrame {

    public Order data;
    public User us;
    private TableRowSorter<DefaultTableModel> sorter;
    
    public void getOrders(String dateCondition, boolean checkCompletedQuantity, Integer userID) {
    Connect cn = null;
    ResultSet resultSet = null;

    try {
        cn = new Connect();
        System.out.println("Kết nối tới cơ sở dữ liệu thành công");

        DefaultTableModel dt = (DefaultTableModel) tbOrder.getModel();
        dt.setRowCount(0);

        // Câu truy vấn cơ bản với từ khóa DISTINCT
        StringBuilder query = new StringBuilder("SELECT DISTINCT Orders.OrderID, Orders.OrderDate, Tables.TableNumber, Orders.Status, Orders.TotalAmount ");
        query.append("FROM Orders ");
        query.append("JOIN Tables ON Orders.TableID = Tables.TableID "); // Đổi từ TableNumber sang TableID

        // Điều chỉnh câu truy vấn dựa trên các điều kiện
        if (checkCompletedQuantity) {
            query.append("JOIN OrderDetails ON Orders.OrderID = OrderDetails.OrderID "); // Sửa ở đây
        }
        query.append("WHERE Tables.Status = 1 AND Tables.Condition = 1 ");
        if (dateCondition != null) {
            query.append("AND CAST(Orders.OrderDate AS DATE) = ").append(dateCondition).append(" ");
        }
        if (checkCompletedQuantity) {
            query.append("AND OrderDetails.CompletedQuantity <> OrderDetails.Quantity "); // Sửa ở đây
        }
        if (userID != null) {
            query.append("AND Orders.UserID = ? ");
        }

        // Thực hiện truy vấn với các tham số
        resultSet = userID != null ? cn.selectQuery(query.toString(), new Object[]{userID}) : cn.selectQuery(query.toString(), new Object[0]);
        System.out.println("Truy vấn thực thi thành công");

        // Đưa dữ liệu vào model của bảng
        while (resultSet.next()) {
            Vector<Object> row = new Vector<>();
            row.add(resultSet.getInt("OrderID"));

            java.sql.Timestamp orderTimestamp = resultSet.getTimestamp("OrderDate");
            row.add(new java.sql.Date(orderTimestamp.getTime()));
            row.add(new java.sql.Time(orderTimestamp.getTime()));
            row.add(resultSet.getInt("TableNumber")); // Lấy TableNumber từ bảng Tables thông qua TableID

            int status = resultSet.getInt("Status");
            row.add(status == 1 ? "Complete" : "In Process");
            row.add(resultSet.getFloat("TotalAmount"));

            dt.addRow(row);
        }
        System.out.println("Dữ liệu được lấy thành công");

    } catch (SQLException sqlEx) {
        JOptionPane.showMessageDialog(null, "Lỗi SQL: " + sqlEx.getMessage());
        sqlEx.printStackTrace();
    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Lỗi khi lấy thông tin đơn hàng: " + e.getMessage());
        e.printStackTrace();
    } finally {
        try {
            if (resultSet != null) resultSet.close();
            if (cn != null) cn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

    public void getOrder() {
    try {
        Connect cn = new Connect();
        System.out.println("Connected to database successfully");

        // Tạo một DefaultTableModel để lưu trữ dữ liệu
        DefaultTableModel dt = (DefaultTableModel) tbOrder.getModel();
        dt.setRowCount(0); // Đặt lại số dòng về 0 trước khi thêm dữ liệu

        // Lấy ngày từ jDateChooser1
        java.util.Date selectedDate = jDateChooser1.getDate();
        java.sql.Date sqlDate = new java.sql.Date(selectedDate.getTime()); // Chuyển đổi thành java.sql.Date

        // Lấy giá trị của cbBoxStatus và xác định điều kiện truy vấn cho trạng thái
        String statusCondition = "";
        String selectedStatus = (String) cbBoxStatus.getSelectedItem();
        if ("In Process".equals(selectedStatus)) {
            statusCondition = " AND Orders.Status = 0";
        } else if ("Completed".equals(selectedStatus)) {
            statusCondition = " AND Orders.Status = 1";
        }

        // Truy vấn để lấy dữ liệu từ bảng Orders và Tables với điều kiện ngày và trạng thái
        String query = "SELECT Orders.OrderID, Orders.OrderDate, Tables.TableNumber, Orders.Status, Orders.TotalAmount " +
                       "FROM Orders " +
                       "JOIN Tables ON Orders.TableID = Tables.TableID " +
                       "WHERE CAST(Orders.OrderDate AS DATE) = ?";
        if (!statusCondition.isEmpty()) {
            query += statusCondition;
        }

        // Thực thi câu truy vấn với tham số sqlDate
        try (ResultSet resultSet = cn.selectQuery(query, new Object[]{sqlDate})) {
            System.out.println("Query executed successfully");

            // Duyệt qua ResultSet và thêm dữ liệu vào model
            while (resultSet.next()) {
                Vector<Object> v = new Vector<>();

                // Thêm OrderID vào model
                v.add(resultSet.getInt("OrderID")); // OrderID

                // Tách Date và Time từ OrderDate
                java.sql.Timestamp orderTimestamp = resultSet.getTimestamp("OrderDate");
                java.sql.Date orderDate = new java.sql.Date(orderTimestamp.getTime());
                java.sql.Time orderTime = new java.sql.Time(orderTimestamp.getTime());

                v.add(orderDate); // Chỉ ngày từ OrderDate
                v.add(orderTime); // Chỉ giờ từ OrderDate

                v.add(resultSet.getInt("TableNumber")); // Hiển thị TableNumber thay vì TableID

                // Chuyển đổi trạng thái từ số sang chuỗi (lấy từ Orders.Status)
                int status = resultSet.getInt("Status");
                String statusString = (status == 1) ? "Complete" : "In Process";
                v.add(statusString); // Thêm trạng thái vào model

                v.add(resultSet.getFloat("TotalAmount")); // Tổng tiền

                dt.addRow(v); // Thêm dòng mới vào model
            }
        }

        // Đóng kết nối
        cn.close();
        System.out.println("Data fetched successfully");
    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Lỗi khi lấy thông tin đơn hàng: " + e);
        System.out.println(e);
    }
}
    private void setDefaultDate() {
        jDateChooser1.setDate(new Date()); // Đặt ngày của jDateChooser1 là ngày hiện tại
    }
    
    public void getCombobox() {
        // Thêm các giá trị trực tiếp vào JComboBox
        cbBoxStatus.addItem("All");
        cbBoxStatus.addItem("Completed");
        cbBoxStatus.addItem("In Process");

        // Đặt giá trị mặc định là "In Process"
        cbBoxStatus.setSelectedItem("In Process");
        
    }
    
    public void receivedata(User data) {
    us = data;
    System.out.println("username: " + us.username);
    System.out.println("role: " + us.role);

    switch (us.role) {
        case "Customer" -> {
            // Dành cho khách hàng, lấy tất cả các đơn hàng của UserID
            getOrders(null, false, us.UserID);
            jDateChooser1.setVisible(false);
            cbBoxStatus.setVisible(false);
            btnShow.setVisible(false);
        }
        case "Chef" -> {
            // Dành cho đầu bếp, chỉ lấy các đơn hàng có CompletedQuantity <> Quantity trong ngày
            getOrders("CAST(GETDATE() AS DATE)", true, null);
            jDateChooser1.setVisible(false);
            cbBoxStatus.setVisible(false);
            btnShow.setVisible(false);
        }
        default -> {
            // Dành cho các vai trò khác, lấy tất cả các đơn hàng trong ngày
            getOrders("CAST(GETDATE() AS DATE)", false, null);
        }
    }
}
    
    public frmOrder() {
        initComponents();
        setDefaultDate();
        getCombobox();
        sorter = new TableRowSorter<>((DefaultTableModel) tbOrder.getModel()); // Khởi tạo sorter
        tbOrder.setRowSorter(sorter); // Gán sorter cho tbMenu
    }
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        Order = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tbOrder = new javax.swing.JTable();
        jDateChooser1 = new com.toedter.calendar.JDateChooser();
        jLabel2 = new javax.swing.JLabel();
        btnSearch = new javax.swing.JButton();
        txtSearch = new javax.swing.JTextField();
        btnShow = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();
        btnExit = new javax.swing.JButton();
        cbBoxStatus = new javax.swing.JComboBox<>();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        tbOrder.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Order ID", "Date", "Time", "Table Number", "Status", "Total Amount"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.String.class, java.lang.Float.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tbOrder.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbOrderMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tbOrder);

        jLabel2.setText("Search:");

        btnSearch.setText("Search");
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });

        btnShow.setText("Show");
        btnShow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnShowActionPerformed(evt);
            }
        });

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

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 549, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(101, 101, 101)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 189, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSearch)
                .addContainerGap(131, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(btnShow)
                .addGap(147, 147, 147)
                .addComponent(btnRefresh)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnExit)
                .addGap(24, 24, 24))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addComponent(jDateChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(cbBoxStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(83, 83, 83))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSearch)
                    .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 413, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jDateChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbBoxStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(36, 36, 36)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnRefresh)
                    .addComponent(btnExit)
                    .addComponent(btnShow))
                .addContainerGap(69, Short.MAX_VALUE))
        );

        Order.addTab("Order", jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(Order)
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(Order, javax.swing.GroupLayout.PREFERRED_SIZE, 682, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnShowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShowActionPerformed
        getOrder();
    }//GEN-LAST:event_btnShowActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        txtSearch.setText("");
        switch (us.role) {
        case "Customer" -> {
            // Dành cho khách hàng, lấy tất cả các đơn hàng của UserID
            getOrders(null, false, us.UserID);
            jDateChooser1.setVisible(false);
            cbBoxStatus.setVisible(false);
            btnShow.setVisible(false);
        }
        case "Chef" -> {
            // Dành cho đầu bếp, chỉ lấy các đơn hàng có CompletedQuantity = Quantity trong ngày
            getOrders("CAST(GETDATE() AS DATE)", true, null);
            jDateChooser1.setVisible(false);
            cbBoxStatus.setVisible(false);
            btnShow.setVisible(false);
        }
        default -> {
            // Dành cho các vai trò khác, lấy tất cả các đơn hàng trong ngày
            getOrders("CAST(GETDATE() AS DATE)", false, null);
        }
    }
    cbBoxStatus.setSelectedItem("In Process");
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void btnExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExitActionPerformed
        // TODO add your handling code here:
         this.dispose();
    }//GEN-LAST:event_btnExitActionPerformed

    private void tbOrderMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbOrderMouseClicked
    if (evt.getClickCount() == 2) { // Kiểm tra xem có phải là click đúp không
    int selectedRow = tbOrder.getSelectedRow(); // Lấy chỉ số dòng được chọn
    data = new Order();
    if (selectedRow != -1) { // Kiểm tra xem có hàng nào được chọn không
        int columnCount = tbOrder.getColumnCount(); // Lấy số cột của bảng

        // Lấy dữ liệu từ hàng đã chọn
        data.OrderID = (Integer) tbOrder.getValueAt(selectedRow, 0); // OrderID
        data.OrderDate = (java.util.Date) tbOrder.getValueAt(selectedRow, 1); // OrderDate
        data.OrderTime = (java.util.Date) tbOrder.getValueAt(selectedRow, 2); // OrderTime
        data.TableNumber = (Integer) tbOrder.getValueAt(selectedRow, 3); // TableNumber
        String statusString = (String) tbOrder.getValueAt(selectedRow, 4); // Status
        if (statusString.equals("Complete"))
                data.Status = 1;
        else 
            data.Status = 0;
        data.TotalAmount = (Float) tbOrder.getValueAt(selectedRow, 5); // TotalAmount

        // Kiểm tra xem tab đã tồn tại chưa
        boolean tabExists = false;
        for (int i = 0; i < Order.getTabCount(); i++) {
            if (Order.getTitleAt(i).equals("Table Number: " + data.TableNumber)) {
                Order.setSelectedIndex(i);
                tabExists = true;
                break;
            }
        }
        // Nếu tab chưa tồn tại, tạo tab mới
        if (!tabExists) {
            // Tạo đối tượng OrderDetail với đầy đủ thông tin
            frmOrderDetail orderDetail = new frmOrderDetail(data,us); // Truyền đầy đủ thông tin đơn hàng vào

            // Thêm panel OrderDetail vào TabbedPane với tiêu đề là số bàn
            Order.addTab("Table Number: " + data.TableNumber, orderDetail);
            Order.setSelectedComponent(orderDetail); // Chọn tab vừa thêm
        }
    } else {
        JOptionPane.showMessageDialog(null, "Chưa chọn đơn hàng nào!");
    }
}
    }//GEN-LAST:event_tbOrderMouseClicked

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        // TODO add your handling code here:
    String searchText = txtSearch.getText().trim(); // Lấy văn bản tìm kiếm
    System.out.print(searchText);
    DefaultTableModel dt = (DefaultTableModel) tbOrder.getModel();
    
    if (us.role.equals("Customer") || us.role.equals("Chef")) {
        // Lọc dữ liệu sẵn có trong bảng `tbOrder` với Customer và Chef
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(dt);
        tbOrder.setRowSorter(sorter);

        if (searchText.isEmpty()) {
            sorter.setRowFilter(null); // Nếu ô tìm kiếm rỗng, hiện tất cả hàng
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchText)); // Tìm kiếm không phân biệt hoa thường
        }
        
    } else {
        // Vai trò khác sẽ truy vấn trực tiếp vào database với từ khóa tìm kiếm
        Connect cn = null;
        ResultSet resultSet = null;

        try {
            cn = new Connect(); // Tạo kết nối với database
            dt.setRowCount(0); // Xóa các dòng hiện tại trong bảng

            // Truy vấn SQL lấy dữ liệu theo điều kiện tìm kiếm
            String query = "SELECT Orders.OrderID, Orders.OrderDate, Orders.TableID, Orders.Status, Orders.TotalAmount " +
                           "FROM Orders " +
                           "WHERE (CAST(Orders.OrderID AS CHAR) LIKE ? OR Orders.TableID LIKE ? OR Orders.TotalAmount LIKE ?)";
            
            // Tạo mảng tham số điều kiện tìm kiếm
            Object[] finalParams = new Object[] {
                "%" + searchText + "%",
                "%" + searchText + "%",
                "%" + searchText + "%"
            };

            // Thực hiện truy vấn với `selectQuery`
            resultSet = cn.selectQuery(query, finalParams);

            // Thêm dữ liệu từ ResultSet vào bảng `tbOrder`
            while (resultSet.next()) {
                Vector<Object> row = new Vector<>();
                row.add(resultSet.getInt("OrderID"));
                
                java.sql.Timestamp orderTimestamp = resultSet.getTimestamp("OrderDate");
                java.sql.Date orderDate = new java.sql.Date(orderTimestamp.getTime());
                java.sql.Time orderTime = new java.sql.Time(orderTimestamp.getTime());
                
                row.add(orderDate);
                row.add(orderTime);
                row.add(resultSet.getInt("TableID"));
                
                int status = resultSet.getInt("Status");
                String statusString = (status == 1) ? "Complete" : "In Process";
                row.add(statusString);
                
                row.add(resultSet.getFloat("TotalAmount"));
                dt.addRow(row);
            }
        } catch (SQLException sqlEx) {
            JOptionPane.showMessageDialog(null, "Lỗi SQL: " + sqlEx.getMessage());
            sqlEx.printStackTrace();
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (cn != null) cn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    }//GEN-LAST:event_btnSearchActionPerformed

    /**
     * @param args the command line arguments
     */

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane Order;
    private javax.swing.JButton btnExit;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnShow;
    private javax.swing.JComboBox<String> cbBoxStatus;
    private com.toedter.calendar.JDateChooser jDateChooser1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tbOrder;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
