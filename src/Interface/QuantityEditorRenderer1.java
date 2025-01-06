/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Interface;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
/**
 *
 * @author mSoSm
 */
public class QuantityEditorRenderer1 extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {
    private JPanel panel;
    private JButton btnIncrease;
    private JButton btnDecrease;
    private JLabel lblQuantity; // Nhãn để hiển thị số lượng
    private int quantity;

    public QuantityEditorRenderer1() {
        panel = new JPanel(new BorderLayout(5, 0));
        btnIncrease = new JButton("+");
        btnDecrease = new JButton("-");
        lblQuantity = new JLabel(String.valueOf(quantity), SwingConstants.CENTER); // Hiển thị số lượng ở giữa và căn giữa

        // Thiết lập hành động khi nhấn nút tăng
        btnIncrease.addActionListener(e -> {
            quantity++;
            lblQuantity.setText(String.valueOf(quantity)); // Cập nhật số lượng hiển thị
            stopCellEditing();
        });

        // Thiết lập hành động khi nhấn nút giảm
        btnDecrease.addActionListener(e -> {
            if (quantity > 0) {
                quantity--;
                lblQuantity.setText(String.valueOf(quantity)); // Cập nhật số lượng hiển thị
            }
            stopCellEditing();
        });

        // Đặt các thành phần vào panel
        panel.add(btnDecrease, BorderLayout.WEST);
        panel.add(lblQuantity, BorderLayout.CENTER); // Hiển thị số lượng ở giữa
        panel.add(btnIncrease, BorderLayout.EAST);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (value instanceof Integer) {
            quantity = (Integer) value;
            lblQuantity.setText(String.valueOf(quantity)); // Cập nhật số lượng hiển thị
        }
        return panel;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        if (value instanceof Integer) {
            quantity = (Integer) value;
            lblQuantity.setText(String.valueOf(quantity)); // Cập nhật số lượng hiển thị
        }
        return panel;
    }

    @Override
    public Object getCellEditorValue() {
        return quantity;
    }
}