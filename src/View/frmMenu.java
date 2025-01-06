/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package View;

import Database.Connect;
import Interface.QuantityEditorRenderer1;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.Vector;
import javax.swing.*;
import javax.swing.RowFilter;
import javax.swing.table.TableRowSorter;
import javax.swing.table.*;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;


/**
 *
 * @author mSoSm
 */
public class frmMenu extends javax.swing.JInternalFrame {

    /**
     * Creates new form frmMenu
     */
    private int id;
    public boolean isAddedSuccessfully;
    private TableRowSorter<DefaultTableModel> sorter;
    public frmMenu() {
        initComponents();
        getMenu();
        btnAdd.setVisible(false);
        btnShow.setVisible(false);
        sorter = new TableRowSorter<>((DefaultTableModel) tbMenu.getModel()); // Khởi tạo sorter
        tbMenu.setRowSorter(sorter); // Gán sorter cho tbMenu
    }
    
    public void hideTable(){
    tbMenu.getColumnModel().getColumn(tbMenu.getColumnCount() - 1).setMinWidth(0);
    tbMenu.getColumnModel().getColumn(tbMenu.getColumnCount() - 1).setMaxWidth(0);
    tbMenu.getColumnModel().getColumn(tbMenu.getColumnCount() - 1).setPreferredWidth(0);
    }
    
    public void receiveData(int OrderID){
        id = OrderID;
        jButton1.setVisible(false);
        btnAdd.setVisible(true);
        btnShow.setVisible(true);
    }
    
    public void exportTableToExcel(JTable table) {
    // Lấy TableModel của JTable
    TableModel model = table.getModel();

    // Tạo một Workbook đơn giản với HSSFWorkbook (dành cho định dạng Excel 2003 và cũ hơn)
    org.apache.poi.hssf.usermodel.HSSFWorkbook workbook = new org.apache.poi.hssf.usermodel.HSSFWorkbook();
    
    // Tạo một Sheet trong Workbook
    org.apache.poi.hssf.usermodel.HSSFSheet sheet = workbook.createSheet("Sheet 1");

    // Tạo tiêu đề cột từ JTable
    org.apache.poi.hssf.usermodel.HSSFRow headerRow = sheet.createRow(0);
    for (int i = 0; i < model.getColumnCount(); i++) {
        org.apache.poi.hssf.usermodel.HSSFCell cell = headerRow.createCell(i);
        cell.setCellValue(model.getColumnName(i));
    }

    // Lặp qua các hàng dữ liệu của JTable và thêm vào Sheet
    for (int rowIndex = 0; rowIndex < model.getRowCount(); rowIndex++) {
        org.apache.poi.hssf.usermodel.HSSFRow row = sheet.createRow(rowIndex + 1);
        for (int colIndex = 0; colIndex < model.getColumnCount(); colIndex++) {
            org.apache.poi.hssf.usermodel.HSSFCell cell = row.createCell(colIndex);
            Object value = model.getValueAt(rowIndex, colIndex);
            if (value != null) {
                cell.setCellValue(value.toString());
            }
        }
    }

    // Sử dụng JFileChooser để cho phép người dùng chọn thư mục và tên file
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Chọn nơi lưu file Excel");
    fileChooser.setSelectedFile(new File("table_data.xls")); // Đặt tên mặc định cho file

    // Hiển thị cửa sổ chọn file và kiểm tra nếu người dùng đã chọn file
    int userSelection = fileChooser.showSaveDialog(null);
    if (userSelection == JFileChooser.APPROVE_OPTION) {
        // Lấy đường dẫn của file người dùng chọn
        File fileToSave = fileChooser.getSelectedFile();

        // Kiểm tra lại nếu file đã có đuôi ".xls", nếu không thì thêm vào
        if (!fileToSave.getAbsolutePath().endsWith(".xls")) {
            fileToSave = new File(fileToSave.getAbsolutePath() + ".xls");
        }

        // Lưu Workbook vào file Excel đã chọn
        try (FileOutputStream fileOut = new FileOutputStream(fileToSave)) {
            workbook.write(fileOut);
            JOptionPane.showMessageDialog(null, "Dữ liệu đã được xuất ra file Excel thành công!");
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Có lỗi xảy ra khi xuất dữ liệu!");
        }
    }
}
    
    public void addSelectedDishesToOrderDetails() {
    try {
        Connect cn = new Connect();
        DefaultTableModel dt = (DefaultTableModel) tbMenu.getModel();

        for (int row = 0; row < dt.getRowCount(); row++) {
            int quantity = (int) dt.getValueAt(row, 5); // Giả sử cột 5 chứa số lượng
            if (quantity > 0) { // Chỉ thêm những món có số lượng lớn hơn 0
                int dishID = (int) dt.getValueAt(row, 0); // Giả sử cột 0 chứa DishID

                // Kiểm tra nếu bản ghi với OrderID và DishID đã tồn tại
                String checkExistenceSQL = "SELECT Quantity FROM OrderDetails WHERE OrderID = ? AND DishID = ?";
                Object[] checkParams = new Object[] { id, dishID };
                ResultSet resultSet = cn.selectQuery(checkExistenceSQL, checkParams);

                if (resultSet.next()) {
                    // Nếu bản ghi đã tồn tại, cập nhật Quantity
                    int existingQuantity = resultSet.getInt("Quantity");
                    int newQuantity = existingQuantity + quantity;

                    String updateSQL = "UPDATE OrderDetails SET Quantity = ? WHERE OrderID = ? AND DishID = ?";
                    Object[] updateParams = new Object[] { newQuantity, id, dishID };
                    int updateResult = cn.executeQuery(updateSQL, updateParams);

                    if (updateResult > 0) {
                        System.out.println("Đã cập nhật số lượng của DishID: " + dishID + " trong OrderDetails.");
                    }
                } else {
                    // Nếu bản ghi chưa tồn tại, thêm bản ghi mới
                    String insertOrderDetailsSQL = "INSERT INTO OrderDetails (OrderID, DishID, Quantity) VALUES (?, ?, ?)";
                    Object[] insertParams = new Object[] { id, dishID, quantity };
                    int insertResult = cn.executeQuery(insertOrderDetailsSQL, insertParams);

                    if (insertResult > 0) {
                        System.out.println("Đã thêm món ăn với DishID: " + dishID + " vào OrderDetails.");
                    }
                }

                resultSet.close(); // Đóng ResultSet sau khi sử dụng
            }
        }
        JOptionPane.showMessageDialog(null, "Đã thêm món ăn vào đơn hàng thành công!");
        frmOrderDetail.needRefresh = true;
        System.out.println(frmOrderDetail.needRefresh);
    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Lỗi khi thêm món ăn vào OrderDetails: " + e);
        System.out.println(e);
    }
}


