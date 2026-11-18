package com.chatsystem.user;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;
import java.util.Locale;

/** Scrollable transcript with responsive left/right bubbles and preview search. */
final class MessageListPanel extends JScrollPane {
    private final Transcript transcript = new Transcript();
    private List<ChatData.Message> messages = List.of();
    private String term = "";
    private int selected = -1, visibleCount, lastWidth;
    private boolean global;
    private String selectedConversation;

    MessageListPanel() {
        setViewportView(transcript);
        setBorder(BorderFactory.createEmptyBorder());
        setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
        getVerticalScrollBar().setUnitIncrement(20);
        getViewport().setBackground(Color.WHITE);
        getViewport().addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                int width = getViewport().getWidth();
                if (width > 0 && width != lastWidth) { lastWidth = width; render(); }
            }
        });
    }
    @Override public void doLayout() {
        super.doLayout();
        int width = getViewport().getWidth();
        if (width > 0 && width != lastWidth) {
            lastWidth = width;
            render();
        }
    }
    void conversation(ChatData.Conversation conversation) {
        selectedConversation = conversation.name();
        messages = ChatData.messages(conversation); selected = -1; render();
    }
    void filter(String query, boolean allHistory) {
        term = query.strip().toLowerCase(Locale.ROOT); global = allHistory; selected = -1; render();
    }
    int visibleCount() { return visibleCount; }
    boolean hasSelection() { return selected >= 0; }

    private void render() {
        transcript.removeAll();
        transcript.setBorder(BorderFactory.createEmptyBorder(22, 16, 16, 16));
        int available = Math.max(280, getViewport().getWidth());
        int bubbleWidth = Math.max(150, Math.min(440, (available - 90) * 65 / 100));
        JLabel day = new JLabel(global ? "Kết quả trong toàn bộ lịch sử mẫu" : "Hôm nay");
        day.setForeground(Ui.MUTED); day.setFont(day.getFont().deriveFont(12f));
        JPanel dateRow = new JPanel(new FlowLayout(FlowLayout.CENTER)); dateRow.setOpaque(false); dateRow.add(day);
        append(dateRow);
        visibleCount = 0;
        if (global) {
            for (var c : ChatData.CONVERSATIONS) {
                for (int i = 0; i < ChatData.messages(c).size(); i++) {
                    ChatData.Message m = ChatData.messages(c).get(i);
                    if (matches(m)) { addBubble(m, i, bubbleWidth, c.name()); visibleCount++; }
                }
            }
        } else {
            for (int i = 0; i < messages.size(); i++) {
                if (matches(messages.get(i))) { addBubble(messages.get(i), i, bubbleWidth, null); visibleCount++; }
            }
        }
        if (visibleCount == 0) {
            JLabel empty = new JLabel("Không tìm thấy tin nhắn phù hợp.");
            empty.setForeground(Ui.MUTED);
            JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER)); row.setOpaque(false); row.add(empty); append(row);
        }
        transcript.revalidate(); transcript.repaint();
    }
    private boolean matches(ChatData.Message message) { return message.text().toLowerCase(Locale.ROOT).contains(term); }
    private void addBubble(ChatData.Message message, int index, int width, String source) {
        JPanel row = new JPanel(new BorderLayout()); row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(14, 0, 8, 0));
        JPanel bubbleGroup = new JPanel(new BorderLayout(0, 5)); bubbleGroup.setOpaque(false);
        if (source != null) { JLabel label = new JLabel(source); label.setForeground(Ui.MUTED); bubbleGroup.add(label, BorderLayout.NORTH); }
        MessageBubble bubble = new MessageBubble(message, width, () -> { selected = index; selectedConversation = source == null ? selectedConversation : source; render(); });
        if (selected == index && (source == null || source.equals(selectedConversation))) {
            bubble.setBorder(BorderFactory.createLineBorder(Ui.BLUE, 2));
        }
        bubbleGroup.add(bubble);
        row.add(bubbleGroup, message.outgoing() ? BorderLayout.EAST : BorderLayout.WEST);
        append(row);
    }
    private void append(JComponent component) {
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        component.setMaximumSize(new Dimension(Integer.MAX_VALUE, component.getPreferredSize().height));
        transcript.add(component);
    }
    private static final class Transcript extends JPanel implements Scrollable {
        Transcript() { setBackground(Color.WHITE); setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); }
        public Dimension getPreferredScrollableViewportSize() { return new Dimension(600, 500); }
        public int getScrollableUnitIncrement(Rectangle r, int orientation, int direction) { return 20; }
        public int getScrollableBlockIncrement(Rectangle r, int orientation, int direction) { return Math.max(20, r.height - 40); }
        public boolean getScrollableTracksViewportWidth() { return true; }
        public boolean getScrollableTracksViewportHeight() { return false; }
    }
}
