/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package View;
import Database.Connect;

import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.util.Vector;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDate;
/**
 *
 * @author PREDATOR HELIOS
 */
public class frmShiftDetail extends javax.swing.JInternalFrame {

    public final String employeeId ;
    public frmShiftDetail(String employeeID) {
        initComponents();
        this.employeeId = employeeID; 
        System.out.println("ID:" + employeeId);
        getShift(employeeId);
    }
    
    public void getShift(String id) {
    System.out.println("ID Table:" + id);
    try {
        Connect cn = new Connect();
        DefaultTableModel dt = (DefaultTableModel) tbDetailShift.getModel();
        dt.setRowCount(0); // Làm sạch bảng trước khi thêm dữ liệu mới
        int recordCount = 0;
        
        // Cập nhật truy vấn SQL để sắp xếp theo ngày và theo tên ca
        String query = "SELECT EmployeeShiftSchedule.WorkDate, WorkShift.ShiftName, EmployeeShiftSchedule.Note " +
                       "FROM EmployeeShiftSchedule " +
                       "JOIN WorkShift ON EmployeeShiftSchedule.ShiftID = WorkShift.ShiftID " +
                       "WHERE EmployeeShiftSchedule.EmployeeID = ? " +
                       "AND MONTH(EmployeeShiftSchedule.WorkDate) = MONTH(GETDATE()) " +
                       "AND YEAR(EmployeeShiftSchedule.WorkDate) = YEAR(GETDATE()) " +
                       "AND EmployeeShiftSchedule.Status = 1 " +
                       "ORDER BY EmployeeShiftSchedule.WorkDate ASC, " + 
                       "CASE WorkShift.ShiftName " +
                       "    WHEN 'Sáng' THEN 1 " +
                       "    WHEN 'Chiều' THEN 2 " +
                       "    WHEN 'Tối' THEN 3 " +
                       "    ELSE 4 " + // Để xử lý các ca khác nếu có
                       "END";

        Object[] params = { id }; // Tham số là Employee ID
        ResultSet resultSet = cn.selectQuery(query, params);
        
        while (resultSet.next()) {
            String workDate = resultSet.getString("WorkDate");
            String shiftName = resultSet.getString("ShiftName");
            String note = resultSet.getString("Note");

            // Thêm dữ liệu vào bảng
            Vector v = new Vector();
            v.add(workDate);
            v.add(shiftName);
            v.add(note);
            dt.addRow(v); // Thêm hàng vào bảng
            recordCount++;
        }
        
        LabelTotal.setText("Total Shift: " + recordCount);
        // Cập nhật bảng sau khi thêm dữ liệu
        cn.close(); // Đóng kết nối
    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Lỗi khi lấy dữ liệu: " + e.getMessage());
    }
}

    private void resetToToday() {
        System.out.println("Employee ID reset: " + employeeId); // In ra Employee ID
        LocalDate today = LocalDate.now(); // Lấy ngày hiện tại
        jMonthChooser1.setMonth(today.getMonthValue() - 1); // Đặt tháng (JMonthChooser bắt đầu từ 0)
        jYearChooser1.setYear(today.getYear()); // Đặt năm
        LabelTotal.setText("Total Shift:");
    }
    
