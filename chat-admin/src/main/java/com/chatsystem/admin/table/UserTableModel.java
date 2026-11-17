package com.chatsystem.admin.table;

import com.chatsystem.admin.model.User;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Non-editable Swing table model for the Admin User Management JTable.
 * Columns: STT, Tên đăng nhập, Họ tên, Email, Ngày tạo, Trạng thái, Thao tác.
 */
public class UserTableModel extends AbstractTableModel {

    public static final int COL_INDEX = 0;
    public static final int COL_USERNAME = 1;
    public static final int COL_FULL_NAME = 2;
    public static final int COL_EMAIL = 3;
    public static final int COL_CREATED_AT = 4;
    public static final int COL_STATUS = 5;
    public static final int COL_ACTIONS = 6;

    private static final String[] COLUMN_NAMES = {
            "STT",
            "Tên đăng nhập",
            "Họ tên",
            "Email",
            "Ngày tạo",
            "Trạng thái",
            "Thao tác"
    };

    private final List<User> users = new ArrayList<>();

    public UserTableModel(List<User> initialUsers) {
        if (initialUsers != null) {
            this.users.addAll(initialUsers);
        }
    }

    public User getUserAt(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= users.size()) {
            return null;
        }
        return users.get(rowIndex);
    }

    @Override
    public int getRowCount() {
        return users.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        User user = users.get(rowIndex);
        return switch (columnIndex) {
            case COL_INDEX -> rowIndex + 1;
            case COL_USERNAME -> user.getUsername();
            case COL_FULL_NAME -> user.getFullName();
            case COL_EMAIL -> user.getEmail();
            case COL_CREATED_AT -> user.getFormattedCreatedAt();
            case COL_STATUS -> user.getDisplayStatus();
            case COL_ACTIONS -> "Sửa | Thao tác ▾";
            default -> "";
        };
    }
}
