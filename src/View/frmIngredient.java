/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package View;

import Database.Connect;
import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;
import java.sql.SQLException; // Import SQLException
import java.sql.ResultSet; // Import ResultSet
import java.text.ParseException;
import java.text.SimpleDateFormat; // Import SimpleDateFormat
import java.util.Date; // Import Date
import java.util.Locale;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane; // Import JOptionPane
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableModel;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author mSoSm
 */
public final class frmIngredient extends javax.swing.JInternalFrame {
    /**
     * Creates new form frmIngredient
     */
    public frmIngredient() {
        initComponents();
        Getdefault();
        getIngredient();
        getByDate();
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
    
    public final void getIngredient() {
    try {
        // Lấy DefaultTableModel của bảng tbIngredient
        DefaultTableModel dt = (DefaultTableModel) tbIngredient.getModel();
        dt.setRowCount(0); // Xóa tất cả các hàng hiện có trong bảng

        // Kết nối cơ sở dữ liệu
        Connect cn = new Connect();
        System.out.println("Connected to database successfully");
        Object[] argv = new Object[0]; // Không có tham số cho truy vấn

        // Thực hiện truy vấn để lấy danh sách nguyên liệu
        try (ResultSet resultSet = cn.selectQuery("SELECT * FROM Ingredients", argv)) {
            System.out.println("Fetching data from Ingredients...");

            // Duyệt qua kết quả ResultSet và thêm vào bảng
            while (resultSet.next()) {
                Vector v = new Vector();
                v.add(resultSet.getInt("IngredientID")); // ID của nguyên liệu
                v.add(resultSet.getString("Name")); // Tên nguyên liệu
                v.add(resultSet.getString("IngredientType")); // Loại nguyên liệu
                v.add(resultSet.getInt("Quantity")); // Số lượng
                v.add(resultSet.getString("Unit")); // Đơn vị
                v.add(resultSet.getTimestamp("LastUpdated")); // Thời gian cập nhật cuối
                dt.addRow(v); // Thêm hàng vào model của bảng
            }
            cn.close(); // Đóng kết nối sau khi hoàn thành
        }
 //       loadIngredientNames();
    } catch (Exception e) {
        // Hiển thị thông báo lỗi
        JOptionPane.showMessageDialog(null, "Lỗi khi lấy danh sách nguyên liệu: " + e);
        System.out.println(e);
    }
}

    public final void getByDate() {
    try {
        // Lấy ngày từ jDateChooser1
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String selectedDate = sdf.format(jDateChooser1.getDate()); // Chuyển ngày thành chuỗi

        // Lấy DefaultTableModel của bảng tbIngredient
        DefaultTableModel dt = (DefaultTableModel) tbByDate.getModel();
        dt.setRowCount(0); // Xóa tất cả các hàng hiện có trong bảng

        // Kết nối cơ sở dữ liệu
        Connect cn = new Connect();
        System.out.println("Connected to database successfully");

        // Câu truy vấn SQL để lọc dữ liệu theo ngày
        String query = "SELECT e.EntryID, i.Name, i.IngredientType, e.Quantity, e.RemainingQuantity, e.UnitPrice, e.ExpirationDate,i.Unit "
                     + "FROM IngredientsEntry e "
                     + "INNER JOIN Ingredients i ON e.IngredientID = i.IngredientID "
                     + "WHERE e.EntryDate = ?";

        // Thực hiện truy vấn với tham số ngày
        Object[] argv = {selectedDate};
        try (ResultSet resultSet = cn.selectQuery(query, argv)) {
            System.out.println("Fetching data from IngredientsEntry for date: " + selectedDate);

            // Duyệt qua kết quả ResultSet và thêm vào bảng
            while (resultSet.next()) {
                Vector v = new Vector();
                v.add(resultSet.getInt("EntryID")); // ID của nguyên liệu
                v.add(resultSet.getString("Name")); // Tên nguyên liệu
                v.add(resultSet.getString("IngredientType")); // Loại nguyên liệu
                v.add(resultSet.getInt("Quantity")); // Số lượng nhập
                v.add(resultSet.getInt("RemainingQuantity")); // Số lượng còn lại
                v.add(resultSet.getString("Unit"));
                v.add(resultSet.getDouble("UnitPrice")); // Giá mỗi đơn vị
                v.add(resultSet.getDate("ExpirationDate")); // Ngày hết hạn
                dt.addRow(v); // Thêm hàng vào model của bảng
            }
            cn.close(); // Đóng kết nối sau khi hoàn thành
        }

    } catch (Exception e) {
        // Hiển thị thông báo lỗi
        JOptionPane.showMessageDialog(null, "Lỗi khi lọc nguyên liệu theo ngày: " + e);
        System.out.println(e);
    }
}
    
    /*private void loadIngredientNames() {
    try {
        ingredientNames.clear(); // Xóa danh sách cũ nếu có
        DefaultTableModel dt = (DefaultTableModel) tbIngredient.getModel(); // Model của bảng
        for (int i = 0; i < dt.getRowCount(); i++) {
            String name = dt.getValueAt(i, 1).toString(); // Lấy cột "Name"
            ingredientNames.add(name); // Thêm vào danh sách
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Lỗi khi tải tên nguyên liệu: " + e.getMessage());
    }
}
    */
    public void Getdefault() {
        jDateChooser1.setDate(new Date()); // Đặt ngày hiện tại
        jDateChooser2.setDate(new Date());
    }
    
    public void addIngredientDay() {
    Connect cn = null;
    try {
        // Bắt đầu kết nối cơ sở dữ liệu
        cn = new Connect();
        cn.setAutoCommit(false); // Bắt đầu transaction

        // Lấy dữ liệu từ giao diện
        String name = txtName.getText().trim();
        int quantity = Integer.parseInt(txtQuantity.getText().trim());
        String unit = txtUnit.getText().trim();
        double unitPrice = Double.parseDouble(txtPrice.getText().trim());
        String ingredientType = jComboBox1.getSelectedItem().toString();

        // Định dạng ngày hết hạn và ngày nhập
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String expirationDate = sdf.format(jDateChooser2.getDate());
        String entryDate = sdf.format(new Date()); // Ngày hiện tại

        // 1. Kiểm tra xem nguyên liệu đã tồn tại hay chưa
        String checkQuery = "SELECT IngredientID FROM Ingredients WHERE Name = ?";
        Object[] checkArgs = {name};
        ResultSet rs = cn.selectQuery(checkQuery, checkArgs);

        int ingredientID = -1; // ID của nguyên liệu

        if (rs.next()) {
            // Nguyên liệu đã tồn tại, lấy IngredientID
            ingredientID = rs.getInt("IngredientID");
        } else {
            // Nguyên liệu chưa tồn tại, thêm mới vào bảng Ingredients
            String insertIngredientQuery = "INSERT INTO Ingredients (Name, Quantity, Unit, IngredientType) VALUES (?, 0, ?, ?)";
            Object[] insertArgs = {name, unit, ingredientType};
            cn.executeQuery(insertIngredientQuery, insertArgs);

            // Lấy lại IngredientID của nguyên liệu vừa thêm
            ResultSet newRs = cn.selectQuery(checkQuery, checkArgs);
            if (newRs.next()) {
                ingredientID = newRs.getInt("IngredientID");
            }
        }

        // 2. Thêm vào bảng IngredientsEntry
        String insertEntryQuery = "INSERT INTO IngredientsEntry (IngredientID, EntryDate, Quantity, UnitPrice, ExpirationDate, RemainingQuantity) VALUES (?, ?, ?, ?, ?, ?)";
        Object[] insertEntryArgs = {ingredientID, entryDate, quantity, unitPrice, expirationDate, quantity};
        cn.executeQuery(insertEntryQuery, insertEntryArgs);

        // Commit transaction nếu mọi thứ thành công
        cn.commit();

        JOptionPane.showMessageDialog(null, "Thêm nguyên liệu thành công!");
        getIngredient(); // Làm mới bảng nguyên liệu
        getByDate();     // Làm mới bảng nhập theo ngày

    } catch (Exception e) {
        try {
            if (cn != null) {
                cn.rollback(); // Rollback nếu có lỗi xảy ra
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        JOptionPane.showMessageDialog(null, "Lỗi khi thêm nguyên liệu: " + e.getMessage());
        e.printStackTrace();
    } finally {
        try {
            if (cn != null) {
                cn.close(); // Đóng kết nối
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}

    public void deleteIngredientEntry() {
    Connect cn = null;
    try {
        // Kết nối đến cơ sở dữ liệu
        cn = new Connect();

        // Lấy dữ liệu từ các JTextField, JComboBox và JDateChooser
        String name = txtName.getText().trim(); // Tên nguyên liệu
        String quantityStr = txtQuantity.getText().trim(); // Số lượng
        String priceStr = txtPrice.getText().trim(); // Giá

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String expirationDate = sdf.format(jDateChooser2.getDate()); // Ngày hết hạn

        // Kiểm tra xem dữ liệu có rỗng không
        if (name.isEmpty() || quantityStr.isEmpty() || priceStr.isEmpty() || expirationDate.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        int quantity = Integer.parseInt(quantityStr);
        double price = Double.parseDouble(priceStr);

        // Truy vấn để lấy EntryID dựa trên dữ liệu từ giao diện
        String selectQuery = "SELECT EntryID FROM IngredientsEntry e " +
                             "INNER JOIN Ingredients i ON e.IngredientID = i.IngredientID " +
                             "WHERE i.Name = ? AND e.Quantity = ? AND e.UnitPrice = ? AND e.ExpirationDate = ?";
        Object[] selectArgs = {name, quantity, price, expirationDate};

        ResultSet rs = cn.selectQuery(selectQuery, selectArgs);

        if (rs.next()) {
            int entryID = rs.getInt("EntryID"); // Lấy EntryID

            // Xác nhận trước khi xóa
            int confirm = JOptionPane.showConfirmDialog(null, "Bạn có chắc chắn muốn xóa bản ghi này?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return; // Hủy thao tác nếu người dùng chọn No
            }

            // Thực hiện câu lệnh DELETE trong bảng IngredientsEntry
            String deleteQuery = "DELETE FROM IngredientsEntry WHERE EntryID = ?";
            Object[] deleteArgs = {entryID};
            int rowsAffected = cn.executeQuery(deleteQuery, deleteArgs);

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(null, "Xóa bản ghi thành công!");
                getByDate();
            } else {
                JOptionPane.showMessageDialog(null, "Không tìm thấy bản ghi cần xóa!");
            }
        } else {
            JOptionPane.showMessageDialog(null, "Không tìm thấy bản ghi khớp với thông tin đã nhập!");
        }
    } catch (HeadlessException | NumberFormatException | SQLException e) {
        JOptionPane.showMessageDialog(null, "Lỗi khi xóa bản ghi: " + e.getMessage());
    } finally {
        try {
            if (cn != null) {
                cn.close(); // Đóng kết nối
            }
        } catch (SQLException ex) {
        }
    }
}

    public void updateIngredientEntry() {
    Connect cn = null;
    try {
        // Kết nối đến cơ sở dữ liệu
        cn = new Connect();

        // Lấy EntryID từ LabelID (giả sử LabelID có dạng "ID: 123")
        String labelText = LabelID.getText();  // lblEntryID là JLabel chứa thông tin "ID: 123"
        
        // Tách ID từ chuỗi, giả sử LabelID có dạng "ID: 123"
        String entryIDStr = labelText.replace("ID: ", "").trim();  // Xóa "ID: " và khoảng trắng
        int entryID = Integer.parseInt(entryIDStr);  // Chuyển thành int

        // Lấy dữ liệu từ các JTextField, JComboBox và JDateChooser
        String name = txtName.getText().trim(); // Tên nguyên liệu
        String quantityStr = txtQuantity.getText().trim(); // Số lượng
        String priceStr = txtPrice.getText().trim(); // Giá

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String expirationDate = sdf.format(jDateChooser2.getDate()); // Ngày hết hạn

        // Kiểm tra xem dữ liệu có rỗng không
        if (name.isEmpty() || quantityStr.isEmpty() || priceStr.isEmpty() || expirationDate.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        int quantity = Integer.parseInt(quantityStr);
        double price = Double.parseDouble(priceStr);

        // Cập nhật thông tin trong IngredientsEntry
        String updateQuery = "UPDATE IngredientsEntry SET "
                             + "Quantity = ?, "
                             + "UnitPrice = ?, "
                             + "ExpirationDate = ? "
                             + "WHERE EntryID = ?";
        Object[] updateArgs = {quantity, price, expirationDate, entryID};

        // Thực hiện câu lệnh UPDATE
        int rowsAffected = cn.executeQuery(updateQuery, updateArgs);

        if (rowsAffected > 0) {
            JOptionPane.showMessageDialog(null, "Cập nhật bản ghi thành công!");
            getByDate(); // Cập nhật lại giao diện sau khi cập nhật
        } else {
            JOptionPane.showMessageDialog(null, "Không thể cập nhật bản ghi!");
        }

    } catch (HeadlessException | NumberFormatException | SQLException e) {
        JOptionPane.showMessageDialog(null, "Lỗi khi cập nhật bản ghi: " + e.getMessage());
    } finally {
        try {
            if (cn != null) {
                cn.close(); // Đóng kết nối
            }
        } catch (SQLException ex) {
            // Xử lý lỗi khi đóng kết nối
        }
    }
}
    
   public void updateRemainingQuantityFromTable() {
    Connect cn = null;
    StringBuilder errorMessages = new StringBuilder();
    boolean hasError = false; // Biến kiểm tra xem có lỗi hay không
    
    try {
        cn = new Connect();
        
        // Lấy dữ liệu từ JTable (tbByDate)
        DefaultTableModel model = (DefaultTableModel) tbByDate.getModel();
        
        for (int row = 0; row < model.getRowCount(); row++) {
            // Lấy giá trị từ các cột trong JTable
            int entryID = Integer.parseInt(model.getValueAt(row, 0).toString()); // ID từ cột đầu tiên (ID)
            String name = model.getValueAt(row, 1).toString(); // Name từ cột thứ 2 (Name)
            int remainingQuantity = Integer.parseInt(model.getValueAt(row, 4).toString()); // RemainingQuantity từ cột thứ 5 (Remaining Quantity)
            int quantity = Integer.parseInt(model.getValueAt(row, 3).toString()); // Quantity từ cột thứ 4 (Quantity)

            // Kiểm tra xem RemainingQuantity có nhỏ hơn hoặc bằng Quantity không
            if (remainingQuantity > quantity) {
                // Nếu vi phạm, ghi lại thông tin lỗi
                errorMessages.append("Hàng với ID: " + entryID + " (" + name + ") có RemainingQuantity lớn hơn Quantity!\n");
                hasError = true;
            } else {
                // Nếu không vi phạm, tiếp tục cập nhật vào cơ sở dữ liệu
                String updateQuery = "UPDATE IngredientsEntry SET RemainingQuantity = ? WHERE EntryID = ?";
                Object[] updateArgs = {remainingQuantity, entryID};
                int rowsAffected = cn.executeQuery(updateQuery, updateArgs);

                if (rowsAffected > 0) {
                    System.out.println("Cập nhật RemainingQuantity cho EntryID " + entryID + " thành công.");
                }
            }
        }
        
        // Sau khi kiểm tra xong tất cả hàng, nếu có lỗi thì thông báo
        if (hasError) {
            JOptionPane.showMessageDialog(null, "Có lỗi trong các hàng sau:\n" + errorMessages.toString(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, "Cập nhật dữ liệu thành công!");
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(null, "Lỗi khi cập nhật dữ liệu: " + e.getMessage());
    } finally {
        try {
            if (cn != null) {
                cn.close(); // Đóng kết nối
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}

    public final void searchIngredient() {
    try {
        // Lấy từ khóa tìm kiếm từ txtSearch
        String keyword = txtSearch.getText().trim();

        // Kiểm tra nếu không có từ khóa nào thì thông báo
        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập từ khóa tìm kiếm!");
            return;
        }

        // Lấy DefaultTableModel của bảng tbIngredient
        DefaultTableModel dt = (DefaultTableModel) tbIngredient.getModel();
        dt.setRowCount(0); // Xóa tất cả các hàng hiện có trong bảng

        // Kết nối cơ sở dữ liệu
        Connect cn = new Connect();
        System.out.println("Connected to database successfully");

        // Câu truy vấn SQL để tìm kiếm nguyên liệu
        String searchSQL = "SELECT * FROM Ingredients WHERE Name LIKE ? OR IngredientType LIKE ?";

        // Tạo tham số cho câu truy vấn
        Object[] argv = { "%" + keyword + "%", "%" + keyword + "%" };

        // Thực hiện truy vấn để lấy danh sách nguyên liệu
        try (ResultSet resultSet = cn.selectQuery(searchSQL, argv)) {
            System.out.println("Fetching data from Ingredients...");

            // Duyệt qua kết quả ResultSet và thêm vào bảng
            while (resultSet.next()) {
                Vector v = new Vector();
                v.add(resultSet.getInt("IngredientID")); // ID của nguyên liệu
                v.add(resultSet.getString("Name")); // Tên nguyên liệu
                v.add(resultSet.getString("IngredientType")); // Loại nguyên liệu
                v.add(resultSet.getInt("Quantity")); // Số lượng
                v.add(resultSet.getString("Unit")); // Đơn vị
                v.add(resultSet.getTimestamp("LastUpdated")); // Thời gian cập nhật cuối
                dt.addRow(v); // Thêm hàng vào model của bảng
            }
            cn.close(); // Đóng kết nối sau khi hoàn thành
        }
    } catch (Exception e) {
        // Hiển thị thông báo lỗi
        JOptionPane.showMessageDialog(null, "Lỗi khi tìm kiếm nguyên liệu: " + e);
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

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tbIngredient = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        txtSearch = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        btnRefresh1 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jDateChooser1 = new com.toedter.calendar.JDateChooser();
        jScrollPane2 = new javax.swing.JScrollPane();
        tbByDate = new javax.swing.JTable();
        jButton5 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        btnRefresh2 = new javax.swing.JButton();
        btnAdd = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jDateChooser2 = new com.toedter.calendar.JDateChooser();
        jLabel4 = new javax.swing.JLabel();
        txtQuantity = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        txtPrice = new javax.swing.JTextField();
        jComboBox1 = new javax.swing.JComboBox<>();
        jLabel7 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        txtUnit = new javax.swing.JTextField();
        LabelID = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();

        setClosable(true);
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);

        tbIngredient.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Name", "Ingredient Type", "Quantity", "Unit", "Last Updated"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.Object.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class
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
        jScrollPane1.setViewportView(tbIngredient);

        jLabel1.setText("Search:");

        txtSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSearchActionPerformed(evt);
            }
        });

        jButton1.setText("Search");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton4.setText("Exit");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        btnRefresh1.setText("Refresh");
        btnRefresh1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefresh1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(243, 243, 243)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addContainerGap(356, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnRefresh1)
                .addGap(278, 278, 278)
                .addComponent(jButton4)
                .addGap(180, 180, 180))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 511, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnRefresh1)
                    .addComponent(jButton4))
                .addGap(28, 28, 28))
        );

        jTabbedPane1.addTab("Ingredient", jPanel1);

        jDateChooser1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jDateChooser1MouseClicked(evt);
            }
        });
        jDateChooser1.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jDateChooser1PropertyChange(evt);
            }
        });

        tbByDate.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Name", "Type ", "Quantity", "Remaining Quantity", "Unit", "Unit Price", " Expiration Date"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.Float.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, true, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tbByDate.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbByDateMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(tbByDate);

        jButton5.setText("Update Data");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jButton3.setText("Exit");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        btnRefresh2.setText("Refresh");
        btnRefresh2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefresh2ActionPerformed(evt);
            }
        });

        btnAdd.setText("Add");
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        btnDelete.setText("Delete");
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });

        jLabel2.setText("Name:");

        txtName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNameActionPerformed(evt);
            }
        });
        txtName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtNameKeyReleased(evt);
            }
        });

        jLabel3.setText("Expiration Date:");

        jLabel4.setText("Quantity:");

        jLabel6.setText("Unit Price:");

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Meat and Poultry", "Seafood", "Fruits and Vegetables", "Grains and Grain Products", "Spices and Seasonings", "Drinks", "Ready-to-eat Food" }));

        jLabel7.setText("Type:");

        jButton2.setText("Update Table");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jLabel5.setText("Unit:");

        LabelID.setText("ID:");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(325, 325, 325)
                .addComponent(jDateChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(LabelID)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(jButton5)
                        .addGap(52, 52, 52)
                        .addComponent(btnDelete)))
                .addGap(0, 33, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(78, 78, 78)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(txtQuantity, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jDateChooser2, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                        .addGap(32, 32, 32)
                        .addComponent(jButton2)
                        .addGap(52, 52, 52)
                        .addComponent(btnRefresh2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(btnAdd)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 103, Short.MAX_VALUE)
                        .addComponent(jButton3)
                        .addGap(44, 44, 44))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtUnit, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(jDateChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 331, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(LabelID)
                .addGap(20, 20, 20)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4)
                            .addComponent(txtQuantity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6)
                            .addComponent(txtPrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(26, 26, 26)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7)
                            .addComponent(jLabel3)))
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(txtUnit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(jDateChooser2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 51, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAdd)
                    .addComponent(btnRefresh2)
                    .addComponent(jButton3)
                    .addComponent(btnDelete)
                    .addComponent(jButton5)
                    .addComponent(jButton2))
                .addGap(39, 39, 39))
        );

        jTabbedPane1.addTab("Entry", jPanel2);

        jMenu1.setText("Import");
        jMenu1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jMenu1MouseClicked(evt);
            }
        });
        jMenu1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu1ActionPerformed(evt);
            }
        });
        jMenuBar1.add(jMenu1);

        jMenu2.setText("Export");
        jMenu2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu2ActionPerformed(evt);
            }
        });

        jMenuItem1.setText("Ingredient");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem1);

        jMenuItem2.setText("Entry by Date");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem2);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.TRAILING)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSearchActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
        this.dispose();
    }//GEN-LAST:event_jButton4ActionPerformed

    private void btnRefresh1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefresh1ActionPerformed
        // TODO add your handling code here:
        txtSearch.setText("");
        getIngredient();
    }//GEN-LAST:event_btnRefresh1ActionPerformed

    private void txtNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtNameActionPerformed

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        // TODO add your handling code here:
        addIngredientDay();
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnRefresh2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefresh2ActionPerformed
        // TODO add your handling code here:
        Getdefault();
        getByDate();
    }//GEN-LAST:event_btnRefresh2ActionPerformed

    private void jDateChooser1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jDateChooser1MouseClicked
        // TODO add your handling code here:
        getByDate();
    }//GEN-LAST:event_jDateChooser1MouseClicked
    
    public void importExcelToTableWithPreview(JTable table) {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Chọn file Excel");
    int result = fileChooser.showOpenDialog(null);

    if (result == JFileChooser.APPROVE_OPTION) {
        File file = fileChooser.getSelectedFile();
        try (FileInputStream fis = new FileInputStream(file)) {
            // Sử dụng Apache POI để đọc file Excel
            Workbook workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheetAt(0);

            // Lấy thông tin tiêu đề cột từ dòng đầu tiên
            Row headerRow = sheet.getRow(0);
            DefaultTableModel model = new DefaultTableModel();

            // Thêm cột chỉ nếu có dữ liệu
            for (int i = 0; i < headerRow.getPhysicalNumberOfCells(); i++) {
                Cell cell = headerRow.getCell(i);
                if (cell != null && !cell.toString().trim().isEmpty()) {
                    model.addColumn(cell.toString()); // Thêm tên cột nếu cell không null và không trống
                }
            }

            // Đọc dữ liệu từ các hàng và thêm vào model
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row != null) {
                    Object[] rowData = new Object[row.getPhysicalNumberOfCells()];
                    boolean hasData = false; // Biến kiểm tra xem hàng có dữ liệu không

                    for (int colIndex = 0; colIndex < row.getPhysicalNumberOfCells(); colIndex++) {
                        Cell cell = row.getCell(colIndex);
                        if (cell != null) {
                            rowData[colIndex] = cell.toString(); // Chuyển giá trị cell thành chuỗi nếu không null
                            hasData = true; // Nếu có dữ liệu, đánh dấu hàng có dữ liệu
                        } else {
                            rowData[colIndex] = ""; // Nếu cell là null, gán giá trị rỗng
                        }
                    }

                    // Nếu hàng có dữ liệu, thêm vào model
                    if (hasData) {
                        model.addRow(rowData);
                    }
                }
            }

            // Tạo cửa sổ xem trước dữ liệu
            JFrame previewFrame = new JFrame("Xem trước dữ liệu");
            previewFrame.setLayout(new BorderLayout());

            // Tạo bảng với dữ liệu vừa đọc
            JTable previewTable = new JTable(model);
            JScrollPane scrollPane = new JScrollPane(previewTable);
            previewFrame.add(scrollPane, BorderLayout.CENTER);

            // Tạo nút Add và Cancel
            JPanel buttonPanel = new JPanel();
            JButton addButton = new JButton("Add");
            JButton cancelButton = new JButton("Cancel");

            // Nút "Add" để thêm dữ liệu vào cơ sở dữ liệu
            addButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Thêm dữ liệu vào cơ sở dữ liệu
                    addDataToDatabase(model);
                    previewFrame.dispose(); // Đóng cửa sổ xem trước sau khi thêm
                }
            });

            // Nút "Cancel" để hủy bỏ
            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    previewFrame.dispose(); // Đóng cửa sổ xem trước nếu hủy bỏ
                }
            });

            buttonPanel.add(addButton);
            buttonPanel.add(cancelButton);
            previewFrame.add(buttonPanel, BorderLayout.SOUTH);

            // Thiết lập kích thước cửa sổ và hiển thị
            previewFrame.setSize(600, 400);
            previewFrame.setLocationRelativeTo(null);
            previewFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            previewFrame.setVisible(true);

        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Lỗi khi đọc file Excel!");
        }
    }
}

    public void addDataToDatabase(DefaultTableModel model) {
    Connect cn = null;
    try {
        cn = new Connect();
        cn.setAutoCommit(false); // Bắt đầu transaction

        // Lặp qua các hàng của model và thêm vào cơ sở dữ liệu
        for (int rowIndex = 0; rowIndex < model.getRowCount(); rowIndex++) {
            // Lấy dữ liệu từ các cột trong bảng
            String name = (String) model.getValueAt(rowIndex, 1);
            String ingredientType = (String) model.getValueAt(rowIndex, 2);

            // Kiểm tra và xử lý giá trị của cột Quantity
            float quantity = 0;
            if (model.getValueAt(rowIndex, 3) != null) {
                Object qtyValue = model.getValueAt(rowIndex, 3);
                if (qtyValue instanceof Number) {
                    quantity = ((Number) qtyValue).floatValue();
                } else if (qtyValue instanceof String) {
                    try {
                        quantity = Float.parseFloat((String) qtyValue);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid quantity value at row " + rowIndex + ": " + qtyValue);
                    }
                }
            }

            // Kiểm tra và xử lý giá trị của cột RemainingQuantity
            float remainingQuantity = 0;
            if (model.getValueAt(rowIndex, 4) != null) {
                Object remainingQtyValue = model.getValueAt(rowIndex, 4);
                if (remainingQtyValue instanceof Number) {
                    remainingQuantity = ((Number) remainingQtyValue).floatValue();
                } else if (remainingQtyValue instanceof String) {
                    try {
                        remainingQuantity = Float.parseFloat((String) remainingQtyValue);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid remaining quantity value at row " + rowIndex + ": " + remainingQtyValue);
                    }
                }
            }

            String unit = (String) model.getValueAt(rowIndex, 5);

            // Kiểm tra và xử lý giá trị của cột UnitPrice
            double unitPrice = 0.0;
            if (model.getValueAt(rowIndex, 6) != null) {
                Object priceValue = model.getValueAt(rowIndex, 6);
                if (priceValue instanceof Number) {
                    unitPrice = ((Number) priceValue).doubleValue();
                } else if (priceValue instanceof String) {
                    try {
                        unitPrice = Double.parseDouble((String) priceValue);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid unit price value at row " + rowIndex + ": " + priceValue);
                    }
                }
            }

            // Kiểm tra và xử lý giá trị của cột ExpirationDate
            Date expirationDate = null;
            if (model.getValueAt(rowIndex, 7) != null) {
                Object expirationValue = model.getValueAt(rowIndex, 7);
                if (expirationValue instanceof String) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
                        expirationDate = sdf.parse((String) expirationValue);
                    } catch (ParseException e) {
                        System.out.println("Invalid expiration date at row " + rowIndex + ": " + expirationValue);
                    }
                }
            }

            // Kiểm tra xem nguyên liệu đã tồn tại trong ngày chưa
            String checkEntryQuery = "SELECT Quantity FROM IngredientsEntry WHERE IngredientID = (SELECT IngredientID FROM Ingredients WHERE Name = ?) AND CAST(EntryDate AS DATE) = CAST(GETDATE() AS DATE)";
            Object[] checkEntryArgs = {name};
            ResultSet entryRs = cn.selectQuery(checkEntryQuery, checkEntryArgs);
            int ingredientID = -1;

            // Kiểm tra xem nguyên liệu đã tồn tại trong bảng Ingredients
            String checkQuery = "SELECT IngredientID FROM Ingredients WHERE Name = ?";
            Object[] checkArgs = {name};
            ResultSet rs = cn.selectQuery(checkQuery, checkArgs);

            if (rs.next()) {
                ingredientID = rs.getInt("IngredientID");
            } else {
                // Nếu nguyên liệu chưa tồn tại, thêm mới vào bảng Ingredients
                String insertIngredientQuery = "INSERT INTO Ingredients (Name, Quantity, Unit, IngredientType) VALUES (?, 0, ?, ?)";
                Object[] insertArgs = {name, unit, ingredientType};
                cn.executeQuery(insertIngredientQuery, insertArgs);

                // Lấy lại IngredientID của nguyên liệu vừa thêm
                ResultSet newRs = cn.selectQuery(checkQuery, checkArgs);
                if (newRs.next()) {
                    ingredientID = newRs.getInt("IngredientID");
                }
            }

            if (entryRs.next()) {
                // Nếu nguyên liệu đã tồn tại trong ngày, lấy số lượng hiện tại và cộng thêm số lượng mới
                float existingQuantity = entryRs.getFloat("Quantity");
                float updatedQuantity = existingQuantity + quantity;

                String updateQuery = "UPDATE IngredientsEntry SET Quantity = ?, RemainingQuantity = ? WHERE IngredientID = ? AND CAST(EntryDate AS DATE) = CAST(GETDATE() AS DATE)";
                Object[] updateArgs = {updatedQuantity, remainingQuantity, ingredientID};
                cn.executeQuery(updateQuery, updateArgs);
            } else {
                // Nếu nguyên liệu chưa tồn tại trong ngày, thêm mới vào bảng IngredientsEntry
                String insertEntryQuery = "INSERT INTO IngredientsEntry (IngredientID, EntryDate, Quantity, UnitPrice, ExpirationDate, RemainingQuantity) VALUES (?, GETDATE(), ?, ?, ?, ?)";
                Object[] insertEntryArgs = {ingredientID, quantity, unitPrice, expirationDate, remainingQuantity};
                cn.executeQuery(insertEntryQuery, insertEntryArgs);
            }
        }

        cn.commit(); // Commit transaction nếu mọi thứ thành công
        JOptionPane.showMessageDialog(null, "Dữ liệu đã được thêm vào cơ sở dữ liệu thành công!");
        getByDate();
    } catch (Exception e) {
        try {
            if (cn != null) {
                cn.rollback(); // Rollback nếu có lỗi xảy ra
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        JOptionPane.showMessageDialog(null, "Lỗi khi thêm dữ liệu vào cơ sở dữ liệu: " + e.getMessage());
        e.printStackTrace();
    } finally {
        try {
            if (cn != null) {
                cn.close(); // Đóng kết nối
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}

    private void tbByDateMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbByDateMouseClicked
        // TODO add your handling code here:
         int selectedRow = tbByDate.getSelectedRow(); // Lấy hàng được chọn trong bảng
        if (selectedRow != -1) { // Kiểm tra nếu hàng hợp lệ
            DefaultTableModel dt = (DefaultTableModel) tbByDate.getModel(); // Lấy model của bảng
            
            // Lấy dữ liệu từ các cột
            String id = dt.getValueAt(selectedRow, 0).toString();
            String name = dt.getValueAt(selectedRow, 1).toString(); // Cột "Name"
            String quantity = dt.getValueAt(selectedRow, 4).toString(); // Cột "Quantity"
            String price = dt.getValueAt(selectedRow, 6).toString(); // Cột "Unit Price"
            String expirationDate = dt.getValueAt(selectedRow, 7).toString(); // Cột "Expiration Date"
            String Unit = dt.getValueAt(selectedRow, 5).toString();
            String ingredientType = dt.getValueAt(selectedRow, 2).toString(); // Cột "Type"

            // Set dữ liệu vào các trường giao diện
            LabelID.setText("ID: " +id);
            txtName.setText(name); // Gán vào TextField txtName
            txtQuantity.setText(quantity); // Gán vào TextField txtQuantity
            txtPrice.setText(price); // Gán vào TextField txtPrice
            txtUnit.setText(Unit);
            // Đặt giá trị ngày vào JDateChooser
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                java.util.Date date = sdf.parse(expirationDate);
                jDateChooser2.setDate(date); // Set giá trị cho JDateChooser
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Lỗi định dạng ngày: " + e.getMessage());
            }

            // Đặt giá trị vào JComboBox
            jComboBox1.setSelectedItem(ingredientType); // Set loại nguyên liệu vào JComboBox
        }
    }//GEN-LAST:event_tbByDateMouseClicked

    private void txtNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtNameKeyReleased

    }//GEN-LAST:event_txtNameKeyReleased

    private void jDateChooser1PropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jDateChooser1PropertyChange
        if ("date".equals(evt.getPropertyName())) {
                    // Gọi hàm getByDate() sau khi chọn ngày
                    getByDate();
                }
    }//GEN-LAST:event_jDateChooser1PropertyChange

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
        this.dispose();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        // TODO add your handling code here:
        deleteIngredientEntry();
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // TODO add your handling code here:
        updateIngredientEntry();
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
       updateRemainingQuantityFromTable();
       getByDate();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jMenu2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu2ActionPerformed
        // TODO add your handling code here:
        exportTableToExcel(tbByDate);
    }//GEN-LAST:event_jMenu2ActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        // TODO add your handling code here:
        exportTableToExcel(tbIngredient);
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        // TODO add your handling code here:
        exportTableToExcel(tbByDate);
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenu1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu1ActionPerformed
        // TODO add your handling code here:
        importExcelToTableWithPreview(tbByDate);
    }//GEN-LAST:event_jMenu1ActionPerformed

    private void jMenu1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jMenu1MouseClicked
        // TODO add your handling code here:
        jTabbedPane1.setSelectedIndex(1);
        importExcelToTableWithPreview(tbByDate);
    }//GEN-LAST:event_jMenu1MouseClicked

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        searchIngredient();
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * @param args the command line arguments
     */
   

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel LabelID;
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnRefresh1;
    private javax.swing.JButton btnRefresh2;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JComboBox<String> jComboBox1;
    private com.toedter.calendar.JDateChooser jDateChooser1;
    private com.toedter.calendar.JDateChooser jDateChooser2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable tbByDate;
    private javax.swing.JTable tbIngredient;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtPrice;
    private javax.swing.JTextField txtQuantity;
    private javax.swing.JTextField txtSearch;
    private javax.swing.JTextField txtUnit;
    // End of variables declaration//GEN-END:variables
}
