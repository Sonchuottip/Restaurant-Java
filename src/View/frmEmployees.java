/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package View;
import Database.Connect;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.Vector;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.DefaultComboBoxModel;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLException;
import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableModel;
/**
 *
 * @author mSoSm
 */
public class frmEmployees extends javax.swing.JInternalFrame {

    /**
     * Creates new form FrmEmployees
     */
    
    public frmEmployees() {
        initComponents();
        getEmployees();
        getPosition();
        tbEmployees.setDefaultEditor(Object.class, null);
    }
    
    public void getEmployees() {
    try {
        Connect cn = new Connect();
        DefaultTableModel dt = (DefaultTableModel) tbEmployees.getModel();
        dt.setRowCount(0); // Làm sạch bảng trước khi thêm dữ liệu mới

        // Câu lệnh truy vấn để lấy dữ liệu từ Employees và Users
        String query = "SELECT Employees.EmployeeID, Employees.Name,Employees.CitizenID, Employees.Position, " +
                       "Employees.Salary, Employees.ShiftCount, Users.UserID, Users.Email, Users.Phone " +
                       "FROM Employees " +
                       "JOIN Users ON Employees.UserID = Users.UserID";

        try (ResultSet resultSet = cn.selectQuery(query, new Object[0])) {
            while (resultSet.next()) {
                Vector v = new Vector();
                v.add(resultSet.getInt("EmployeeID")); // Thêm EmployeeID vào Vector
                v.add(resultSet.getString("Name")); // Thêm Name vào Vector
                v.add(resultSet.getString("CitizenID"));
                v.add(resultSet.getString("Position")); // Thêm Position vào Vector
                v.add(resultSet.getBigDecimal("Salary")); // Thêm Salary vào Vector
                v.add(resultSet.getString("ShiftCount")); // Thêm ShiftCount vào Vector
                v.add(resultSet.getString("Email")); // Thêm Email vào Vector
                v.add(resultSet.getString("Phone")); // Thêm Phone vào Vector
                dt.addRow(v); // Thêm Vector vào bảng
            }
            System.out.println("Lấy dữ liệu thành công từ Employees và Users");
        }
        
        cn.close(); // Đóng kết nối
    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Lỗi khi lấy dữ liệu: " + e.getMessage());
        System.out.println(e);
    }
}

    public void getPosition(){
        try{
            DefaultComboBoxModel model = (DefaultComboBoxModel) jComboBox1.getModel();
            model.removeAllElements();
            Connect cn = new Connect();
            Object[] argv = new Object[0];
            
            try (ResultSet rs= cn.selectQuery("SELECT DISTINCT Position FROM Employees", argv))
            {
                model.addElement("Choice");
                while (rs.next()){
                    String position = rs.getString("Position");
                    model.addElement(position);
                    jComboBox1.setActionCommand(position);
                }
            }
            jComboBox1.setModel(model);
            cn.close();
        }
        catch(Exception e){
        }
    }
    
