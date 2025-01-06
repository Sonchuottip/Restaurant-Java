/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Process;
import java.util.Date;

/**
 *
 * @author PREDATOR HELIOS
 */
public class Payments {
    public int paymentID;          // Mã định danh cho thanh toán
    public int orderID;            // Khóa ngoại tham chiếu đến bảng Orders
    public Integer userID;         // Khóa ngoại tham chiếu đến bảng Users (có thể null)
    public String paymentMethod;    // Phương thức thanh toán
    public Date paymentDate;       // Thời gian thanh toán
    public double totalAmount;      // Tổng số tiền thanh toán
    public double amountGiven;      // Số tiền khách đưa
}
