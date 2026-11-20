package com.chatsystem.user;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.Locale;
import java.util.function.Consumer;

final class PeoplePanel extends JPanel {
    final JTable table;
    final JTextField search = Ui.input("Tìm tên hoặc tên đăng nhập");
    final JComboBox<String> status = new JComboBox<>(new String[]{"Tất cả", "Trực tuyến", "Ngoại tuyến"});

    PeoplePanel(String kind, Consumer<String> openChatForTarget, Consumer<String> createGroupWithTarget) {
        super(new BorderLayout());
        boolean friends = kind.equals("Bạn bè"), requests = kind.equals("Yêu cầu kết bạn");
        JPanel p = Ui.page(kind, friends ? "Giữ liên lạc với những người bạn của bạn"
                : requests ? "Những người đang muốn kết nối với bạn"
                : "Tìm và kết nối với mọi người trong ChatSystem");
        table = Ui.table(
            requests ? new String[]{"Tên đăng nhập", "Họ và tên", "Lời nhắn", "Ngày gửi"}
                     : new String[]{"Tên đăng nhập", "Họ và tên", "Trạng thái"},
            friends ? MockData.friends() : requests ? MockData.requests() : MockData.people()
        );
        JPanel body = new JPanel(new BorderLayout(8, 8));
        JPanel toolbar = Ui.row(search);
        if (friends) toolbar.add(status);
        body.add(toolbar, BorderLayout.NORTH);
        body.add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel actions = Ui.row();
        Runnable openSelectedChat = () -> openChatForTarget.accept(selectedUsername());
        Runnable openCreateGroup = () -> createGroupWithTarget.accept(selectedUsername());
        if (friends) {
            actions.add(Ui.button("Chat", openSelectedChat));
            actions.add(Ui.button("Tạo nhóm", openCreateGroup));
            actions.add(Ui.button("Hủy kết bạn", () -> confirm("Hủy kết bạn")));
            actions.add(Ui.button("Chặn người dùng", () -> confirm("Chặn người dùng")));
        } else if (requests) {
            actions.add(Ui.button("Đồng ý", () -> confirm("Đồng ý kết bạn")));
            actions.add(Ui.button("Từ chối", () -> confirm("Từ chối kết bạn")));
        } else {
            actions.add(Ui.button("Kết bạn", () -> confirm("Gửi yêu cầu kết bạn")));
            actions.add(Ui.button("Chat", openSelectedChat));
            actions.add(Ui.button("Tạo nhóm", openCreateGroup));
        }
        body.add(actions, BorderLayout.SOUTH);
        p.add(body);
        add(p);
        search.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filter(friends); }
            public void removeUpdate(DocumentEvent e) { filter(friends); }
            public void changedUpdate(DocumentEvent e) { filter(friends); }
        });
        status.addActionListener(e -> filter(friends));
    }

    /** Returns the selected row's normalized username via model index conversion (safe under filtering/sorting). */
    String selectedUsername() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return "";
        int modelRow = table.convertRowIndexToModel(viewRow);
        return table.getModel().getValueAt(modelRow, 0).toString().strip().replace("@", "");
    }

    private void filter(boolean friends) {
        @SuppressWarnings("unchecked")
        TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>) table.getRowSorter();
        String term = search.getText().strip().toLowerCase(Locale.ROOT), state = (String) status.getSelectedItem();
        sorter.setRowFilter(new RowFilter<>() {
            public boolean include(Entry<? extends TableModel, ? extends Integer> e) {
                return (e.getStringValue(0) + " " + e.getStringValue(1)).toLowerCase(Locale.ROOT).contains(term)
                        && (!friends || state.equals("Tất cả") || e.getStringValue(2).equals(state));
            }
        });
    }

    private void confirm(String action) {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một người dùng.");
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        String name = table.getModel().getValueAt(modelRow, 1).toString();
        if (JOptionPane.showConfirmDialog(this, action + " với " + name + "?\nThao tác minh họa, không lưu thay đổi.", action, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            Ui.demo(this);
        }
    }
}
