package com.chatsystem.user;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

/** Java Swing user workspace using the team's dark-sidebar reference. */
public final class UserApp extends JFrame {
    final CardLayout cards = new CardLayout();
    final JPanel content = new JPanel(cards);
    final ChatPanel chat = new ChatPanel();
    final AccountPanel account = new AccountPanel(() -> showPage("Tin nhắn"));
    final PeoplePanel friends = new PeoplePanel("Bạn bè", () -> showPage("Tin nhắn"), () -> chat.openDialog("create"));
    private final Map<String, JButton> navigation = new LinkedHashMap<>();

    public UserApp() {
        super("ChatSystem User - Màn hình chat chính");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1440, 900);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);

        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(Ui.NAV);
        sidebar.setPreferredSize(new Dimension(210, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(26, 12, 18, 12));
        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        JLabel brand = new JLabel("ChatSystem User");
        brand.setForeground(Color.WHITE);
        brand.setFont(brand.getFont().deriveFont(Font.BOLD, 21));
        brand.setAlignmentX(LEFT_ALIGNMENT);
        brand.setBorder(BorderFactory.createEmptyBorder(0, 0, 28, 0));
        top.add(brand);
        JSeparator separator = new JSeparator(); separator.setForeground(new Color(0x374151));
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1)); top.add(separator);
        top.add(Box.createVerticalStrut(28));

        content.add(chat, "Tin nhắn");
        content.add(friends, "Bạn bè");
        content.add(new PeoplePanel("Yêu cầu kết bạn", () -> showPage("Tin nhắn"), () -> chat.openDialog("create")), "Yêu cầu kết bạn");
        content.add(new PeoplePanel("Tìm người", () -> showPage("Tin nhắn"), () -> chat.openDialog("create")), "Tìm người");
        content.add(account, "Tài khoản");

        addNavigation(top, "Tin nhắn", "chat", () -> showPage("Tin nhắn"));
        addNavigation(top, "Bạn bè", "friends", () -> showPage("Bạn bè"));
        addNavigation(top, "Nhóm chat", "group", () -> showPage("Nhóm chat"));
        addNavigation(top, "Tìm kiếm", "search", () -> showPage("Tìm người"));
        addNavigation(top, "Yêu cầu kết bạn", "requests", () -> showPage("Yêu cầu kết bạn"));
        addNavigation(top, "Cài đặt", "settings", () -> showPage("Tài khoản"));
        sidebar.add(top, BorderLayout.NORTH);
        JPanel footer = new JPanel(new GridLayout(0, 1, 0, 7)); footer.setOpaque(false);
        JLabel who = new JLabel("Phúc · Phiên bản 1");
        who.setForeground(new Color(0xCBD5E1)); who.setFont(who.getFont().deriveFont(12f));
        JLabel note = new JLabel("Dữ liệu mẫu · Swing UI");
        note.setForeground(new Color(0x9CA3AF)); note.setFont(note.getFont().deriveFont(11f));
        JButton logout = new JButton("Đăng xuất");
        logout.setForeground(new Color(0xCBD5E1)); logout.setBorderPainted(false);
        logout.setContentAreaFilled(false); logout.setHorizontalAlignment(SwingConstants.LEFT);
        logout.addActionListener(e -> { account.tabs.setSelectedIndex(0); showPage("Tài khoản"); });
        footer.add(logout); footer.add(who); footer.add(note); sidebar.add(footer, BorderLayout.SOUTH);
        add(sidebar, BorderLayout.WEST); add(content);
        showPage("Tin nhắn");
    }

    private void addNavigation(JPanel parent, String name, String icon, Runnable action) {
        JButton button = new JButton(name, new NavigationIcon(icon));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setIconTextGap(12);
        button.setForeground(new Color(0xD1D5DB));
        button.setBackground(Ui.NAV);
        button.setBorder(BorderFactory.createEmptyBorder(14, 12, 14, 8));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setAlignmentX(LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        button.setPreferredSize(new Dimension(184, 54));
        button.addActionListener(e -> action.run());
        parent.add(button); parent.add(Box.createVerticalStrut(10));
        navigation.put(name, button);
    }

    void showPage(String name) {
        if (name.equals("Nhóm chat")) {
            cards.show(content, "Tin nhắn");
            chat.showGroups();
        } else {
            cards.show(content, name);
            if (name.equals("Tin nhắn")) chat.conversationList.groupsOnly(false);
        }
        String selected = switch (name) { case "Tìm người" -> "Tìm kiếm"; case "Tài khoản" -> "Cài đặt"; default -> name; };
        navigation.forEach((label, button) -> {
            boolean active = label.equals(selected);
            button.setBackground(active ? Ui.BLUE : Ui.NAV);
            button.setForeground(active ? Color.WHITE : new Color(0xD1D5DB));
            button.setFont(button.getFont().deriveFont(active ? Font.BOLD : Font.PLAIN));
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> { Ui.setup(); new UserApp().setVisible(true); });
    }
}
