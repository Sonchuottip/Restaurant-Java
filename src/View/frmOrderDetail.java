/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package View;
import Process.Order;
import Process.User;
import Interface.QuantityEditorRenderer2;
import Interface.QuantityEditorRenderer1;
import java.sql.ResultSet;
import Database.Connect;
import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.sql.SQLException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
/**
 *
 * @author mSoSm
 */
public class frmOrderDetail extends javax.swing.JPanel {
    public Order or;
    /**
     * Creates new form OrderDetail
     */
    public static boolean needRefresh = false;
    public User us;
   
    public frmOrderDetail(Order order,User data) {
        initComponents();
        receivedata(order,data);
        new Thread(() -> {
            while (true) {
                if (needRefresh) {
                    SwingUtilities.invokeLater(frmOrderDetail.this::checkAndRefresh);
                }
                try {
                    Thread.sleep(1000); // Kiểm tra mỗi giây
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }   }
        }).start();
}
    
    public void checkAndRefresh() {
        if (needRefresh) {
            getOrderDetails();// Gọi hàm làm mới dữ liệu
            getTotalAmountFromDatabase();
            jLabel1.setText("Total Amount:"+ or.TotalAmount);
            needRefresh = false;   // Reset trạng thái sau khi làm mới
        }
    }
    
    private BufferedImage createImageFromPanel(JPanel panel) {
        BufferedImage image = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        panel.printAll(g2d); // Sao chép nội dung của JPanel vào BufferedImage
        g2d.dispose();
        return image;
    }

    // Hàm xem trước nội dung của JPanel1
    private void showPrintPreview(JPanel panel) {
        // Tạo một JDialog để hiển thị bản xem trước
        JFrame tempFrame = new JFrame();  
        JDialog previewDialog = new JDialog(tempFrame, "Print Preview", true);
        previewDialog.setSize(650,700);
        previewDialog.setLocationRelativeTo(this);

        // Tạo hình ảnh từ JPanel1
        BufferedImage image = createImageFromPanel(panel);

        // Hiển thị hình ảnh trong JLabel
        JLabel previewLabel = new JLabel(new ImageIcon(image));
        JScrollPane scrollPane = new JScrollPane(previewLabel);
        previewDialog.add(scrollPane, BorderLayout.CENTER);

        // Thêm nút "Print" vào JDialog
        JButton printButton = new JButton("Print");
        printButton.addActionListener(e -> {
            printPanel(panel);  // Gọi hàm in khi nhấn nút "Print"
            previewDialog.dispose();  // Đóng cửa sổ xem trước sau khi in
        });

        previewDialog.add(printButton, BorderLayout.SOUTH);
        previewDialog.setVisible(true);
    }
    
    private void printPanel(JPanel panel) {
        PrinterJob printerJob = PrinterJob.getPrinterJob();
        printerJob.setPrintable(new Printable() {
            @Override
            public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
                if (pageIndex > 0) {
                    return Printable.NO_SUCH_PAGE;
                }

                Graphics2D g2d = (Graphics2D) graphics;
                g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

                // In nội dung của JPanel1
                panel.printAll(graphics);

                return Printable.PAGE_EXISTS;
            }
        });

