package com.chatsystem.admin.ui;

import com.chatsystem.admin.model.User;
import com.chatsystem.admin.model.UserFriendRecord;
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
 * Modal dialog displaying the accepted direct friends list for a selected user account.
 */
public class UserFriendsDialog extends JDialog {

    public UserFriendsDialog(Window owner, User user, List<UserFriendRecord> friends) {
        super(
                owner,
                "Danh sách bạn bè — " + user.getFullName() + " (@" + user.getUsername() + ")",
                ModalityType.APPLICATION_MODAL
        );

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppTheme.SURFACE_CARD);

        root.add(createHeader(user, friends.size()), BorderLayout.NORTH);
        root.add(createTableSection(friends), BorderLayout.CENTER);
        root.add(createFooter(friends.size()), BorderLayout.SOUTH);

        setContentPane(root);
        setSize(new Dimension(840, 480));
        setMinimumSize(new Dimension(740, 400));
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

        JLabel titleLabel = new JLabel("Danh sách bạn bè trực tiếp của người dùng");
        titleLabel.setFont(AppTheme.FONT_SECTION_TITLE);
        titleLabel.setForeground(AppTheme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subLabel = new JLabel(
                user.getFullName() + " (@" + user.getUsername() + ")  •  " + user.getEmail()
                        + "  •  Quan hệ kết bạn đã chấp nhận (ACCEPTED)"
        );
        subLabel.setFont(AppTheme.FONT_CAPTION);
        subLabel.setForeground(AppTheme.TEXT_SECONDARY);
        subLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        textCol.add(titleLabel);
        textCol.add(Box.createVerticalStrut(4));
        textCol.add(subLabel);

        JLabel badgeLabel = new JLabel("Bạn bè trực tiếp: " + count);
        badgeLabel.setFont(AppTheme.FONT_CAPTION_BOLD);
        badgeLabel.setForeground(AppTheme.SUCCESS);
        badgeLabel.setOpaque(true);
        badgeLabel.setBackground(AppTheme.SUCCESS_BG);
        badgeLabel.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

        JPanel badgeWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        badgeWrapper.setOpaque(false);
        badgeWrapper.add(badgeLabel);

        header.add(textCol, BorderLayout.CENTER);
        header.add(badgeWrapper, BorderLayout.EAST);
        return header;
    }

    private JPanel createTableSection(List<UserFriendRecord> friends) {
        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(AppTheme.PAGE_BG);
        body.setBorder(BorderFactory.createEmptyBorder(
                AppTheme.SPACING_MD,
                AppTheme.CONTENT_PADDING,
                AppTheme.SPACING_MD,
                AppTheme.CONTENT_PADDING
        ));

        JTable table = new JTable(new FriendsTableModel(friends));
        AppTheme.configureSharedTable(table);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(115);
        table.getColumnModel().getColumn(2).setPreferredWidth(165);
        table.getColumnModel().getColumn(3).setPreferredWidth(205);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setPreferredWidth(95);
        table.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);
        table.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(6).setPreferredWidth(125);

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
                ? "Người dùng này hiện chưa có bạn bè trực tiếp nào."
                : "Các lời mời đang chờ (PENDING) và tài khoản đã xóa mềm không được tính vào danh sách này.";
        JLabel noteLabel = new JLabel(info);
        noteLabel.setFont(AppTheme.FONT_CAPTION);
        noteLabel.setForeground(AppTheme.TEXT_SECONDARY);

        JButton closeButton = AppTheme.createSecondaryButton("Đóng", 100);
        closeButton.addActionListener(e -> dispose());

        footer.add(noteLabel, BorderLayout.WEST);
        footer.add(closeButton, BorderLayout.EAST);
        return footer;
    }

    private static final class FriendsTableModel extends AbstractTableModel {

        private static final String[] COLUMNS = {
                "STT",
                "Tên đăng nhập",
                "Họ và tên",
                "Email",
                "Ngày kết bạn",
                "TT Tài khoản",
                "Phiên trực tuyến"
        };

        private final List<UserFriendRecord> friends;

        private FriendsTableModel(List<UserFriendRecord> friends) {
            this.friends = friends;
        }

        @Override
        public int getRowCount() {
            return friends.size();
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
            UserFriendRecord f = friends.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> rowIndex + 1;
                case 1 -> f.getFriendUsername();
                case 2 -> f.getFriendFullName();
                case 3 -> f.getFriendEmail();
                case 4 -> f.getFormattedFriendshipDate();
                case 5 -> f.getAccountStatusLabel();
                case 6 -> f.getPresenceStatusLabel();
                default -> "";
            };
        }
    }
}
