/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package View;
import Database.Connect;
import Process.User;
import java.sql.ResultSet;
import java.util.Vector;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.*;
import javax.swing.table.TableColumn;

/**
 *
 * @author mSoSm
 */
public class frmMenuManager extends javax.swing.JInternalFrame {

    /**
     * Creates new form frmMenuManager
     */
    public User us;
    
    public void getMenu() {
    try {
        DefaultTableModel dt = (DefaultTableModel) tbMenu.getModel();
        dt.setRowCount(0);
        Connect cn = new Connect();
        // Create a JTable model to store the retrieved data
        System.out.println("Connected database success");
        Object[] argv = new Object[0];
        
        // Cập nhật truy vấn để lấy FoodType
        try (ResultSet resultSet = cn.selectQuery("SELECT * FROM Menu", argv)) {
            System.out.println("Ket noi ok" + resultSet);
            while (resultSet.next()) {
                Vector v = new Vector();
                v.add(resultSet.getInt("DishID"));
                v.add(resultSet.getString("Name")); // Tên món ăn
                v.add(resultSet.getString("FoodType")); // Thêm FoodType
                v.add(resultSet.getString("Description")); // Mô tả món ăn
                v.add(resultSet.getFloat("Price")); // Giá món ăn
                v.add(resultSet.getBoolean("Availability")); // Tình trạng khả dụng
                dt.addRow(v);
            }
            cn.close();
        }

    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Lỗi khi lấy thực đơn: " + e);
        System.out.println(e);
    }
}

