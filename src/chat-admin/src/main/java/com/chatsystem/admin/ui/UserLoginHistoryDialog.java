package com.chatsystem.admin.ui;

import com.chatsystem.admin.model.LoginRecord;
import com.chatsystem.admin.model.User;
import com.chatsystem.admin.theme.AppTheme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.List;

/**
 * Modal dialog displaying the chronological login history for a selected user account.
 */
public class UserLoginHistoryDialog extends JDialog {

    public UserLoginHistoryDialog(Window owner, User user, List<LoginRecord> loginRecords) {
        super(
                owner,
                "Lịch sử đăng nhập — " + user.getFullName() + " (@" + user.getUsername() + ")",
                ModalityType.APPLICATION_MODAL
        );

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppTheme.SURFACE_CARD);

        root.add(createHeader(user, loginRecords.size()), BorderLayout.NORTH);
        root.add(createTableSection(loginRecords), BorderLayout.CENTER);
        root.add(createFooter(loginRecords.size()), BorderLayout.SOUTH);

        setContentPane(root);
        setSize(new Dimension(760, 480));
        setMinimumSize(new Dimension(680, 400));
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    private JPanel createHeader(User user, int count) {
        JPanel header = new JPanel(new BorderLayout(AppTheme.SPACING_MD, 0));
        header.setBackground(AppTheme.SURFACE_CARD);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.BORDER),
                BorderFactory.createEmptyBorder(
                        AppTheme.SPACING_MD + 2,
                        AppTheme.CONTENT_PADDING,
                        AppTheme.SPACING_MD,
                        AppTheme.CONTENT_PADDING
                )
        ));

        JPanel textCol = new JPanel();
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        textCol.setOpaque(false);

        JLabel titleLabel = new JLabel("Lịch sử đăng nhập của người dùng");
        titleLabel.setFont(AppTheme.FONT_SECTION_TITLE);
        titleLabel.setForeground(AppTheme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subLabel = new JLabel(
                user.getFullName() + " (@" + user.getUsername() + ")  •  " + user.getEmail()
                        + "  •  TK: " + user.getAccountStatusLabel()
                        + " (" + user.getPresenceStatusLabel() + ")"
        );
        subLabel.setFont(AppTheme.FONT_CAPTION);
        subLabel.setForeground(AppTheme.TEXT_SECONDARY);
        subLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        textCol.add(titleLabel);
        textCol.add(Box.createVerticalStrut(4));
        textCol.add(subLabel);

        JLabel badgeLabel = new JLabel("Tổng số phiên: " + count);
        badgeLabel.setFont(AppTheme.FONT_CAPTION_BOLD);
        badgeLabel.setForeground(AppTheme.PRIMARY);
        badgeLabel.setOpaque(true);
        badgeLabel.setBackground(AppTheme.PRIMARY_SOFT_BG);
        badgeLabel.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

        JPanel badgeWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        badgeWrapper.setOpaque(false);
        badgeWrapper.add(badgeLabel);

        header.add(textCol, BorderLayout.CENTER);
        header.add(badgeWrapper, BorderLayout.EAST);
        return header;
    }

    private JPanel createTableSection(List<LoginRecord> loginRecords) {
        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(AppTheme.PAGE_BG);
        body.setBorder(BorderFactory.createEmptyBorder(
                AppTheme.SPACING_MD,
                AppTheme.CONTENT_PADDING,
                AppTheme.SPACING_MD,
                AppTheme.CONTENT_PADDING
        ));

        JTable table = new JTable(new LoginHistoryTableModel(loginRecords));
        AppTheme.configureSharedTable(table);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        leftRenderer.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(0).setMaxWidth(60);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER, 1, true));
        scrollPane.getViewport().setBackground(AppTheme.SURFACE_CARD);

        body.add(scrollPane, BorderLayout.CENTER);
        return body;
    }

    private JPanel createFooter(int count) {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(AppTheme.SURFACE_CARD);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, AppTheme.BORDER),
                BorderFactory.createEmptyBorder(
                        AppTheme.SPACING_SM + 4,
                        AppTheme.CONTENT_PADDING,
                        AppTheme.SPACING_SM + 4,
                        AppTheme.CONTENT_PADDING
                )
        ));

        String info = count == 0
                ? "Người dùng này chưa có bản ghi đăng nhập nào trong dữ liệu mẫu."
                : "Hiển thị theo thứ tự thời gian đăng nhập mới nhất trước (Múi giờ Asia/Ho_Chi_Minh).";
        JLabel noteLabel = new JLabel(info);
        noteLabel.setFont(AppTheme.FONT_CAPTION);
        noteLabel.setForeground(AppTheme.TEXT_SECONDARY);

        JButton closeButton = AppTheme.createSecondaryButton("Đóng", 100);
        closeButton.addActionListener(e -> dispose());

        footer.add(noteLabel, BorderLayout.WEST);
        footer.add(closeButton, BorderLayout.EAST);
        return footer;
    }

    private static final class LoginHistoryTableModel extends AbstractTableModel {

        private static final String[] COLUMNS = {
                "STT",
                "Thời gian đăng nhập",
                "Thời gian đăng xuất",
                "Địa chỉ IP",
                "Trạng thái phiên"
        };

        private final List<LoginRecord> records;

        private LoginHistoryTableModel(List<LoginRecord> records) {
            this.records = records;
        }

        @Override
        public int getRowCount() {
            return records.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMNS.length;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMNS[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            LoginRecord r = records.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> rowIndex + 1;
                case 1 -> r.getFormattedLoginAt();
                case 2 -> r.getFormattedLogoutAt();
                case 3 -> r.getIpAddress();
                case 4 -> r.getLogoutAt() == null ? "Phiên đang mở" : "Đã kết thúc";
                default -> "";
            };
        }
    }
}
