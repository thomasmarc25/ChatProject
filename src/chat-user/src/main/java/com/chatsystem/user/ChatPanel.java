package com.chatsystem.user;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;

/** Main Swing chat screen matching the team's three-column reference. */
final class ChatPanel extends JPanel {
    final MessageListPanel messages = new MessageListPanel();
    final JTextField search = Ui.input("Tìm nội dung tin nhắn...");
    final JComboBox<String> scope = new JComboBox<>(new String[]{"Cuộc trò chuyện này", "Toàn bộ lịch sử"});
    final JTextArea composer = new JTextArea(2, 20);
    final JLabel title = new JLabel(), status = new JLabel(), avatar = new JLabel();
    final JPanel searchBar = new JPanel(new BorderLayout(8, 0));
    final JLabel searchSummary = new JLabel();
    final ConversationListPanel conversationList;
    final UserSessionProfile session;
    ChatData.Conversation current = ChatData.CONVERSATIONS.get(0);
    boolean group;

    ChatPanel() {
        this(UserSessionProfile.DEMO_USER);
    }

    ChatPanel(UserSessionProfile session) {
        super(new BorderLayout(12, 0));
        this.session = session != null ? session : UserSessionProfile.DEMO_USER;
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setBackground(Ui.BG);

        JPanel conversation = new JPanel(new BorderLayout());
        conversation.setBackground(Color.WHITE);
        conversation.setBorder(BorderFactory.createLineBorder(Ui.BORDER));
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        JPanel header = new JPanel(new BorderLayout(14, 0));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Ui.BORDER),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        header.add(avatar, BorderLayout.WEST);
        JPanel identity = new JPanel(new GridLayout(2, 1, 0, 5));
        identity.setOpaque(false);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 19));
        status.setFont(status.getFont().deriveFont(12f));
        identity.add(title); identity.add(status); header.add(identity);

        JButton find = compact("Tìm tin", () -> showSearch(!searchBar.isVisible()));
        JButton create = compact("+ Tạo nhóm", () -> openDialog("create"));
        JButton details = compact("Chi tiết", () -> {});
        details.addActionListener(e -> showDetails(details));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.setOpaque(false); actions.add(find); actions.add(create); actions.add(details);
        header.add(actions, BorderLayout.EAST);
        top.add(header, BorderLayout.NORTH);

        search.setColumns(12);
        searchBar.setBackground(Color.WHITE);
        searchBar.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        searchBar.add(search);
        JPanel searchActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0)); searchActions.setOpaque(false);
        searchActions.add(scope); searchActions.add(compact("Tìm", this::searchMessages));
        searchBar.add(searchActions, BorderLayout.EAST);
        searchBar.setVisible(false);
        JPanel searchWrapper = new JPanel(new BorderLayout()); searchWrapper.setOpaque(false);
        searchWrapper.add(searchBar, BorderLayout.NORTH);
        searchSummary.setForeground(Ui.MUTED);
        searchSummary.setBorder(BorderFactory.createEmptyBorder(0, 14, 6, 14));
        searchSummary.setVisible(false);
        searchWrapper.add(searchSummary, BorderLayout.SOUTH); top.add(searchWrapper);
        search.addActionListener(e -> searchMessages());
        conversation.add(top, BorderLayout.NORTH);
        conversation.add(messages);

        JPanel bottom = new JPanel(new BorderLayout(10, 0));
        bottom.setBackground(Color.WHITE);
        bottom.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Ui.BORDER),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        composer.setLineWrap(true); composer.setWrapStyleWord(true);
        composer.setBorder(BorderFactory.createEmptyBorder(9, 10, 9, 10));
        composer.putClientProperty("JTextArea.placeholderText", "Nhập tin nhắn...");
        composer.getAccessibleContext().setAccessibleName("Soạn tin nhắn");
        JScrollPane input = new JScrollPane(composer);
        input.setBorder(BorderFactory.createLineBorder(Ui.BORDER));
        bottom.add(input);
        JButton send = Ui.primaryButton("Gửi", () -> Ui.demo(this));
        send.setPreferredSize(new Dimension(66, 48));
        bottom.add(send, BorderLayout.EAST);
        conversation.add(bottom, BorderLayout.SOUTH);

        conversationList = new ConversationListPanel(this::applyConversation);
        add(conversationList, BorderLayout.WEST);
        add(conversation);
        applyConversation(current);
    }

    private JButton compact(String text, Runnable action) {
        JButton b = new JButton(text);
        b.addActionListener(e -> action.run());
        b.putClientProperty("JButton.minimumWidth", 0);
        b.setMargin(new Insets(6, 10, 6, 10));
        b.setPreferredSize(new Dimension(b.getPreferredSize().width + 16, 36));
        return b;
    }

    @Override public void doLayout() {
        int width = getWidth() < 1050 ? 255 : 295;
        if (conversationList.getPreferredSize().width != width)
            conversationList.setPreferredSize(new Dimension(width, 0));
        super.doLayout();
    }

    void selectThread(int index) {
        // Stable indices for UI capture: personal (0), group (1), encrypted group (5).
        conversationList.select(index == 2 ? 5 : index);
    }

    void selectConversationByTarget(String usernameOrName) {
        if (usernameOrName == null || usernameOrName.isBlank()) return;
        String clean = usernameOrName.strip().replace("@", "").toLowerCase(Locale.ROOT);
        for (int i = 0; i < ChatData.CONVERSATIONS.size(); i++) {
            ChatData.Conversation c = ChatData.CONVERSATIONS.get(i);
            if (c.username().equalsIgnoreCase(clean) || c.name().toLowerCase(Locale.ROOT).contains(clean)) {
                conversationList.select(i);
                return;
            }
        }
    }

    void showGroups() { conversationList.groupsOnly(true); }

    private void applyConversation(ChatData.Conversation conversation) {
        if (conversation == current && conversation.name().equals(title.getText())) {
            return;
        }
        current = conversation; group = conversation.group();
        title.setText(conversation.name());
        avatar.setIcon(new AvatarIcon(conversation.initials(), conversation.color(), 46));
        status.setText(conversation.encrypted() ? "● E2EE · " + conversation.members().size() + " thành viên · Dữ liệu mẫu"
                : group ? "● " + conversation.members().size() + " thành viên"
                : conversation.online() ? "● Đang hoạt động" : "● Ngoại tuyến");
        status.setForeground(conversation.online() ? Ui.SUCCESS : Ui.MUTED);
        search.setText(""); searchSummary.setVisible(false);
        messages.filter("", false); messages.conversation(conversation);
    }

    void showSearch(boolean visible) {
        searchBar.setVisible(visible);
        if (visible) search.requestFocusInWindow();
        else { search.setText(""); messages.filter("", false); searchSummary.setVisible(false); }
        revalidate(); repaint();
    }

    void searchMessages() {
        messages.filter(search.getText(), scope.getSelectedIndex() == 1);
        searchSummary.setText(messages.visibleCount() + " kết quả trong " +
                (scope.getSelectedIndex() == 1 ? "toàn bộ lịch sử mẫu" : "cuộc trò chuyện này"));
        searchSummary.setVisible(true);
        revalidate();
    }

    private void showDetails(JButton anchor) {
        JPopupMenu menu = new JPopupMenu();
        if (group) item(menu, "Quản lý nhóm / Thành viên / Mã hóa", () -> openDialog("group"));
        else item(menu, "Thông tin người dùng", () -> JOptionPane.showMessageDialog(this,
                current.name() + " (@" + current.username() + ")\n"
                + (current.online() ? "Đang hoạt động" : "Ngoại tuyến")
                + "\nThông tin mẫu đồng bộ với script/02_seed.sql."));
        menu.addSeparator();
        item(menu, "Gợi ý soạn tin bằng LLM", () -> openDialog("llm"));
        item(menu, "Xóa / ẩn tin đã chọn", () -> confirm("Xóa/ẩn tin nhắn chỉ với bạn"));
        item(menu, "Xóa lịch sử trò chuyện", () -> confirm("Xóa lịch sử chỉ với bạn"));
        item(menu, "Báo cáo spam", () -> openDialog("spam"));
        menu.show(anchor, 0, anchor.getHeight());
    }

    private void item(JPopupMenu menu, String name, Runnable action) {
        JMenuItem item = new JMenuItem(name); item.addActionListener(e -> action.run()); menu.add(item);
    }

    JDialog createDialog(String type) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog d = new JDialog(owner, switch (type) {
            case "llm" -> "Trợ lý soạn tin";
            case "spam" -> "Báo cáo spam";
            case "create" -> "Tạo nhóm chat";
            default -> "Quản lý nhóm chat";
        }, Dialog.ModalityType.APPLICATION_MODAL);
        JPanel p = Ui.form();
        if (type.equals("llm")) {
            JTextArea request = new JTextArea("Viết lời nhắc họp nhóm lịch sự vào 14 giờ ngày mai.", 3, 32);
            request.setLineWrap(true); request.setWrapStyleWord(true);
            JTextArea suggestion = new JTextArea("Chào cả nhóm, mình hẹn mọi người họp lúc 14 giờ ngày mai để kiểm tra giao diện nhé!", 4, 32);
            suggestion.setLineWrap(true); suggestion.setWrapStyleWord(true);
            Ui.field(p, 0, "Yêu cầu", new JScrollPane(request));
            Ui.field(p, 1, "", Ui.button("Xem gợi ý mẫu", () -> suggestion.setText("Chào cả nhóm, mình hẹn mọi người họp lúc 14 giờ ngày mai để kiểm tra giao diện nhé!")));
            Ui.field(p, 2, "Gợi ý (có thể sửa)", new JScrollPane(suggestion));
            Ui.field(p, 3, "", Ui.button("Đưa vào ô soạn tin", () -> { composer.setText(suggestion.getText()); d.dispose(); }));
            Ui.field(p, 4, "Lưu ý", new JLabel("Mẫu tĩnh; chưa kết nối LLM, không gửi dữ liệu."));
        } else if (type.equals("spam")) {
            boolean isSampleSpamThread = ChatData.SAMPLE_SPAM_REPORTED_USERNAME.equals(current.username());
            Ui.field(p, 0, "Người báo cáo", new JLabel(session.displayLabel()));
            Ui.field(p, 1, "Người bị báo cáo", new JLabel(current.name() + " (@" + current.username() + ")"));
            JComboBox<String> reasonBox = new JComboBox<>(new String[]{"Tin nhắn quảng cáo", "Lừa đảo", "Quấy rối", "Khác"});
            if (isSampleSpamThread) reasonBox.setSelectedItem("Lừa đảo");
            Ui.field(p, 2, "Lý do", reasonBox);
            JTextArea desc = new JTextArea(4, 30);
            desc.setLineWrap(true); desc.setWrapStyleWord(true);
            if (isSampleSpamThread) {
                desc.setText(ChatData.SAMPLE_SPAM_REASON + "\nBản chụp tin nhắn mẫu: \"" + ChatData.SAMPLE_SPAM_SNAPSHOT + "\"");
            }
            Ui.field(p, 3, "Mô tả & Bản chụp", new JScrollPane(desc));
            Ui.field(p, 4, "", Ui.primaryButton("Gửi báo cáo", () -> Ui.demo(d)));
        } else {
            boolean createMode = type.equals("create");
            JTextField name = Ui.input("Tên nhóm");
            name.setText(createMode ? "" : current.name());
            Ui.field(p, 0, "Tên nhóm", name);
            String[] memberItems = (createMode || current.members().isEmpty())
                    ? new String[]{
                        session.displayLabel() + " · Admin nhóm",
                        "Trần Thị Bích Ngọc (@tranthibich) · Thành viên",
                        "Lê Hoàng Nam (@lehoangnam) · Thành viên",
                        "Hoàng Quốc Việt (@hoangquocviet) · Thành viên"
                    }
                    : current.members().toArray(new String[0]);
            JList<String> members = new JList<>(memberItems);
            members.setVisibleRowCount(Math.min(5, memberItems.length));
            if (memberItems.length > 0) members.setSelectedIndex(0);
            Ui.field(p, 1, "Thành viên hiện tại", new JScrollPane(members));
            Ui.field(p, 2, "Chọn người để thêm", new JComboBox<>(new String[]{
                "Vũ Thị Phương Thảo (@vuthithao)",
                "Ngô Thùy Dương (@ngothuyduong)",
                "Đặng Quang Huy (@dangquanghuy)"
            }));
            Ui.field(p, 3, "", Ui.row(Ui.button("Thêm", () -> Ui.demo(d)), Ui.button("Xóa thành viên", () -> Ui.demo(d))));
            Ui.field(p, 4, "Quản trị nhóm", Ui.button("Gán admin", () -> Ui.demo(d)));
            JCheckBox encrypted = new JCheckBox("Bật mã hóa đầu cuối (thiết kế)");
            encrypted.setSelected(!createMode && current.encrypted());
            Ui.field(p, 5, "Bảo mật", encrypted);
            Ui.field(p, 6, "Trạng thái khóa", new JLabel("Chưa trao đổi khóa · Không có mã hóa thật ở phiên bản 1"));
            Ui.field(p, 7, "", Ui.primaryButton(createMode ? "Tạo nhóm" : "Lưu tên và tùy chọn", () -> Ui.demo(d)));
        }
        Ui.field(p, 9, "", Ui.button("Đóng", d::dispose));
        d.setContentPane(p); d.pack();
        d.setMinimumSize(new Dimension(660, 520));
        d.setSize(Math.max(660, d.getWidth()), Math.max(520, d.getHeight()));
        d.setLocationRelativeTo(owner);
        return d;
    }

    void openDialog(String type) { createDialog(type).setVisible(true); }

    private void confirm(String text) {
        if (!messages.hasSelection() && text.contains("tin nhắn")) {
            JOptionPane.showMessageDialog(this, "Chọn một tin nhắn trước.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, text + "?\nThao tác minh họa.", "Xác nhận", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            Ui.demo(this);
        }
    }
}