    public void getFoodTypes() {
    try {
        DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) cbType.getModel();
        model.removeAllElements();
        
        // Thêm một lựa chọn mặc định
        model.addElement("Choice");
        
        // Thêm các loại thực phẩm có thể chọn
        model.addElement("Appetizer");
        model.addElement("Main Course");
        model.addElement("Dessert");
        model.addElement("Ready-to-eat Food");
        model.addElement("Drinks");
        model.addElement("Combo");

        cbType.setModel(model);
    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Lỗi khi lấy loại thực phẩm: " + e);
        System.out.println(e);
    }
    }

    public int insertMenu() {
    Object[] argv = new Object[5]; // Thêm 1 phần tử để lưu FoodType
    argv[0] = txtName.getText(); // Tên món ăn
    argv[1] = txtDescription.getText(); // Mô tả món ăn
    argv[2] = Float.parseFloat(txtPrice.getText()); // Giá món ăn
    argv[3] = checkbox1.isSelected(); // Tình trạng khả dụng (1 hoặc 0)
    argv[4] = cbType.getSelectedItem(); // Lấy FoodType từ JComboBox

    try {
        Connect cn = new Connect();

        // Kiểm tra xem món ăn đã tồn tại chưa (không kiểm tra Availability)
        String checkQuery = "SELECT COUNT(*) FROM Menu WHERE Name = ? AND Description = ? AND Price = ? AND FoodType = ?";
        Object[] checkArgs = new Object[] { argv[0], argv[1], argv[2], argv[4] }; // Thêm FoodType vào kiểm tra
        ResultSet checkResult = cn.selectQuery(checkQuery, checkArgs);
        checkResult.next();
        int count = checkResult.getInt(1);
        if (count > 0) {
            JOptionPane.showMessageDialog(null, "Món ăn đã tồn tại trong thực đơn.");
            return 0; // Không thêm mới
        }

        // Thực hiện thêm món ăn
        int rs = cn.executeQuery("INSERT INTO Menu (Name, Description, Price, Availability, FoodType) VALUES (?, ?, ?, ?, ?)", argv);
        if (rs > 0) {
            JOptionPane.showMessageDialog(null, "Thêm mới thành công Món ăn: " + txtName.getText());
            clearText();
        }
        return rs;

    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Thêm mới Món Ăn: " + txtName.getText() + " - Lỗi: " + e);
        System.out.println(e);
        return 0;
    }
}

    public int deleteMenu() {
    Object[] argv = new Object[1];
    // Lấy DishID từ Label có tên là LabelID
    argv[0] = Integer.parseInt(LabelID.getText().split(": ")[1]); // Giả sử LabelID có định dạng như "DishID: 1"
    String name = txtName.getText();
    int confirm = JOptionPane.showConfirmDialog(null, "Bạn có chắc chắn muốn xóa món: " + name + "?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
    if (confirm != JOptionPane.YES_OPTION) {
        return 0; // Không xóa nếu người dùng không xác nhận
    }
    
    try {
        Connect cn = new Connect();
        // Thực hiện truy vấn xóa món ăn từ bảng Menu theo DishID
        int rs = cn.executeQuery("DELETE FROM Menu WHERE DishID = ?", argv);
        
        if (rs > 0) {
            JOptionPane.showMessageDialog(null, "Xóa thành công món: " + name);
            clearText(); // Xóa các trường văn bản nếu cần
        }            
        
        return rs;
    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Xóa thất bại món: " + name + ", error detail: " + e);
        System.out.println(e);
        return 0;
    }
}

    public int updateDataMenu() {    
    Object[] argv = new Object[6]; // Thêm 1 phần tử cho FoodType
    argv[0] = txtName.getText(); // Tên món ăn
    argv[1] = txtDescription.getText(); // Mô tả món ăn
    argv[2] = Float.parseFloat(txtPrice.getText()); // Giá món ăn
    argv[3] = checkbox1.isSelected(); // Tình trạng khả dụng (1 hoặc 0)
    argv[4] = cbType.getSelectedItem(); // Lấy FoodType từ JComboBox
    // Lấy ID từ LabelID (sau dấu ": ")
    String labelText = LabelID.getText();
    int dishId = Integer.parseInt(labelText.split(": ")[1]); // Lấy ID món ăn

    argv[5] = dishId; // ID món ăn
    try {
        Connect cn = new Connect(); // Kết nối cơ sở dữ liệu
        int rs = cn.executeQuery("UPDATE Menu SET Name=?, Description=?, Price=?, Availability=?, FoodType=? WHERE DishID =?", argv); // Câu lệnh cập nhật
        if (rs > 0) {
            JOptionPane.showMessageDialog(null, "Cập nhật thành công món ăn: " + txtName.getText());
            clearText(); // Xóa thông tin trên giao diện
        }
        return rs;

    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Cập nhật thất bại món ăn: " + txtName.getText() + ", error detail: " + e);                 
        System.out.println(e);
        return 0; // Trả về 0 khi có lỗi xảy ra
    }
}

    public void updateAvailability() {
    boolean updateSuccessful = false;
    try {
        Connect cn = new Connect();
        DefaultTableModel model = (DefaultTableModel) tbMenu.getModel();

        // Lặp qua tất cả các hàng trong JTable
        for (int rowIndex = 0; rowIndex < model.getRowCount(); rowIndex++) {
            // Lấy DishID và Availability từ JTable
            int dishId = (Integer) model.getValueAt(rowIndex, 0); // Cột 0 là DishID
            boolean availabilityInTable = (Boolean) model.getValueAt(rowIndex, 5); // Cột 5 là Availability

            // Truy vấn từ database để lấy availability hiện tại
            String query = "SELECT Availability FROM Menu WHERE DishID = ?";
            Object[] argv = new Object[]{dishId};
            try (ResultSet resultSet = cn.selectQuery(query, argv)) {
                if (resultSet.next()) {
                    boolean availabilityInDatabase = resultSet.getBoolean("Availability");

                    // So sánh và cập nhật nếu có sự khác biệt
                    if (availabilityInTable != availabilityInDatabase) {
                        // Cập nhật availability trong database
                        String updateQuery = "UPDATE Menu SET Availability = ? WHERE DishID = ?";
                        Object[] updateArgs = new Object[]{availabilityInTable, dishId}; // Truyền giá trị boolean
                        int updateCount = cn.executeQuery(updateQuery, updateArgs);

                        if (updateCount > 0) {
                            updateSuccessful = true; // Đánh dấu là đã cập nhật thành công
                            System.out.println("Cập nhật thành công trạng thái bảng: " + dishId);
                        } else {
                            System.out.println("Cập nhật không thành công trạng thái bảng: " + dishId);
                        }
                    }
                }
            }
        }
        cn.close();
    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Lỗi khi cập nhật availability: " + e);
        System.out.println(e);
    }
    if (updateSuccessful) {
        JOptionPane.showMessageDialog(null, "Cập nhật bảng thành công!");
        getMenu();
    } else {
        JOptionPane.showMessageDialog(null, "Cập nhật bảng không thành công!");
    }
}

    public void searchMenu() {
    String searchQuery = txtSearch.getText().trim(); // Lấy giá trị từ ô tìm kiếm

    try {
        Connect cn = new Connect();
        String query;
        Object[] argv;

        // Kiểm tra xem có ký tự "có" hoặc "không" trong tìm kiếm
        if (searchQuery.equalsIgnoreCase("có")) {
            // Tìm kiếm với điều kiện Availability = 1
            query = "SELECT * FROM Menu WHERE Availability = 1";
            argv = new Object[] {}; // Không cần tham số
        } else if (searchQuery.equalsIgnoreCase("không")) {
            // Tìm kiếm với điều kiện Availability = 0
            query = "SELECT * FROM Menu WHERE Availability = 0";
            argv = new Object[] {}; // Không cần tham số
        } else {
            // Tìm kiếm theo các trường thông thường
            query = "SELECT * FROM Menu WHERE Name LIKE ? OR Description LIKE ? OR FoodType LIKE ? OR CAST(Price AS NVARCHAR) LIKE ?";
            argv = new Object[] {
                "%" + searchQuery + "%", // Tìm kiếm trong cột Name
                "%" + searchQuery + "%", // Tìm kiếm trong cột Description
                "%" + searchQuery + "%", // Tìm kiếm trong cột FoodType
                "%" + searchQuery + "%"  // Tìm kiếm trong cột Price (chuyển đổi Price thành chuỗi)
            };
        }

        DefaultTableModel model = (DefaultTableModel) tbMenu.getModel();
        model.setRowCount(0); // Xóa tất cả hàng cũ trong bảng

        try (ResultSet rs = cn.selectQuery(query, argv)) {
            while (rs.next()) {
                // Lấy thông tin món ăn từ ResultSet
                int dishId = rs.getInt("DishID");
                String name = rs.getString("Name");
                String description = rs.getString("Description");
                float price = rs.getFloat("Price");
                boolean availability = rs.getBoolean("Availability");
                String foodType = rs.getString("FoodType");

                // Thêm hàng vào bảng
                model.addRow(new Object[] {
                    dishId, name, foodType, description, price ,availability
                });
            }
        }
        cn.close();
    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Lỗi khi tìm kiếm menu: " + e);
        System.out.println(e);
    }
}

    public void clearText() {
        LabelID.setText("Dish ID:");
        txtName.setText("");
        txtDescription.setText("");
        txtPrice.setText("");
        checkbox1.setSelected(false);
        txtSearch.setText("");
    }
    
    public frmMenuManager() {
        initComponents();
        getMenu();
        getFoodTypes();
        tbMenu.setDefaultEditor(Object.class, null);
        
    }
    
    public void receivedata(User data){
        System.out.println("username: " + data.username);
        System.out.println("role: " + data.role);
        us = data;
        VisibilewithRole();
    }
    
    public void VisibilewithRole(){
        if (us.role.equals("Chef"))
        {
            System.out.println("role2: " + us.role);
            btnAdd.setVisible(false);
            btnDelete.setVisible(false);
            btnUpdateData.setVisible(false);
            LabelID.setVisible(false);
            jLabel1.setVisible(false);
            jLabel2.setVisible(false);
            jLabel3.setVisible(false);
            jLabel4.setVisible(false);
            txtName.setVisible(false);
            txtPrice.setVisible(false);
            txtDescription.setBorder(null);
            txtDescription.setOpaque(false);
            txtDescription.setBorder(BorderFactory.createEmptyBorder());
            txtDescription.setVisible(false);
            jLabel5.setVisible(false);
            checkbox1.setVisible(false);
            cbType.setVisible(false);
            jLabel7.setVisible(false);
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
        tbMenu = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtPrice = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        checkbox1 = new javax.swing.JCheckBox();
        btnAdd = new javax.swing.JButton();
        btnRefesh = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnUpdateData = new javax.swing.JButton();
        btnExit = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtDescription = new javax.swing.JTextArea();
        LabelID = new javax.swing.JLabel();
        btnSearch = new javax.swing.JButton();
        btnUpdateTb = new javax.swing.JButton();
        cbType = new javax.swing.JComboBox<>();
        jLabel7 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        txtSearch = new javax.swing.JTextField();

        setClosable(true);
        setIconifiable(true);

        jLabel1.setFont(new java.awt.Font("Segoe UI Black", 1, 18)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Menu Manager");

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
                "ID", "Name", "Food Type", "Description", "Price", "Availability"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Boolean.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
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
        if (tbMenu.getColumnModel().getColumnCount() > 0) {
            tbMenu.getColumnModel().getColumn(0).setMaxWidth(30);
            tbMenu.getColumnModel().getColumn(3).setMinWidth(300);
            tbMenu.getColumnModel().getColumn(5).setMaxWidth(100);
        }

        jLabel2.setText("Name:");

        jLabel3.setText("Price:");

        txtPrice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPriceActionPerformed(evt);
            }
        });

        jLabel4.setText("Description:");

        jLabel5.setText("Availabilty:");

        checkbox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkbox1ActionPerformed(evt);
            }
        });

        btnAdd.setText("Add");
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        btnRefesh.setText("Refesh");
        btnRefesh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefeshActionPerformed(evt);
            }
        });

        btnDelete.setText("Delete");
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });

        btnUpdateData.setText("Update Data");
        btnUpdateData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateDataActionPerformed(evt);
            }
        });

        btnExit.setText("Exit");
        btnExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExitActionPerformed(evt);
            }
        });

        txtDescription.setColumns(20);
        txtDescription.setLineWrap(true);
        txtDescription.setRows(5);
        txtDescription.setWrapStyleWord(true);
        txtDescription.setBorder(null);
        txtDescription.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        jScrollPane2.setViewportView(txtDescription);

        LabelID.setText("Dish ID:");

        btnSearch.setText("Search");
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });

        btnUpdateTb.setText("Update Table");
        btnUpdateTb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateTbActionPerformed(evt);
            }
        });

        cbType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbType.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                cbTypePopupMenuWillBecomeVisible(evt);
            }
        });

        jLabel7.setText("Food Type:");

        jLabel6.setText("Search:");

        txtSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSearchActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(LabelID, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addComponent(jLabel3)
                                        .addGap(44, 44, 44))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(btnRefesh)
                                        .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(25, 25, 25)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(txtPrice, javax.swing.GroupLayout.DEFAULT_SIZE, 262, Short.MAX_VALUE)
                                        .addComponent(txtName)
                                        .addGroup(layout.createSequentialGroup()
                                            .addComponent(cbType, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGap(60, 60, 60)
                                            .addComponent(jLabel5)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(checkbox1)))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(27, 27, 27)
                                        .addComponent(btnDelete)
                                        .addGap(46, 46, 46)
                                        .addComponent(btnAdd)))))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel4))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(17, 17, 17)
                                .addComponent(btnUpdateTb)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 38, Short.MAX_VALUE)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(77, 77, 77)
                                .addComponent(btnUpdateData)
                                .addGap(111, 111, 111)
                                .addComponent(btnExit))
                            .addGroup(layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 355, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(39, 39, 39))
                    .addComponent(jScrollPane1))
                .addContainerGap())
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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addComponent(LabelID)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))
                        .addGap(6, 6, 6)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtPrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3))
                        .addGap(19, 19, 19)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(checkbox1, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel5)
                                .addComponent(cbType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel7)))
                        .addGap(26, 26, 26))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4))
                        .addGap(44, 44, 44)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnExit)
                    .addComponent(btnUpdateData)
                    .addComponent(btnDelete)
                    .addComponent(btnRefesh)
                    .addComponent(btnUpdateTb)
                    .addComponent(btnAdd))
                .addContainerGap(85, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtPriceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPriceActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPriceActionPerformed

    private void btnExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExitActionPerformed
        // TODO add your handling code here:
        this.dispose();
    }//GEN-LAST:event_btnExitActionPerformed

    private void tbMenuMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbMenuMouseClicked
        // TODO add your handling code here:
        int i = tbMenu.getSelectedRow(); // Lấy chỉ số hàng được chọn
        String id = tbMenu.getValueAt(i, 0).toString(); // Lấy DishID
        String name = tbMenu.getValueAt(i, 1).toString(); // Lấy tên món ăn
        String foodType = tbMenu.getValueAt(i, 2).toString(); // Lấy FoodType
        String description = tbMenu.getValueAt(i, 3).toString(); // Lấy mô tả món ăn
        String price = tbMenu.getValueAt(i, 4).toString(); // Lấy giá món ăn
        boolean availability = (boolean) tbMenu.getValueAt(i, 5); // Lấy tình trạng khả dụng

        // Cập nhật các thành phần giao diện
        LabelID.setText("Dish ID: " + id);
        txtName.setText(name);
        txtDescription.setText(description);
        txtPrice.setText(price);
        checkbox1.setSelected(availability);

        // Cập nhật JComboBox với FoodType đã chọn
        cbType.setSelectedItem(foodType); // Chọn FoodType tương ứng trong JComboBox
        
    }//GEN-LAST:event_tbMenuMouseClicked

    private void checkbox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkbox1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_checkbox1ActionPerformed

    private void btnRefeshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefeshActionPerformed
        // TODO add your handling code here:
        clearText();
        getMenu();
        getFoodTypes();
    }//GEN-LAST:event_btnRefeshActionPerformed

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        // TODO add your handling code here:
        insertMenu();
        getMenu();
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnUpdateDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateDataActionPerformed
        // TODO add your handling code here:
        updateDataMenu();
        getMenu();
    }//GEN-LAST:event_btnUpdateDataActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        deleteMenu();
        getMenu();
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        // TODO add your handling code here:
        searchMenu();
    }//GEN-LAST:event_btnSearchActionPerformed

    private void btnUpdateTbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateTbActionPerformed
        // TODO add your handling code here:
        updateAvailability();
        getMenu();
    }//GEN-LAST:event_btnUpdateTbActionPerformed

    private void cbTypePopupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_cbTypePopupMenuWillBecomeVisible
        DefaultComboBoxModel model = (DefaultComboBoxModel) cbType.getModel();

        // Kiểm tra nếu "Choice" tồn tại và loại bỏ nó
        int index = model.getIndexOf("Choice");
        if (index != -1) {
            model.removeElementAt(index);
        }

        // Đặt lại mô hình cho cbType
        cbType.setModel(model);
    }//GEN-LAST:event_cbTypePopupMenuWillBecomeVisible

    private void txtSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSearchActionPerformed

    /**
     * @param args the command line arguments
     */
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel LabelID;
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnExit;
    private javax.swing.JButton btnRefesh;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnUpdateData;
    private javax.swing.JButton btnUpdateTb;
    private javax.swing.JComboBox<String> cbType;
    private javax.swing.JCheckBox checkbox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable tbMenu;
    private javax.swing.JTextArea txtDescription;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtPrice;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