    public void getMenu() {
    try {
        DefaultTableModel dt = (DefaultTableModel) tbMenu.getModel();
        dt.setRowCount(0);
        Connect cn = new Connect();
        // Create a JTable model to store the retrieved data
        System.out.println("Connected database success");
        Object[] argv = new Object[0];
        
        // Cập nhật truy vấn để lấy FoodType
        try (ResultSet resultSet = cn.selectQuery("SELECT * FROM Menu WHERE Availability = 1", argv)) {
            System.out.println("Ket noi ok" + resultSet);
            while (resultSet.next()) {
                Vector v = new Vector();
                v.add(resultSet.getInt("DishID"));
                v.add(resultSet.getString("Name")); // Tên món ăn
                v.add(resultSet.getString("FoodType")); // Thêm FoodType
                v.add(resultSet.getString("Description")); // Mô tả món ăn
                v.add(resultSet.getFloat("Price")); // Giá món ăn
                v.add(0);
                dt.addRow(v);
            }
            cn.close();
        }
        tbMenu.getColumnModel().getColumn(5).setCellRenderer(new QuantityEditorRenderer1());
        tbMenu.getColumnModel().getColumn(5).setCellEditor(new QuantityEditorRenderer1());
    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Lỗi khi lấy thực đơn: " + e);
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

        btnAdd = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        btnRefesh = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tbMenu = new javax.swing.JTable();
        txtSearch = new javax.swing.JTextField();
        btnExit = new javax.swing.JButton();
        btnSearch = new javax.swing.JButton();
        btnShow = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();

        setClosable(true);
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);

        btnAdd.setText("Add");
        btnAdd.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnAddMouseClicked(evt);
            }
        });
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Segoe UI Black", 1, 18)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Menu");

        btnRefesh.setText("Refesh");
        btnRefesh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefeshActionPerformed(evt);
            }
        });

        jLabel6.setText("Search:");

        tbMenu.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        tbMenu.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Name", "Food Type", "Description", "Price", "Quantity"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tbMenu.setGridColor(new java.awt.Color(204, 204, 204));
        tbMenu.setRowHeight(30);
        tbMenu.setShowGrid(true);
        tbMenu.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbMenuMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tbMenu);

        btnExit.setText("Exit");
        btnExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExitActionPerformed(evt);
            }
        });

        btnSearch.setText("Search");
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });

        btnShow.setText("Show Dish Order");
        btnShow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnShowActionPerformed(evt);
            }
        });

        jButton1.setText("Export");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(252, 252, 252)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 311, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addGap(18, 18, 18)
                                .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnSearch)))
                        .addGap(0, 309, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnRefesh)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnAdd)
                .addGap(94, 94, 94)
                .addComponent(jButton1)
                .addGap(102, 102, 102)
                .addComponent(btnShow)
                .addGap(129, 129, 129)
                .addComponent(btnExit)
                .addGap(33, 33, 33))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSearch)
                    .addComponent(jLabel6)
                    .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 303, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(29, 29, 29)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAdd)
                    .addComponent(btnRefesh)
                    .addComponent(btnExit)
                    .addComponent(btnShow)
                    .addComponent(jButton1))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnRefeshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefeshActionPerformed
        // TODO add your handling code here:
        txtSearch.setText("");
        sorter.setRowFilter(null);
    }//GEN-LAST:event_btnRefeshActionPerformed

    private void tbMenuMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbMenuMouseClicked
        // TODO add your handling code here:
       
    }//GEN-LAST:event_tbMenuMouseClicked

    private void btnExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExitActionPerformed
        // TODO add your handling code here:
        this.dispose();
    }//GEN-LAST:event_btnExitActionPerformed

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        // TODO add your handling code here:
        String searchText = txtSearch.getText().trim(); // Lấy văn bản tìm kiếm
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchText)); // Tìm kiếm không phân biệt hoa thường
    }//GEN-LAST:event_btnSearchActionPerformed

    private void btnAddMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnAddMouseClicked
        // TODO add your handling code here:
        addSelectedDishesToOrderDetails();
        this.dispose();
    }//GEN-LAST:event_btnAddMouseClicked

    private void btnShowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShowActionPerformed
        // TODO add your handling code here:
        sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
        @Override
        public boolean include(RowFilter.Entry<? extends DefaultTableModel, ? extends Integer> entry) {
            int quantity = (int) entry.getValue(5); // Cột 5 là cột Quantity
            return quantity > 0; // Chỉ bao gồm hàng có quantity > 0
        }
    });
    }//GEN-LAST:event_btnShowActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        exportTableToExcel(tbMenu);
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * @param args the command line arguments
     */
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnExit;
    private javax.swing.JButton btnRefesh;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnShow;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tbMenu;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