    public boolean isValidInputs(String citizenID, String salary, String phone) {
    // Kiểm tra CitizenID (12 ký tự số)
    if (citizenID.length() != 12 || !citizenID.matches("\\d{12}")) {
        JOptionPane.showMessageDialog(null, "CitizenID phải là 12 ký tự số.");
        return false;
    }
    
    // Kiểm tra Salary (phải là số dương)
    try {
        BigDecimal salaryValue = new BigDecimal(salary.trim()); // Sử dụng BigDecimal
        if (salaryValue.compareTo(BigDecimal.ZERO) <= 0) {
            JOptionPane.showMessageDialog(null, "Salary phải là số dương.");
            return false;
        }
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(null, "Salary phải là một số hợp lệ.");
        return false;
    }
    
    // Kiểm tra Phone (10 ký tự số)
    if (phone.length() != 10 || !phone.matches("\\d{10}")) {
        JOptionPane.showMessageDialog(null, "Phone phải là 10 ký tự số.");
        return false;
    }

    return true; // Tất cả các kiểm tra đều hợp lệ
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
    public int insertEmployees() throws SQLException {
    Object[] argv = new Object[6]; // Chỉ định 6 mục ở đây
    argv[0] = txtName.getText();
    argv[1] = txtCitizenID.getText().replace(" ", "");
    argv[2] = txtSalary.getText().replace(" ", ""); // Giữ nguyên kiểu String cho việc kiểm tra
    argv[3] = jComboBox1.getSelectedItem();
    argv[4] = txtEmail.getText();
    argv[5] = txtPhone.getText().replace(" ", "");

    // Kiểm tra đầu vào
    if (!isValidInputs((String) argv[1], (String) argv[2], (String) argv[5])) {
        JOptionPane.showMessageDialog(null, "Thông tin không hợp lệ. Vui lòng kiểm tra lại.");
        return 0; // Trả về 0 nếu dữ liệu không hợp lệ
    }

    // Bắt đầu giao dịch
    Connect cn = new Connect();
    String errorMessage = null; // Biến lưu trữ thông báo lỗi
    int tempUserID = 0; // Biến tạm lưu UserID, mặc định là 0

    try {
        cn.setAutoCommit(false); // Tắt tự động commit để bắt đầu giao dịch

        Object[] userArgs = new Object[5];
        userArgs[0] = argv[4]; // UserName(Email)
        userArgs[1] = hashPassword("123456789"); // Mật khẩu mặc định
        userArgs[2] = argv[3]; // Role (Vị trí)
        userArgs[3] = argv[4]; // Email
        userArgs[4] = argv[5]; // Số điện thoại

        // Thêm vào bảng Users
        int rs = cn.executeQuery("INSERT INTO Users (Username, Password, Role, Email, Phone) VALUES (?, ?, ?, ?, ?)", userArgs);
        if (rs > 0) {
            // Lấy UserID vừa thêm
            String query = "SELECT UserID FROM Users WHERE Username = ?"; // Truy vấn theo Username
            ResultSet resultSet = cn.selectQuery(query, new Object[]{argv[4]}); // Sử dụng Username từ argv[4]
            int userID = 0;
            if (resultSet.next()) {
                userID = resultSet.getInt("UserID");
                System.out.println("UserID được tạo: " + userID); // In ra UserID vào console
            } else {
                System.out.println("Không thể lấy UserID cho Username: " + argv[4]);
            }

            // Thêm vào bảng Employees với tempUserID là 0
            String sqlEmployee = "INSERT INTO Employees (Name, CitizenID, Position, Salary, UserID) VALUES (?, ?, ?, ?, ?)";
            Object[] employeeArgs = new Object[5];
            employeeArgs[0] = argv[0]; // Tên
            employeeArgs[1] = argv[1]; // CitizenID
            employeeArgs[2] = argv[3]; // Vị trí
            employeeArgs[3] = Integer.parseInt((String) argv[2]); // Lương (chuyển đổi sang int)
            employeeArgs[4] = tempUserID; // Sử dụng tempUserID tạm thời (mặc định là 0)

            int employeeResult = cn.executeQuery(sqlEmployee, employeeArgs);
            if (employeeResult > 0) {
                cn.commit(); // Commit các thay đổi trước khi cập nhật UserID

                // Cập nhật lại UserID trong bảng Employees
                String updateEmployeeSql = "UPDATE Employees SET UserID = ? WHERE UserID = 0 AND CitizenID = ?";
                Object[] updateArgs = new Object[]{userID, argv[1]}; // Cập nhật UserID cho Employee vừa thêm
                int updateResult = cn.executeQuery(updateEmployeeSql, updateArgs);
                
                if (updateResult > 0) {
                    return employeeResult; // Trả về số bản ghi đã thêm vào Employees
                } else {
                    // Nếu không thành công trong cập nhật UserID, có thể báo lỗi
                    JOptionPane.showMessageDialog(null, "Cập nhật UserID không thành công.");
                }
            }
        } else {
            System.out.println("Không thể thêm bản ghi vào bảng Users.");
        }

        clearText();
        getPosition();
        return rs; // Trả về số bản ghi đã thêm vào Users nếu thành công

    } catch (SQLIntegrityConstraintViolationException ex) {
        cn.rollback(); // Hoàn tác nếu có lỗi xảy ra
        errorMessage = "Lỗi ràng buộc dữ liệu: ";
        if (ex.getMessage().contains("Users_Email_UNIQUE")) {
            errorMessage += "Email đã tồn tại.";
        } else if (ex.getMessage().contains("Users_Phone_UNIQUE")) {
            errorMessage += "Số điện thoại đã tồn tại.";
        } else if (ex.getMessage().contains("Employees_CitizenID_UNIQUE")) {
            errorMessage += "Citizen ID đã tồn tại.";
        } else {
            errorMessage += ex.getMessage();
        }
    } catch (Exception e) {
        cn.rollback(); // Hoàn tác nếu có lỗi xảy ra
        errorMessage = "Thêm mới thất bại: " + e.getMessage();
    } finally {
        try {
            // Đảm bảo bật lại chế độ auto-commit
            cn.setAutoCommit(true); 
        } catch (Exception autoCommitEx) {
            System.out.println("Không thể bật lại auto-commit: " + autoCommitEx.getMessage());
        }
        
        // Hiển thị thông báo lỗi duy nhất nếu có
        if (errorMessage != null) {
            JOptionPane.showMessageDialog(null, errorMessage);
        }
    }
    
    return 0; // Trả về 0 để biểu thị lỗi
}

    public int deleteEmployees() {
        // Lấy EmployeeID từ JLabel
        int employeeID = Integer.parseInt(LabelID.getText().split(": ")[1]);
        int userID = 0; // Khai báo biến để lưu UserID
        Connect cn = null; // Khai báo kết nối

        try {
            cn = new Connect();

            // 1. Lấy UserID từ bảng Employees
            String selectQuery = "SELECT UserID FROM Employees WHERE EmployeeID = ?";
            ResultSet resultSet = cn.selectQuery(selectQuery, new Object[]{employeeID});
            if (resultSet.next()) {
                userID = resultSet.getInt("UserID");
            }

            // 2. Xóa nhân viên từ bảng Employees
            String deleteEmployeeQuery = "DELETE FROM Employees WHERE EmployeeID = ?";
            int employeeResult = cn.executeQuery(deleteEmployeeQuery, new Object[]{employeeID});

            // 3. Xóa người dùng từ bảng Users
            if (employeeResult > 0) {
                String deleteUserQuery = "DELETE FROM Users WHERE UserID = ?";
                int userResult = cn.executeQuery(deleteUserQuery, new Object[]{userID});

                if (userResult > 0) {
                    JOptionPane.showMessageDialog(null, "Xóa nhân viên và người dùng thành công!");
                    clearText(); // Gọi hàm để xóa dữ liệu trên giao diện
                    getPosition();
                    return employeeResult; // Trả về số bản ghi đã xóa trong Employees
                } else {
                    // Nếu không xóa được user
                    JOptionPane.showMessageDialog(null, "Không thể xóa người dùng.");
                    return 0;
                }
            } else {
                // Nếu không xóa được employee
                JOptionPane.showMessageDialog(null, "Không thể xóa nhân viên.");
                return 0;
            }

        } catch (SQLException e) {
            // Xử lý lỗi SQL nếu có
            JOptionPane.showMessageDialog(null, "Đã xảy ra lỗi SQL: " + e.getMessage());
            System.out.println(e);
            return 0;
        } catch (Exception e) {
            // Xử lý lỗi chung
            JOptionPane.showMessageDialog(null, "Đã xảy ra lỗi: " + e.getMessage());
            System.out.println(e);
            return 0;
        } finally {
            // Đóng kết nối cơ sở dữ liệu nếu được mở
            if (cn != null) {
                try {
                    cn.close(); // Đảm bảo đóng kết nối
                } catch (SQLException e) {
                    System.out.println("Không thể đóng kết nối: " + e.getMessage());
                }
            }
        }
    }

    public int updateEmployees() {
    // Lấy EmployeeID từ JLabel
    int employeeID = Integer.parseInt(LabelID.getText().split(": ")[1]);
    int userID = 0; // Khai báo biến để lưu UserID
    Connect cn = null; // Khai báo kết nối

    try {
        cn = new Connect();

        // 1. Lấy UserID từ bảng Employees
        String selectQuery = "SELECT UserID FROM Employees WHERE EmployeeID = ?";
        ResultSet resultSet = cn.selectQuery(selectQuery, new Object[]{employeeID});
        if (resultSet.next()) {
            userID = resultSet.getInt("UserID");
        }

        // 2. Cập nhật thông tin nhân viên trong bảng Employees
        String updateEmployeeQuery = "UPDATE Employees SET Name = ?, CitizenID = ?, Salary = ?, Position = ? WHERE EmployeeID = ?";
        Object[] employeeArgs = new Object[]{
            txtName.getText(),
            txtCitizenID.getText().replace(" ", ""),
            txtSalary.getText().replace(" ", ""),
            jComboBox1.getSelectedItem(),
            employeeID
        };
        int employeeResult = cn.executeQuery(updateEmployeeQuery, employeeArgs);

        // 3. Cập nhật thông tin người dùng trong bảng Users
        String updateUserQuery = "UPDATE Users SET Role = ?, Email = ?, Phone = ? WHERE UserID = ?";
        Object[] userArgs = new Object[]{
            jComboBox1.getSelectedItem(), // Cập nhật Role từ jComboBox1
            txtEmail.getText(),
            txtPhone.getText().replace(" ", ""),
            userID
        };
        int userResult = cn.executeQuery(updateUserQuery, userArgs);

        if (employeeResult > 0 && userResult > 0) {
            JOptionPane.showMessageDialog(null, "Cập nhật nhân viên và người dùng thành công!");
            clearText();
            getPosition();
            return employeeResult; // Trả về số bản ghi đã cập nhật trong Employees
        } else {
            // Nếu không cập nhật được nhân viên hoặc người dùng
            JOptionPane.showMessageDialog(null, "Không thể cập nhật nhân viên hoặc người dùng.");
            return 0;
        }

    } catch (SQLException e) {
        // Xử lý lỗi SQL nếu có
        JOptionPane.showMessageDialog(null, "Đã xảy ra lỗi SQL: " + e.getMessage());
        System.out.println(e);
        return 0;
    } catch (Exception e) {
        // Xử lý lỗi chung
        JOptionPane.showMessageDialog(null, "Đã xảy ra lỗi: " + e.getMessage());
        System.out.println(e);
        return 0;
    } finally {
        // Đóng kết nối cơ sở dữ liệu nếu được mở
        if (cn != null) {
            try {
                cn.close(); // Đảm bảo đóng kết nối
            } catch (SQLException e) {
                System.out.println("Không thể đóng kết nối: " + e.getMessage());
            }
        }
    }
}

    public void searchEmployees() {
    try {
        Connect cn = new Connect();
        DefaultTableModel dt = (DefaultTableModel) tbEmployees.getModel();
        dt.setRowCount(0); // Làm sạch bảng trước khi thêm dữ liệu mới

        // Bắt đầu xây dựng câu truy vấn
        String query = "SELECT Employees.EmployeeID, Employees.Name, Employees.CitizenID, " +
                       "Employees.Position, Employees.Salary, Employees.ShiftCount, " +
                       "Users.UserID, Users.Email, Users.Phone " +
                       "FROM Employees " +
                       "JOIN Users ON Employees.UserID = Users.UserID WHERE 1=1";

        // Lấy giá trị từ txtSearch
        String searchQuery = txtSearch.getText().trim();

        // Tạo một mảng Object để lưu các tham số
        List<Object> params = new ArrayList<>();

        // Kiểm tra xem có nội dung trong ô tìm kiếm không
        if (!searchQuery.isEmpty()) {
            query += " AND (Employees.Name LIKE ? OR Employees.CitizenID LIKE ? OR " +
                     "Employees.Position LIKE ? OR CAST(Employees.Salary AS NVARCHAR) LIKE ? " +
                     "OR Users.Email LIKE ? OR Users.Phone LIKE ?)";
            
            // Thêm các tham số cho mỗi điều kiện tìm kiếm
            String likeSearch = "%" + searchQuery + "%";
            params.add(likeSearch); // Tìm kiếm theo Name
            params.add(likeSearch); // Tìm kiếm theo CitizenID
            params.add(likeSearch); // Tìm kiếm theo Position
            params.add(likeSearch); // Tìm kiếm theo Salary
            params.add(likeSearch); // Tìm kiếm theo Email
            params.add(likeSearch); // Tìm kiếm theo Phone
        }

        // Chuyển đổi danh sách params thành mảng Object
        Object[] finalParams = params.toArray();
        
        System.out.println("Câu truy vấn: " + query);
        System.out.print("Tham số: ");
        for (Object param : finalParams) {
            System.out.print(param + " ");
        }
        System.out.println(); // Xuống dòng sau khi in xong tham số

        // Thực thi truy vấn với các tham số đã được xác định
        ResultSet resultSet = cn.selectQuery(query, finalParams);
        while (resultSet.next()) {
            Vector<String> v = new Vector<>();
            v.add(resultSet.getInt("EmployeeID") + ""); // Thêm EmployeeID vào Vector dưới dạng String
            v.add(resultSet.getString("Name")); // Thêm Name vào Vector
            v.add(resultSet.getString("CitizenID"));
            v.add(resultSet.getString("Position")); // Thêm Position vào Vector
            v.add(resultSet.getBigDecimal("Salary") + ""); // Chuyển Salary sang String
            v.add(resultSet.getString("ShiftCount")); // Thêm ShiftCount vào Vector
            v.add(resultSet.getString("Email")); // Thêm Email vào Vector
            v.add(resultSet.getString("Phone")); // Thêm Phone vào Vector
            dt.addRow(v); // Thêm Vector vào bảng
        }

        System.out.println("Tìm kiếm và lấy dữ liệu thành công từ Employees và Users");
        cn.close(); // Đảm bảo đóng kết nối
    } catch (SQLException e) {
        // Xử lý lỗi SQL nếu có
        JOptionPane.showMessageDialog(null, "Đã xảy ra lỗi SQL: " + e.getMessage());
        System.out.println(e);
    } catch (Exception e) {
        // Xử lý lỗi chung
        JOptionPane.showMessageDialog(null, "Đã xảy ra lỗi: " + e.getMessage());
        System.out.println(e);
    }
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

    public void clearText(){
        LabelID.setText("Employee ID: ");
        txtName.setText("");
        txtCitizenID.setText("");
        txtSalary.setText("");
        LabelCount.setText("Shift Count: ");
        txtEmail.setText("");
        txtPhone.setText("");
        txtSearch.setText("");
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        popupMenu1 = new java.awt.PopupMenu();
        jScrollPane1 = new javax.swing.JScrollPane();
        tbEmployees = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        LabelID = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        LabelUserID = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        txtEmail = new javax.swing.JTextField();
        jComboBox1 = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        txtSalary = new javax.swing.JTextField();
        LabelCount = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        txtPhone = new javax.swing.JTextField();
        btnExit = new javax.swing.JButton();
        btnAdd = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnUpdate = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();
        txtCitizenID = new javax.swing.JTextField();
        btnDetail = new javax.swing.JButton();
        btnSearch = new javax.swing.JButton();
        txtSearch = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();

        popupMenu1.setLabel("popupMenu1");

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        tbEmployees.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Name", "Citizen ID", "Position", "Salary", "Shift Count", "Email", "Phone"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        tbEmployees.setShowGrid(true);
        tbEmployees.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbEmployeesMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tbEmployees);
        if (tbEmployees.getColumnModel().getColumnCount() > 0) {
            tbEmployees.getColumnModel().getColumn(0).setMaxWidth(30);
            tbEmployees.getColumnModel().getColumn(1).setMinWidth(100);
            tbEmployees.getColumnModel().getColumn(1).setMaxWidth(130);
            tbEmployees.getColumnModel().getColumn(2).setMinWidth(100);
            tbEmployees.getColumnModel().getColumn(2).setMaxWidth(150);
            tbEmployees.getColumnModel().getColumn(3).setMaxWidth(80);
            tbEmployees.getColumnModel().getColumn(4).setMaxWidth(120);
            tbEmployees.getColumnModel().getColumn(5).setMaxWidth(70);
            tbEmployees.getColumnModel().getColumn(6).setMinWidth(100);
        }

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setText("Employees Manager");

        jLabel2.setText("Name:");

        LabelID.setText("Employee ID:");

        LabelUserID.setText("CitizenID:");

        jLabel5.setText("Email:");

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox1.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                jComboBox1PopupMenuWillBecomeVisible(evt);
            }
        });
        jComboBox1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jComboBox1MouseClicked(evt);
            }
        });
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        jLabel3.setText("Position:");

        jLabel4.setText("Salary:  ");

        LabelCount.setText("Shift Count:");

        jLabel7.setText("Phone:");

        btnExit.setText("Exit");
        btnExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExitActionPerformed(evt);
            }
        });

        btnAdd.setText("Add");
        btnAdd.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnAddMouseClicked(evt);
            }
        });

        btnDelete.setText("Delete");
        btnDelete.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnDeleteMouseClicked(evt);
            }
        });

        btnUpdate.setText("Update");
        btnUpdate.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnUpdateMouseClicked(evt);
            }
        });

        btnRefresh.setText("Refresh");
        btnRefresh.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnRefreshMouseClicked(evt);
            }
        });

        btnDetail.setText("Detail");
        btnDetail.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDetailActionPerformed(evt);
            }
        });

        btnSearch.setText("Search");
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });

        jLabel6.setText("Search:");

        jMenu1.setText("Export");
        jMenu1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jMenu1MouseClicked(evt);
            }
        });
        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnSearch)
                .addGap(365, 365, 365))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(376, 376, 376)
                        .addComponent(jLabel1))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(39, 39, 39)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(LabelID, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(LabelCount)
                                            .addComponent(jLabel4))
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                    .addComponent(txtName)
                                                    .addComponent(txtSalary, javax.swing.GroupLayout.DEFAULT_SIZE, 193, Short.MAX_VALUE)
                                                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                            .addGroup(layout.createSequentialGroup()
                                                .addGap(45, 45, 45)
                                                .addComponent(btnDetail)))))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(217, 217, 217)
                                        .addComponent(LabelUserID))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(225, 225, 225)
                                        .addComponent(jLabel5))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel7)))
                                .addGap(48, 48, 48)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtCitizenID, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtPhone, javax.swing.GroupLayout.PREFERRED_SIZE, 207, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(14, 14, 14)
                                .addComponent(btnAdd)
                                .addGap(82, 82, 82)
                                .addComponent(btnDelete)
                                .addGap(96, 96, 96)
                                .addComponent(btnUpdate)
                                .addGap(94, 94, 94)
                                .addComponent(btnRefresh)
                                .addGap(93, 93, 93)
                                .addComponent(btnExit)))))
                .addGap(140, 174, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(31, 31, 31)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSearch)
                    .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(LabelID)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(LabelUserID)
                    .addComponent(txtCitizenID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(28, 28, 28)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel5)
                    .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(28, 28, 28)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtSalary, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7)
                    .addComponent(txtPhone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(23, 23, 23)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(LabelCount)
                    .addComponent(btnDetail))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 16, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAdd)
                    .addComponent(btnDelete)
                    .addComponent(btnUpdate)
                    .addComponent(btnRefresh)
                    .addComponent(btnExit))
                .addGap(21, 21, 21))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void btnExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExitActionPerformed
        // TODO add your handling code here:
        this.dispose();
    }//GEN-LAST:event_btnExitActionPerformed

    private String formatCitizenID(String input) {
    StringBuilder formatted = new StringBuilder();
    int length = input.length();

    // Kiểm tra nếu có dấu chấm (nếu cần thiết)
    for (int i = 0; i < length; i++) {
        // Thêm dấu cách trước mỗi nhóm 3 chữ số
        if (i > 0 && i % 3 == 0) {
            formatted.append(" ");
        }
        formatted.append(input.charAt(i));
    }

    return formatted.toString().trim(); // Loại bỏ khoảng trắng thừa ở đầu và cuối
}

    private String formatSalary(String input) {
    StringBuilder formatted = new StringBuilder();
    int length = input.length();

    // Kiểm tra nếu có dấu chấm (.)
    int dotIndex = input.indexOf('.');
    String integerPart = (dotIndex != -1) ? input.substring(0, dotIndex) : input;
    String decimalPart = (dotIndex != -1) ? input.substring(dotIndex) : "";

    // Định dạng phần nguyên
    for (int i = 0; i < integerPart.length(); i++) {
        // Thêm dấu cách trước mỗi nhóm 3 chữ số
        if (i > 0 && i % 3 == 0) {
            formatted.append(" ");
        }
        formatted.append(integerPart.charAt(i));
    }

    // Thêm phần thập phân (nếu có)
    formatted.append(decimalPart);

    return formatted.toString().trim(); // Loại bỏ khoảng trắng thừa ở đầu và cuối
}

    private String formatPhone(String input) {
        // Kiểm tra chiều dài của số điện thoại
        if (input.length() != 10) {
            throw new IllegalArgumentException("Số điện thoại phải có 10 chữ số.");
        }

        // Định dạng số điện thoại
        String formatted = input.substring(0, 4) + " " + 
                           input.substring(4, 7) + " " + 
                           input.substring(7);

        return formatted;
    }

    private void tbEmployeesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbEmployeesMouseClicked
        // TODO add your handling code here:
        int i = tbEmployees.getSelectedRow();

        String id = tbEmployees.getValueAt(i, 0).toString();
        LabelID.setText("Employee ID: " + id);
        
        String Name = tbEmployees.getValueAt(i, 1).toString();
        txtName.setText(Name);

        String CitizenID = tbEmployees.getValueAt(i, 2).toString();
        txtCitizenID.setText(formatCitizenID(CitizenID)); // Định dạng CitizenID từ trái sang phải

        String Position = tbEmployees.getValueAt(i, 3).toString();
        jComboBox1.setSelectedItem(Position);

        String Salary = tbEmployees.getValueAt(i, 4).toString();
        txtSalary.setText(formatSalary(Salary)); // Định dạng Salary từ phải sang trái

        String Count = tbEmployees.getValueAt(i, 5).toString();
        LabelCount.setText("Shift Count: " + Count);

        String Email = tbEmployees.getValueAt(i, 6).toString();
        txtEmail.setText(Email);

        String Phone = tbEmployees.getValueAt(i, 7).toString();
        txtPhone.setText(formatPhone(Phone)); // Định dạng Phone từ trái sang phải

    }//GEN-LAST:event_tbEmployeesMouseClicked

    private void jComboBox1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jComboBox1MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox1MouseClicked

    private void jComboBox1PopupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_jComboBox1PopupMenuWillBecomeVisible
        // TODO add your handling code here:
          // Lưu trữ các phần tử hiện có trong mô hình
        DefaultComboBoxModel model = (DefaultComboBoxModel) jComboBox1.getModel();

        // Kiểm tra nếu "Choice" tồn tại và loại bỏ nó
        int index = model.getIndexOf("Choice");
        if (index != -1) {
            model.removeElementAt(index);
        }

        // Đặt lại mô hình cho jComboBox1
        jComboBox1.setModel(model);
    }//GEN-LAST:event_jComboBox1PopupMenuWillBecomeVisible

    private void btnRefreshMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnRefreshMouseClicked
        // TODO add your handling code here:
        clearText();
        getEmployees();
        getPosition();
    }//GEN-LAST:event_btnRefreshMouseClicked

    private void btnAddMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnAddMouseClicked
        // TODO add your handling code here:
        try {
        int result = insertEmployees(); // Gọi phương thức để thêm nhân viên

        // Kiểm tra kết quả và hiển thị thông báo
        if (result > 0) {
            JOptionPane.showMessageDialog(null, "Thêm nhân viên thành công!");
        } else {
            System.out.println("Không thể thêm nhân viên.");
        }
        
        // Cập nhật danh sách nhân viên
        getEmployees(); 

    } catch (SQLException e) {
        // Xử lý lỗi SQL nếu có
        System.out.println("Đã xảy ra lỗi SQL: " + e.getMessage());
    } catch (Exception e) {
        // Xử lý lỗi chung
        System.out.println("Đã xảy ra lỗi: " + e.getMessage());
    }
    }//GEN-LAST:event_btnAddMouseClicked

    private void btnDeleteMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnDeleteMouseClicked
        deleteEmployees();
        getEmployees();
    }//GEN-LAST:event_btnDeleteMouseClicked

    private void btnUpdateMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnUpdateMouseClicked
        // TODO add your handling code here:
        updateEmployees();
        getEmployees();
    }//GEN-LAST:event_btnUpdateMouseClicked

    private void btnDetailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDetailActionPerformed

    if (!LabelID.getText().equals("Employee ID:")) { // Điều kiện kiểm tra
        frmMain mainFrame = (frmMain) SwingUtilities.getWindowAncestor(this);
        String id =(LabelID.getText().split(": ")[1]);
        // Tạo một instance mới của frmShiftDetail
        frmShiftDetail detailForm = new frmShiftDetail(id);
        
        // Thêm frmShiftDetail vào myDesktop của frmMain
        mainFrame.getMyDesktop().add(detailForm);
        
        // Thiết lập kích thước và vị trí cho detailForm
        detailForm.setSize(600, 400); // Kích thước của form
        detailForm.setLocation((mainFrame.getMyDesktop().getWidth() - detailForm.getWidth()) / 2,
                                (mainFrame.getMyDesktop().getHeight() - detailForm.getHeight()) / 2);
        
        // Hiển thị frmShiftDetail
        detailForm.setVisible(true);
    }

    }//GEN-LAST:event_btnDetailActionPerformed

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        // TODO add your handling code here:
        searchEmployees();
    }//GEN-LAST:event_btnSearchActionPerformed

    private void jMenu1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jMenu1MouseClicked
        // TODO add your handling code here:
        exportTableToExcel(tbEmployees);
    }//GEN-LAST:event_jMenu1MouseClicked

    /**
     * @param args the command line arguments
     */
   

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel LabelCount;
    private javax.swing.JLabel LabelID;
    private javax.swing.JLabel LabelUserID;
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnDetail;
    private javax.swing.JButton btnExit;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private java.awt.PopupMenu popupMenu1;
    private javax.swing.JTable tbEmployees;
    private javax.swing.JTextField txtCitizenID;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtPhone;
    private javax.swing.JTextField txtSalary;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
