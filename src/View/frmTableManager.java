/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package View;

import Database.Connect;
import java.sql.ResultSet;
import java.util.Vector;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author mSoSm
 */
public final class frmTableManager extends javax.swing.JInternalFrame {

    /**
     * Creates new form TableManager
     */
    public frmTableManager() {
        initComponents();
        getTables();
    }

    public void getTables() {
    try {
        DefaultTableModel dt = (DefaultTableModel) tbTables.getModel();
        dt.setRowCount(0); // Xóa dữ liệu cũ trong bảng trước khi thêm dữ liệu mới
        Connect cn = new Connect();
        System.out.println("Connected to database successfully");

        // Không có tham số cần truyền, nên để trống argv
        Object[] argv = new Object[0];

        // Thực hiện truy vấn để lấy dữ liệu từ bảng Tables
        try (ResultSet resultSet = cn.selectQuery("SELECT * FROM Tables", argv)) {
            System.out.println("Query executed successfully");

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
    
    private void addTables() {
    try {
        // Lấy dữ liệu từ các trường nhập
        String tableNumber = txtTableNumber.getText();
        String seatingCapacity = txtSeat.getText();
        String status = ComboBox.getSelectedItem().toString();
        boolean condition = CheckBox.isSelected();

        // Kiểm tra dữ liệu đầu vào
        if (tableNumber.isEmpty() || seatingCapacity.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng điền đủ thông tin!");
            return;
        }

        // Chuyển đổi tableNumber sang số nguyên để xử lý
        int tableNum = Integer.parseInt(tableNumber);

        // Kiểm tra xem TableNumber có tồn tại hay không
        String checkSQL = "SELECT COUNT(*) FROM Tables WHERE TableNumber = ?";
        Connect cn = new Connect();
        Object[] checkParams = { tableNum };
        ResultSet rs = cn.selectQuery(checkSQL, checkParams);
        
        if (rs.next() && rs.getInt(1) > 0) {
            JOptionPane.showMessageDialog(this, "Số bàn đã tồn tại, vui lòng chọn số bàn khác!");
            return;
        }

        // Xác định giá trị của `status` dựa trên lựa chọn (0: trống, 1: đang sử dụng, 2: đã đặt trước)
        int statusValue;
        switch (status) {
            case "Available":
                statusValue = 0;
                break;
            case "Occupied":
                statusValue = 1;
                break;
            case "Reserved":
                statusValue = 2;
                break;
            default:
                statusValue = 0;
                break;
        }

        // Tạo câu truy vấn SQL để thêm bàn vào cơ sở dữ liệu
        String insertSQL = "INSERT INTO Tables (TableNumber, SeatingCapacity, Status, Condition) VALUES (?, ?, ?, ?)";
        
        // Thiết lập các tham số cho truy vấn
        Object[] params = {
            tableNum,                                // TableNumber
            Integer.parseInt(seatingCapacity),       // SeatingCapacity
            statusValue,                             // Status
            condition                                // Condition
        };

        // Thực hiện truy vấn thêm bàn mới
        int rowsInserted = cn.executeQuery(insertSQL, params);

        // Kiểm tra kết quả thêm vào cơ sở dữ liệu
        if (rowsInserted > 0) {
            JOptionPane.showMessageDialog(this, "Thêm bàn mới thành công!");
        } else {
            JOptionPane.showMessageDialog(this, "Thêm bàn mới thất bại!");
        }

        // Làm mới dữ liệu bảng hiển thị sau khi thêm thành công
        getTables();

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Lỗi khi thêm bàn: " + e.getMessage());
        System.out.println(e);
    }
}

    private void updateData() {
    try {
        // Lấy dữ liệu từ các trường nhập
        String tableNumber = txtTableNumber.getText();
        String seatingCapacity = txtSeat.getText();
        String status = ComboBox.getSelectedItem().toString();
        boolean condition = CheckBox.isSelected();

        // Kiểm tra dữ liệu đầu vào
        if (tableNumber.isEmpty() || seatingCapacity.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng điền đủ thông tin!");
            return;
        }

        // Xác định giá trị của `status` dựa trên lựa chọn (0: trống, 1: đang sử dụng, 2: đã đặt trước)
        int statusValue;
        statusValue = switch (status) {
            case "Available" -> 0;
            case "Occupied" -> 1;
            case "Reserved" -> 2;
            default -> 0;
        };

        // Lấy TableID từ LabelID để xác định bàn cần cập nhật
        int tableID = Integer.parseInt(LabelID.getText().replace("Table ID: ", "").trim());

        // Tạo câu truy vấn SQL để cập nhật dữ liệu của bàn
        String updateSQL = "UPDATE Tables SET TableNumber = ?, SeatingCapacity = ?, Status = ?, Condition = ? WHERE TableID = ?";

        // Thiết lập các tham số cho truy vấn
        Object[] params = {
            Integer.valueOf(tableNumber),           // TableNumber
            Integer.valueOf(seatingCapacity),       // SeatingCapacity
            statusValue,                             // Status
            condition,                               // Condition
            tableID                                  // TableID (để xác định bản ghi cần cập nhật)
        };

        // Thực hiện truy vấn
        Connect cn = new Connect();
        int rowsUpdated = cn.executeQuery(updateSQL, params);

        // Kiểm tra kết quả cập nhật trong cơ sở dữ liệu
        if (rowsUpdated > 0) {
            JOptionPane.showMessageDialog(this, "Cập nhật thông tin bàn thành công!");
        } else {
            JOptionPane.showMessageDialog(this, "Cập nhật thông tin bàn thất bại!");
        }

        // Làm mới dữ liệu bảng hiển thị sau khi cập nhật
        getTables();

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật bàn: " + e.getMessage());
        System.out.println(e);
    }
}

    private void updateTable() {
    try {
        // Lấy dữ liệu từ bảng tbTables
        DefaultTableModel dt = (DefaultTableModel) tbTables.getModel();
        
        // Tạo kết nối với cơ sở dữ liệu
        Connect cn = new Connect();
        
        // Duyệt qua từng hàng trong bảng
        for (int i = 0; i < dt.getRowCount(); i++) {
            // Lấy TableID và Condition từ hàng hiện tại
            int tableID = (int) dt.getValueAt(i, 0); // Giả định TableID là cột đầu tiên
            boolean condition = (boolean) dt.getValueAt(i, 4); // Giả định Condition là cột thứ năm

            // Truy vấn để lấy giá trị Condition hiện tại trong cơ sở dữ liệu
            String query = "SELECT Condition FROM Tables WHERE TableID = ?";
            Object[] params = {tableID};
            ResultSet resultSet = cn.selectQuery(query, params);
            
            if (resultSet.next()) {
                // Lấy giá trị Condition từ cơ sở dữ liệu
                boolean dbCondition = resultSet.getBoolean("Condition");
                
                // Nếu giá trị Condition không khớp, cập nhật lại trong cơ sở dữ liệu
                if (dbCondition != condition) {
                    String updateSQL = "UPDATE Tables SET Condition = ? WHERE TableID = ?";
                    Object[] updateParams = {condition, tableID};
                    cn.executeQuery(updateSQL, updateParams);
                }
            }
        }

        // Đóng kết nối và làm mới bảng
        cn.close();
        getTables(); // Gọi lại hàm để làm mới dữ liệu bảng

        JOptionPane.showMessageDialog(this, "Cập nhật thành công!");

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật trạng thái bàn: " + e.getMessage());
        System.out.println(e);
    }
}

    private void refreshTables() {
    // Gọi hàm getTables() để tải lại dữ liệu
    getTables();

    // Đặt lại các trường nhập liệu và trạng thái
    LabelID.setText("Table ID:"); // Đặt lại LabelID
    txtTableNumber.setText("");   // Xóa nội dung trường số bàn
    txtSeat.setText("");          // Xóa nội dung trường số lượng chỗ ngồi
    CheckBox.setSelected(true);   // Đặt lại trạng thái của checkbox (có thể là trạng thái mặc định)
    ComboBox.setSelectedIndex(0); // Đặt lại ComboBox về trạng thái mặc định
}

    private void searchTables() {
    try {
        // Lấy từ khóa tìm kiếm từ txtSearch
        String keyword = txtSearch.getText().trim();
        
        // Kiểm tra nếu không có từ khóa nào thì thông báo
        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập từ khóa tìm kiếm!");
            return;
        }

        // Tạo kết nối đến cơ sở dữ liệu
        Connect cn = new Connect();
        
        // Câu truy vấn SQL để tìm kiếm trong bảng Tables dựa trên TableNumber, SeatingCapacity, Status, và Condition
        String searchSQL = """
            SELECT * FROM Tables 
            WHERE TableNumber LIKE ? 
                OR SeatingCapacity LIKE ? 
                OR (Status = ?)
                OR (Condition = ?)
            """;
        
        // Xác định giá trị status dựa trên từ khóa
        Integer statusValue = switch (keyword.toLowerCase()) {
            case "occupied" -> 1;
            case "reserved" -> 2;
            case "available" -> 0;
            default -> null;  // Không khớp với giá trị trạng thái nào, mặc định để null
        };
        
        // Xác định giá trị condition dựa trên từ khóa
        Boolean conditionValue = switch (keyword.toLowerCase()) {
            case "true" -> true;
            case "false" -> false;
            default -> null;
        };
        
        // Thêm tham số cho câu truy vấn
        Object[] params = {
            "%" + keyword + "%",                   // TableNumber
            "%" + keyword + "%",                   // SeatingCapacity
            statusValue,                           // Status theo từ khóa
            conditionValue                          // Condition theo từ khóa (nếu có)
        };
        
        // Thực hiện truy vấn và lấy kết quả
        ResultSet resultSet = cn.selectQuery(searchSQL, params);
        
        // Xóa dữ liệu cũ trong bảng
        DefaultTableModel dt = (DefaultTableModel) tbTables.getModel();
        dt.setRowCount(0);
        
        // Duyệt qua kết quả truy vấn và thêm vào bảng
        while (resultSet.next()) {
            Vector v = new Vector();
            v.add(resultSet.getInt("TableID"));
            v.add(resultSet.getInt("TableNumber"));
            v.add(resultSet.getInt("SeatingCapacity"));

            int status = resultSet.getInt("Status");
            String statusText = switch (status) {
                case 1 -> "Occupied";
                case 2 -> "Reserved";
                default -> "Available";
            };
            v.add(statusText);
            v.add(resultSet.getBoolean("Condition"));

            dt.addRow(v); // Thêm dòng vào mô hình bảng
        }

        cn.close(); // Đóng kết nối sau khi hoàn tất

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Lỗi khi tìm kiếm: " + e.getMessage());
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
        LabelID = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        txtTableNumber = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        txtSeat = new javax.swing.JTextField();
        ComboBox = new javax.swing.JComboBox<>();
        CheckBox = new javax.swing.JCheckBox();
        jLabel6 = new javax.swing.JLabel();
        btnDetail = new javax.swing.JButton();
        btnAdd = new javax.swing.JButton();
        btnUpdateTable = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();
        btnExit = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jButton5 = new javax.swing.JButton();
        txtSearch = new javax.swing.JTextField();
        btnUpdateData = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setText("Table Manager");

        tbTables.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "ID", "Table Number", "Seating Capacity", "Status", "Condition"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tbTables.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbTablesMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tbTables);

        LabelID.setText("Table ID:");

        jLabel3.setText("Table Number:");

        jLabel4.setText("Seating Capacity:");

        jLabel5.setText("Status:");

        ComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Reserved", "Occupied", "Available" }));

        CheckBox.setText("Condition");

        jLabel6.setText("Reservation:");

        btnDetail.setText("Detail");

        btnAdd.setText("Add");
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        btnUpdateTable.setText("Update Table");
        btnUpdateTable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateTableActionPerformed(evt);
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

        jLabel2.setText("Search:");

        jButton5.setText("Search");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        btnUpdateData.setText("Update Data");
        btnUpdateData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateDataActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 465, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGroup(layout.createSequentialGroup()
                .addGap(56, 56, 56)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton5))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(20, 20, 20))))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnAdd)
                    .addComponent(btnUpdateData)))
            .addGroup(layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(LabelID)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtTableNumber))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addGap(18, 18, 18)
                        .addComponent(btnDetail)))
                .addGap(28, 28, 28)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(CheckBox, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtSeat, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE))))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(200, 200, 200)
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(48, 48, 48))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(btnUpdateTable)
                        .addGap(52, 52, 52)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnRefresh)
                    .addComponent(ComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnExit)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(11, 11, 11)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton5)
                        .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(LabelID)
                        .addGap(28, 28, 28)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtTableNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5))
                        .addGap(35, 35, 35)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(btnDetail)
                            .addComponent(CheckBox)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(txtSeat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(26, 26, 26)
                        .addComponent(ComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(28, 28, 28)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAdd)
                    .addComponent(btnRefresh)
                    .addComponent(btnUpdateTable))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnUpdateData)
                        .addGap(10, 10, 10))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(btnExit)
                        .addContainerGap())))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void tbTablesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbTablesMouseClicked
        // TODO add your handling code here:  
    int i = tbTables.getSelectedRow(); // Lấy chỉ số hàng được chọn
    // Lấy giá trị từ các cột trong hàng được chọn
    String tableID = tbTables.getValueAt(i, 0).toString();          // Lấy TableID
    String tableNumber = tbTables.getValueAt(i, 1).toString();      // Lấy TableNumber
    String seatingCapacity = tbTables.getValueAt(i, 2).toString();  // Lấy SeatingCapacity
    String status = tbTables.getValueAt(i, 3).toString();           // Lấy trạng thái bàn
    boolean condition = (boolean) tbTables.getValueAt(i, 4);        // Lấy Condition dưới dạng boolean

    // Cập nhật các thành phần giao diện
    LabelID.setText("Table ID: " + tableID);             // Hiển thị TableID
    txtTableNumber.setText(tableNumber);                 // Hiển thị TableNumber trong txtTableNumber
    txtSeat.setText(seatingCapacity);                    // Hiển thị SeatingCapacity trong txtSeat
    ComboBox.setSelectedItem(status);                    // Cập nhật JComboBox cho trạng thái bàn
    CheckBox.setSelected(condition);                     // Cập nhật CheckBox cho Condition (trạng thái khả dụng)
        
    }//GEN-LAST:event_tbTablesMouseClicked

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        // TODO add your handling code here:
        addTables();
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        // TODO add your handling code here:
        refreshTables();
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void btnExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExitActionPerformed
        // TODO add your handling code here:
        this.dispose();
    }//GEN-LAST:event_btnExitActionPerformed

    private void btnUpdateTableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateTableActionPerformed
        // TODO add your handling code here:
        updateTable();
    }//GEN-LAST:event_btnUpdateTableActionPerformed

    private void btnUpdateDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateDataActionPerformed
        // TODO add your handling code here:
        updateData();
    }//GEN-LAST:event_btnUpdateDataActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // TODO add your handling code here:
        searchTables();
    }//GEN-LAST:event_jButton5ActionPerformed

    /**
     * @param args the command line arguments
     */
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox CheckBox;
    private javax.swing.JComboBox<String> ComboBox;
    private javax.swing.JLabel LabelID;
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnDetail;
    private javax.swing.JButton btnExit;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnUpdateData;
    private javax.swing.JButton btnUpdateTable;
    private javax.swing.JButton jButton5;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tbTables;
    private javax.swing.JTextField txtSearch;
    private javax.swing.JTextField txtSeat;
    private javax.swing.JTextField txtTableNumber;
    // End of variables declaration//GEN-END:variables
}