    private void searchShifts() {
    String day = txtDay.getText().trim();
    int month = jMonthChooser1.getMonth() + 1; // Month is 0-based
    int year = jYearChooser1.getYear();
    int recordCount = 0; // Biến đếm số bản ghi

    DefaultTableModel dtm = (DefaultTableModel) tbDetailShift.getModel();
    dtm.setRowCount(0); // Làm sạch bảng trước khi thêm dữ liệu mới

    StringBuilder queryBuilder = new StringBuilder();
    queryBuilder.append("SELECT EmployeeShiftSchedule.WorkDate, WorkShift.ShiftName, EmployeeShiftSchedule.Note ")
            .append("FROM EmployeeShiftSchedule ")
            .append("JOIN WorkShift ON EmployeeShiftSchedule.ShiftID = WorkShift.ShiftID ")
            .append("WHERE EmployeeShiftSchedule.EmployeeID = ? ");

    Object[] params;

    boolean isDayValid = true;
    int dayInt = 0;

    if (!day.isEmpty()) {
        try {
            dayInt = Integer.parseInt(day); // Chuyển đổi sang số nguyên
            if (dayInt < 1 || dayInt > getDaysInMonth(month, year)) {
                isDayValid = false;
                JOptionPane.showMessageDialog(null, "Ngày không hợp lệ cho tháng " + month + " năm " + year);
            }
        } catch (NumberFormatException e) {
            isDayValid = false;
            JOptionPane.showMessageDialog(null, "Ngày phải là một số hợp lệ");
        }

        if (isDayValid) {
            queryBuilder.append("AND DAY(EmployeeShiftSchedule.WorkDate) = ? ")
                        .append("AND MONTH(EmployeeShiftSchedule.WorkDate) = ? ")
                        .append("AND YEAR(EmployeeShiftSchedule.WorkDate) = ? ");
            params = new Object[]{employeeId, dayInt, month, year};
        } else {
            return; // Không thực hiện tìm kiếm nếu ngày không hợp lệ
        }
    } else {
        queryBuilder.append("AND MONTH(EmployeeShiftSchedule.WorkDate) = ? ")
                    .append("AND YEAR(EmployeeShiftSchedule.WorkDate) = ? ");
        params = new Object[]{employeeId, month, year};
    }

    // Thêm sắp xếp vào truy vấn
    queryBuilder.append("ORDER BY EmployeeShiftSchedule.WorkDate ASC, ")
                .append("CASE WorkShift.ShiftName ")
                .append("    WHEN 'Sáng' THEN 1 ")
                .append("    WHEN 'Chiều' THEN 2 ")
                .append("    WHEN 'Tối' THEN 3 ")
                .append("    ELSE 4 ") // Để xử lý các ca khác nếu có
                .append("END");

    try {
        Connect cn = new Connect();
        try (ResultSet resultSet = cn.selectQuery(queryBuilder.toString(), params)) {
            while (resultSet.next()) {
                Vector v = new Vector();
                v.add(resultSet.getString("WorkDate"));
                v.add(resultSet.getString("ShiftName"));
                v.add(resultSet.getString("Note"));
                dtm.addRow(v); // Thêm bản ghi vào DefaultTableModel
                recordCount++; // Tăng số bản ghi
            }
            LabelTotal.setText("Total Shift: " + recordCount);
        }

        cn.close(); // Đóng kết nối
    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Lỗi khi lấy dữ liệu: " + e.getMessage());
    }
}


// Phương thức để lấy số ngày trong tháng
    private int getDaysInMonth(int month, int year) {
        if (month == 2) {
            return (isLeapYear(year)) ? 29 : 28;
        }
        return switch (month) {
            case 1, 3, 5, 7, 8, 10, 12 -> 31;
            case 4, 6, 9, 11 -> 30;
            default -> 0;
        };
    }

    // Phương thức kiểm tra năm nhuận
    private boolean isLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }

// Phương thức lấy và hiển thị chi tiết ca làm việc

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        tbDetailShift = new javax.swing.JTable();
        btnSearch = new javax.swing.JButton();
        btnExit = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        txtDay = new javax.swing.JTextField();
        jYearChooser1 = new com.toedter.calendar.JYearChooser();
        jMonthChooser1 = new com.toedter.calendar.JMonthChooser();
        btnRefresh = new javax.swing.JButton();
        LabelTotal = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        tbDetailShift.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Date", "Work Shift ", "Note"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        tbDetailShift.setShowGrid(true);
        jScrollPane1.setViewportView(tbDetailShift);

        btnSearch.setText("Search");
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });

        btnExit.setText("Exit");
        btnExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExitActionPerformed(evt);
            }
        });

        jLabel1.setText("Day:");

        btnRefresh.setText("Refresh");
        btnRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshActionPerformed(evt);
            }
        });

        LabelTotal.setText("Total Shift:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(51, 51, 51)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtDay, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(45, 45, 45)
                        .addComponent(jMonthChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(56, 56, 56)
                        .addComponent(jYearChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(97, 97, 97)
                        .addComponent(btnSearch)
                        .addGap(41, 41, 41)
                        .addComponent(btnRefresh)
                        .addGap(54, 54, 54)
                        .addComponent(btnExit))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(33, 33, 33)
                        .addComponent(LabelTotal)))
                .addContainerGap(87, Short.MAX_VALUE))
            .addComponent(jScrollPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jMonthChooser1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jYearChooser1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtDay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(LabelTotal)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 28, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSearch)
                    .addComponent(btnRefresh)
                    .addComponent(btnExit))
                .addGap(48, 48, 48))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExitActionPerformed
        // TODO add your handling code here:
        this.dispose();
    }//GEN-LAST:event_btnExitActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        resetToToday();
        getShift(employeeId);
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        searchShifts();
    }//GEN-LAST:event_btnSearchActionPerformed
    
    /**
     * @param args the command line arguments
     */
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel LabelTotal;
    private javax.swing.JButton btnExit;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnSearch;
    private javax.swing.JLabel jLabel1;
    private com.toedter.calendar.JMonthChooser jMonthChooser1;
    private javax.swing.JScrollPane jScrollPane1;
    private com.toedter.calendar.JYearChooser jYearChooser1;
    private javax.swing.JTable tbDetailShift;
    private javax.swing.JTextField txtDay;
    // End of variables declaration//GEN-END:variables
}
