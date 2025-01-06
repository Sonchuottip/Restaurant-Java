package Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Connect {
    // Bước 1: Thiết lập các tham số kết nối
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=RestaurantDB;encrypt=false;";
    private static final String USER = "sa";
    private static final String PASSWORD = "123456789";
    
    private Connection conn;

    public Connect() throws SQLException {
        try {
            // Bước 2: Thiết lập kết nối
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Connect.class.getName()).log(Level.SEVERE, null, ex);
        }
        conn = DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public ResultSet selectQuery(String query, Object[] params) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(query);
        
        // Thiết lập tham số động
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
        
        return stmt.executeQuery(); // Trả về một ResultSet
    }

    // Phương thức thực hiện các truy vấn SQL động (INSERT, UPDATE, DELETE)
    public int executeQuery(String query, Object[] params) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(query);
        
        // Thiết lập tham số động
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }        
        // Xử lý các loại truy vấn khác nhau
        return stmt.executeUpdate(); // Thực hiện INSERT, UPDATE, DELETE        
    }

    // Phương thức để đóng kết nối
    public void close() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        conn.setAutoCommit(autoCommit);
    }

    public void commit() throws SQLException {
        conn.commit();
    }
    
     public void rollback() throws SQLException {
        conn.rollback();
    }

    public int executeScalar(String sqlGetOrderID) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
