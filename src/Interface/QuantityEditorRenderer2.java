package Interface;
import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class QuantityEditorRenderer2 extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {
    private JPanel panel;
    private JButton btnIncrease;
    private JButton btnDecrease;
    private JLabel lblQuantity;
    private int quantity;
    private JTable table;

    public QuantityEditorRenderer2(JTable table) {
        this.table = table;
        panel = new JPanel(new BorderLayout(5, 0));
        btnIncrease = new JButton("+");
        btnDecrease = new JButton("-");
        lblQuantity = new JLabel(String.valueOf(quantity), SwingConstants.CENTER);

        // Thiết lập hành động khi nhấn nút tăng
        btnIncrease.addActionListener(e -> {
            int currentRow = table.getEditingRow();
            int maxQuantity = (int) table.getValueAt(currentRow, table.getEditingColumn() - 1); // Cột bên cạnh

            if (quantity < maxQuantity) { // Kiểm tra xem có nhỏ hơn giá trị cột bên cạnh không
                quantity++;
                lblQuantity.setText(String.valueOf(quantity)); // Cập nhật số lượng hiển thị
                stopCellEditing();
            }
        });

        // Thiết lập hành động khi nhấn nút giảm
        btnDecrease.addActionListener(e -> {
            if (quantity > 0) {
                quantity--;
                lblQuantity.setText(String.valueOf(quantity)); // Cập nhật số lượng hiển thị
                stopCellEditing();
            }
        });

        // Đặt các thành phần vào panel
        panel.add(btnDecrease, BorderLayout.WEST);
        panel.add(lblQuantity, BorderLayout.CENTER);
        panel.add(btnIncrease, BorderLayout.EAST);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (value instanceof Integer) {
            quantity = (Integer) value;
            lblQuantity.setText(String.valueOf(quantity));
        }
        return panel;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        if (value instanceof Integer) {
            quantity = (Integer) value;
            lblQuantity.setText(String.valueOf(quantity));
        }
        return panel;
    }

    @Override
    public Object getCellEditorValue() {
        return quantity;
    }
}