        // Kiểm tra xem có thể in không
        if (printerJob.printDialog()) {
            try {
                printerJob.print();
            } catch (PrinterException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    private void getTotalAmountFromDatabase() {
    try {
        // Tạo kết nối đến cơ sở dữ liệu
        Connect cn = new Connect();
        System.out.println("Kết nối đến cơ sở dữ liệu thành công");

        // Định nghĩa câu truy vấn SQL để lấy TotalAmount dựa trên OrderID
        String query = "SELECT TotalAmount FROM Orders WHERE OrderID = ?";

        // Thực thi câu truy vấn với OrderID làm tham số
        try (ResultSet resultSet = cn.selectQuery(query, new Object[]{or.OrderID})) {
            if (resultSet.next()) {
                // Lấy TotalAmount từ ResultSet và gán cho đối tượng Order
                double totalAmount = resultSet.getDouble("TotalAmount");
                or.TotalAmount = (float) totalAmount;  // Gán giá trị vào đối tượng `or`
                System.out.println("Total Amount: " + totalAmount);
            }
        }

        // Đóng kết nối
        cn.close();
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Lỗi khi truy xuất TotalAmount: " + e.getMessage());
        System.out.println(e);
    }
}
    
    private void receivedata(Order other, User data){
        or = other;
        us = data;
        jLabel1.setText("Total Amount:"+ or.TotalAmount);
        getOrderDetails();
        UserInterface();
    } 
    
    private void getOrderDetails() {
    try {
        getTotalAmountFromDatabase();
        Connect cn = new Connect();
        System.out.println("Connected to database successfully");

        // Tùy chỉnh câu truy vấn dựa trên vai trò của người dùng
        String query = "SELECT OrderDetails.OrderDetailID, Menu.Name, OrderDetails.Quantity, OrderDetails.CompletedQuantity, Menu.Price " +
                       "FROM OrderDetails " +
                       "JOIN Menu ON OrderDetails.DishID = Menu.DishID " +
                       "WHERE OrderDetails.OrderID = ?";

        // Nếu vai trò là "Chef", thêm điều kiện để chỉ lấy các món ăn có CompletedQuantity khác Quantity
        if (us.role.equals("Chef")) {
            query += " AND OrderDetails.CompletedQuantity <> OrderDetails.Quantity";
        }

        try (ResultSet resultSet = cn.selectQuery(query, new Object[]{or.OrderID})) {
            // Kiểm tra xem resultSet có dữ liệu hay không
            if (!resultSet.isBeforeFirst()) {
                return; // Nếu không có dữ liệu, thoát khỏi hàm
            }

            // Xóa dữ liệu hiện có trong tbDetail
            DefaultTableModel model = (DefaultTableModel) tbDetail.getModel();
            model.setRowCount(0);

            // Lặp qua từng kết quả và thêm vào bảng
            while (resultSet.next()) {
                int orderDetailId = resultSet.getInt("OrderDetailID");
                String dishName = resultSet.getString("Name");
                int quantity = resultSet.getInt("Quantity");
                int completedQuantity = resultSet.getInt("CompletedQuantity");
                double price = resultSet.getDouble("Price");

                // Thêm hàng vào bảng
                model.addRow(new Object[]{
                    orderDetailId,
                    dishName,
                    quantity,
                    completedQuantity,
                    price
                });
            }
        }
        cn.close();
    } catch (HeadlessException | SQLException e) {
        JOptionPane.showMessageDialog(this, "Lỗi khi tải thông tin chi tiết đơn hàng: " + e.getMessage());
        System.out.println(e);
    }
} 
    
    private void UpdateOrder() {
    try {
        Connect cn = new Connect();
        DefaultTableModel model = (DefaultTableModel) tbDetail.getModel();

        // Duyệt qua tất cả các hàng trong bảng tbDetail
        for (int row = 0; row < model.getRowCount(); row++) {
            int orderDetailId = (int) model.getValueAt(row, 0); // Lấy OrderDetailID (cột 0)
            int quantity = (int) model.getValueAt(row, 2); // Lấy Quantity (cột 2)
            int completedQuantity = (int) model.getValueAt(row, 3); // Lấy CompletedQuantity (cột 3)

            // Câu truy vấn để cập nhật CompletedQuantity
            String updateSQL = "UPDATE OrderDetails SET CompletedQuantity = ? WHERE OrderDetailID = ? AND CompletedQuantity <> ?";
            Object[] params = {completedQuantity, orderDetailId, completedQuantity};

            // Thực hiện cập nhật và kiểm tra kết quả
            int updatedRows = cn.executeQuery(updateSQL, params);

            if (updatedRows > 0) {
                System.out.println("Đã cập nhật CompletedQuantity cho OrderDetailID: " + orderDetailId);
            }
        }

        // Thông báo cập nhật thành công và tải lại chi tiết đơn hàng
        JOptionPane.showMessageDialog(this, "Đã cập nhật đơn hàng thành công!");
        getOrderDetails(); // Tải lại chi tiết đơn hàng để cập nhật giao diện
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật đơn hàng: " + e.getMessage());
        e.printStackTrace();
    }
}

    private void UserInterface(){
        LabelID.setText("Order ID:" + or.OrderID);
        LabelNumber.setText("Table Number:" + or.TableNumber);
        LabelDate.setText("Date:"+or.OrderDate);
        LabelStart.setText("Time Start:"+or.OrderTime);
        jButton1.setVisible(false);
        if (or.Status == 1) {
        try {
            Connect cn = new Connect();
            String query = "SELECT PaymentTime FROM Orders WHERE OrderID = ?";

            // Khởi tạo mảng Object để chứa tham số
            Object[] params = new Object[1];
            params[0] = or.OrderID; // Gán giá trị OrderID vào mảng

            // Thực hiện truy vấn
            ResultSet resultSet = cn.selectQuery(query, params); 

            // Xử lý kết quả từ ResultSet
            if (resultSet.next()) {
            java.util.Date paymentTime = resultSet.getTimestamp("PaymentTime");
            or.PaymentTime = paymentTime;

            // Định dạng chỉ lấy giờ, phút, giây
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm:ss");
            String formattedTime = sdf.format(or.PaymentTime);

            // Cập nhật LabelFinish
            LabelFinish.setVisible(true);
            LabelFinish.setText("Time Finish: " + formattedTime);
            LabelStatus.setText("Status: Complete");
            // In ra giá trị PaymentTime để kiểm tra
            System.out.println("Giá trị PaymentTime: " + formattedTime);
            jButton1.setVisible(true);
            btnAdd.setVisible(false);
            btnPayment.setVisible(false);
            btnUpdate.setVisible(false);
            btnRefresh.setVisible(false);
        }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
            switch (us.role) {
            case "Chef" -> {
                tbDetail.getColumnModel().getColumn(3).setCellRenderer(new QuantityEditorRenderer2(tbDetail));
                tbDetail.getColumnModel().getColumn(3).setCellEditor(new QuantityEditorRenderer2(tbDetail));
            }
            case "Manager" -> {
                tbDetail.setDefaultEditor(Object.class, null);
            }
            case "Server" ->{
                tbDetail.getColumnModel().getColumn(2).setCellRenderer(new QuantityEditorRenderer1());
                tbDetail.getColumnModel().getColumn(2).setCellEditor(new QuantityEditorRenderer1());
                tbDetail.getColumnModel().getColumn(tbDetail.getColumnCount() - 1).setMinWidth(0);
                tbDetail.getColumnModel().getColumn(tbDetail.getColumnCount() - 1).setMaxWidth(0);
                tbDetail.getColumnModel().getColumn(tbDetail.getColumnCount() - 1).setPreferredWidth(0);
            }
            default -> {
                tbDetail.getColumnModel().getColumn(tbDetail.getColumnCount() - 1).setMinWidth(0);
                tbDetail.getColumnModel().getColumn(tbDetail.getColumnCount() - 1).setMaxWidth(0);
                tbDetail.getColumnModel().getColumn(tbDetail.getColumnCount() - 1).setPreferredWidth(0);
            }
        }
    }
        else{
            LabelFinish.setVisible(false);
            LabelStatus.setText("Status: In Process");
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

        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jSpinner1 = new javax.swing.JSpinner();
        btnAdd = new javax.swing.JButton();
        btnPayment = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();
        btnExit = new javax.swing.JButton();
        btnUpdate = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        LabelDate = new javax.swing.JLabel();
        LabelStart = new javax.swing.JLabel();
        LabelFinish = new javax.swing.JLabel();
        LabelID = new javax.swing.JLabel();
        LabelNumber = new javax.swing.JLabel();
        LabelStatus = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tbDetail = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane2.setViewportView(jTextArea1);

        btnAdd.setText("Add Dish");
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        btnPayment.setText("Payment");
        btnPayment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPaymentActionPerformed(evt);
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

        btnUpdate.setText("Update");
        btnUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateActionPerformed(evt);
            }
        });

        LabelDate.setText("Date:");

        LabelStart.setText("Time Start:");

        LabelFinish.setText("Time Finish:");

        LabelID.setText("Order ID:");

        LabelNumber.setText("Table Number:");

        LabelStatus.setText("Status:");

        tbDetail.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "ID", "Name", "Quantity", "Completed Quantity", "Price"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Float.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, true, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tbDetail);

        jLabel1.setText("Total Amount:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 598, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 2, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(LabelStatus)
                    .addComponent(LabelID)
                    .addComponent(LabelNumber))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(LabelDate))
                .addGap(114, 114, 114)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(LabelStart)
                    .addComponent(LabelFinish, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(88, 88, 88))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(LabelID)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(LabelDate, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(LabelNumber)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(LabelStart)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(LabelStatus)
                    .addComponent(LabelFinish)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 432, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jButton1.setText("Print");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnAdd)
                .addGap(18, 18, 18)
                .addComponent(btnPayment)
                .addGap(18, 18, 18)
                .addComponent(btnRefresh)
                .addGap(28, 28, 28)
                .addComponent(jButton1)
                .addGap(43, 43, 43)
                .addComponent(btnUpdate)
                .addGap(18, 18, 18)
                .addComponent(btnExit)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnRefresh)
                    .addComponent(btnPayment)
                    .addComponent(btnAdd)
                    .addComponent(btnExit)
                    .addComponent(btnUpdate)
                    .addComponent(jButton1))
                .addGap(35, 35, 35))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExitActionPerformed
        // TODO add your handling code here:
        java.awt.Container parent = this.getParent(); // Lấy đối tượng cha của panel
        if (parent != null) {
            parent.remove(this); // Xóa panel khỏi JFrame
            parent.revalidate(); // Cập nhật lại giao diện
            parent.repaint(); // Vẽ lại giao diện
        }
    }//GEN-LAST:event_btnExitActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        getOrderDetails();
        
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        // TODO add your handling code here:
        frmMain mainFrame = (frmMain) SwingUtilities.getWindowAncestor(this);
        frmMenu frm = new frmMenu();
        frm.receiveData(or.OrderID);
        mainFrame.getMyDesktop().add(frm);
        frm.setVisible(true);
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        // TODO add your handling code here:
        UpdateOrder();
    }//GEN-LAST:event_btnUpdateActionPerformed

    private void btnPaymentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPaymentActionPerformed
        // TODO add your handling code here:
        frmMain mainFrame = (frmMain) SwingUtilities.getWindowAncestor(this);
        frmPayment frm = new frmPayment(or);
        mainFrame.getMyDesktop().add(frm);
        frm.setVisible(true);
        java.awt.Container parent = this.getParent(); // Lấy đối tượng cha của panel
        if (parent != null) {
            parent.remove(this); // Xóa panel khỏi JFrame
            parent.revalidate(); // Cập nhật lại giao diện
            parent.repaint(); // Vẽ lại giao diện
        }
    }//GEN-LAST:event_btnPaymentActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        showPrintPreview(jPanel1);
        
    }//GEN-LAST:event_jButton1ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel LabelDate;
    private javax.swing.JLabel LabelFinish;
    private javax.swing.JLabel LabelID;
    private javax.swing.JLabel LabelNumber;
    private javax.swing.JLabel LabelStart;
    private javax.swing.JLabel LabelStatus;
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnExit;
    private javax.swing.JButton btnPayment;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTable tbDetail;
    // End of variables declaration//GEN-END:variables
}
