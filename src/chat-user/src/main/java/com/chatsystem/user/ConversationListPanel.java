package com.chatsystem.user;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.Locale;
import java.util.function.Consumer;

/** Searchable middle column of conversation cards. */
final class ConversationListPanel extends JPanel {
    final JTextField search = Ui.input("Tìm cuộc trò chuyện...");
    final JList<ChatData.Conversation> list = new JList<>() {
        public boolean getScrollableTracksViewportWidth() { return true; }
    };
    private final DefaultListModel<ChatData.Conversation> model = new DefaultListModel<>();
    private final Consumer<ChatData.Conversation> onSelect;
    private boolean groupsOnly;

    ConversationListPanel(Consumer<ChatData.Conversation> onSelect) {
        super(new BorderLayout(0, 14));
        this.onSelect = onSelect;
        setBackground(Ui.BG);
        setPreferredSize(new Dimension(295, 0));
        setMinimumSize(new Dimension(245, 0));
        JPanel heading = new JPanel(new BorderLayout(0, 14));
        heading.setOpaque(false);
        JPanel titles = new JPanel(new GridLayout(0, 1, 0, 6));
        titles.setOpaque(false);
        JLabel title = new JLabel("Trò chuyện");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24));
        JLabel subtitle = new JLabel("Danh sách cuộc trò chuyện");
        subtitle.setForeground(Ui.MUTED);
        titles.add(title); titles.add(subtitle);
        heading.add(titles, BorderLayout.NORTH);
        search.setPreferredSize(new Dimension(0, 44));
        heading.add(search, BorderLayout.SOUTH);
        add(heading, BorderLayout.NORTH);
        list.setModel(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFixedCellHeight(96);
        list.setCellRenderer(new ConversationRenderer());
        list.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && list.getSelectedValue() != null) onSelect.accept(list.getSelectedValue());
        });
        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createLineBorder(Ui.BORDER));
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll);
        search.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filter(); }
            public void removeUpdate(DocumentEvent e) { filter(); }
            public void changedUpdate(DocumentEvent e) { filter(); }
        });
        filter();
    }

    void groupsOnly(boolean value) { groupsOnly = value; filter(); }
    void select(int index) {
        groupsOnly = false;
        search.setText("");
        filter();
        list.setSelectedValue(ChatData.CONVERSATIONS.get(index), true);
    }
    private void filter() {
        ChatData.Conversation selected = list.getSelectedValue();
        String term = search.getText().strip().toLowerCase(Locale.ROOT);
        model.clear();
        for (var c : ChatData.CONVERSATIONS) {
            if ((!groupsOnly || c.group()) && c.name().toLowerCase(Locale.ROOT).contains(term)) model.addElement(c);
        }
        if (selected != null && model.contains(selected)) list.setSelectedValue(selected, true);
        else if (!model.isEmpty()) list.setSelectedIndex(0);
    }

    private static final class ConversationRenderer extends JPanel implements ListCellRenderer<ChatData.Conversation> {
        private final JLabel avatar = new JLabel(), name = new JLabel(), preview = new JLabel();
        private final JLabel time = new JLabel(), status = new JLabel();
        ConversationRenderer() {
            super(new BorderLayout(12, 0));
            setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Ui.BORDER),
                    BorderFactory.createEmptyBorder(16, 14, 16, 14)));
            add(avatar, BorderLayout.WEST);
            JPanel middle = new JPanel(new GridLayout(2, 1, 0, 8)); middle.setOpaque(false);
            name.setFont(name.getFont().deriveFont(Font.BOLD)); preview.setForeground(Ui.MUTED);
            middle.add(name); middle.add(preview); add(middle);
            JPanel right = new JPanel(new GridLayout(2, 1, 0, 8)); right.setOpaque(false);
            time.setFont(time.getFont().deriveFont(11f)); time.setForeground(Ui.MUTED);
            status.setFont(status.getFont().deriveFont(11f)); time.setHorizontalAlignment(SwingConstants.RIGHT);
            status.setHorizontalAlignment(SwingConstants.RIGHT); right.add(time); right.add(status); add(right, BorderLayout.EAST);
        }
        public Component getListCellRendererComponent(JList<? extends ChatData.Conversation> list,
                ChatData.Conversation value, int index, boolean selected, boolean focused) {
            setBackground(selected ? Ui.SOFT_BLUE : Color.WHITE);
            avatar.setIcon(new AvatarIcon(value.initials(), value.color(), 44));
            name.setText(value.name()); preview.setText(value.preview()); time.setText(value.time());
            status.setText(value.online() ? "● Online" : "● Offline");
            status.setForeground(value.online() ? Ui.SUCCESS : Ui.MUTED);
            return this;
        }
    }
}
