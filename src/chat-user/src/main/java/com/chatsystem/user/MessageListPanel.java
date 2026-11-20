package com.chatsystem.user;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Scrollable transcript with responsive left/right bubbles, multi-message selection,
 * in-memory hide/delete preview, and search-result jump-to-context support.
 */
final class MessageListPanel extends JScrollPane {
    private final Transcript transcript = new Transcript();
    private ChatData.Conversation currentConversation = ChatData.CONVERSATIONS.get(0);
    private List<ChatData.Message> messages = List.of();
    private final Set<String> selectedMessageIds = new LinkedHashSet<>();
    private final Map<String, Set<String>> hiddenByConversation = new HashMap<>();
    private final Map<String, JComponent> renderedRowsById = new LinkedHashMap<>();
    private String term = "";
    private String highlightedMessageId;
    private int visibleCount, lastWidth;
    private boolean global;
    private Runnable onSelectionChanged;
    private Consumer<ChatData.MessageTarget> onJumpToMessage;

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

    void setOnSelectionChanged(Runnable listener) {
        this.onSelectionChanged = listener;
    }

    void setOnJumpToMessage(Consumer<ChatData.MessageTarget> listener) {
        this.onJumpToMessage = listener;
    }

    void conversation(ChatData.Conversation conversation) {
        this.currentConversation = conversation;
        this.messages = ChatData.messages(conversation);
        this.selectedMessageIds.clear();
        this.highlightedMessageId = null;
        render();
        notifySelectionChanged();
    }

    void filter(String query, boolean allHistory) {
        term = query.strip().toLowerCase(Locale.ROOT);
        global = allHistory;
        selectedMessageIds.clear();
        highlightedMessageId = null;
        render();
        notifySelectionChanged();
    }

    int visibleCount() { return visibleCount; }
    boolean hasSelection() { return !selectedMessageIds.isEmpty(); }
    int selectedCount() { return selectedMessageIds.size(); }
    Set<String> selectedMessageIds() { return Collections.unmodifiableSet(selectedMessageIds); }
    String highlightedMessageId() { return highlightedMessageId; }

    void toggleSelection(String messageId) {
        if (messageId == null || messageId.isBlank()) return;
        if (!selectedMessageIds.remove(messageId)) {
            selectedMessageIds.add(messageId);
        }
        render();
        notifySelectionChanged();
    }

    void clearSelection() {
        if (!selectedMessageIds.isEmpty()) {
            selectedMessageIds.clear();
            render();
            notifySelectionChanged();
        }
    }

    int hideSelectedMessages() {
        int count = selectedMessageIds.size();
        if (count == 0) return 0;
        hiddenByConversation
                .computeIfAbsent(currentConversation.username(), k -> new LinkedHashSet<>())
                .addAll(selectedMessageIds);
        selectedMessageIds.clear();
        highlightedMessageId = null;
        render();
        notifySelectionChanged();
        return count;
    }

    void hideAllInCurrentConversation() {
        Set<String> hidden = hiddenByConversation.computeIfAbsent(currentConversation.username(), k -> new LinkedHashSet<>());
        for (ChatData.Message m : messages) {
            hidden.add(m.id());
        }
        selectedMessageIds.clear();
        highlightedMessageId = null;
        render();
        notifySelectionChanged();
    }

    void highlightAndScrollTo(String messageId) {
        this.highlightedMessageId = messageId;
        render();
        validate();
        scrollToMessage(messageId);
        SwingUtilities.invokeLater(() -> scrollToMessage(messageId));
    }

    private void scrollToMessage(String messageId) {
        JComponent row = renderedRowsById.get(messageId);
        if (row == null) return;
        transcript.doLayout();
        validate();
        Rectangle bounds = row.getBounds();
        int pad = 64;
        Rectangle withContext = new Rectangle(
            bounds.x,
            Math.max(0, bounds.y - pad),
            bounds.width,
            bounds.height + pad * 2
        );
        transcript.scrollRectToVisible(withContext);
    }

    private boolean isHidden(String conversationUsername, String messageId) {
        Set<String> hidden = hiddenByConversation.get(conversationUsername);
        return hidden != null && hidden.contains(messageId);
    }

    private void notifySelectionChanged() {
        if (onSelectionChanged != null) {
            onSelectionChanged.run();
        }
    }

    private void render() {
        transcript.removeAll();
        renderedRowsById.clear();
        transcript.setBorder(BorderFactory.createEmptyBorder(18, 16, 16, 16));
        int available = Math.max(280, getViewport().getWidth());
        int bubbleWidth = Math.max(150, Math.min(440, (available - 90) * 65 / 100));
        boolean searchMode = !term.isEmpty();

        JLabel day = new JLabel(
            global && searchMode ? "Kết quả trong toàn bộ lịch sử mẫu"
                : searchMode ? "Kết quả trong cuộc trò chuyện này"
                : "Hôm nay"
        );
        day.setForeground(Ui.MUTED);
        day.setFont(day.getFont().deriveFont(12f));
        JPanel dateRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        dateRow.setOpaque(false);
        dateRow.add(day);
        append(dateRow);

        visibleCount = 0;
        if (global) {
            for (var c : ChatData.CONVERSATIONS) {
                List<ChatData.Message> convMsgs = ChatData.messages(c);
                for (int i = 0; i < convMsgs.size(); i++) {
                    ChatData.Message m = convMsgs.get(i);
                    if (!isHidden(c.username(), m.id()) && matches(m)) {
                        ChatData.MessageTarget target = new ChatData.MessageTarget(c.username(), m.id(), i);
                        addBubble(m, target, bubbleWidth, c.name(), searchMode);
                        visibleCount++;
                    }
                }
            }
        } else {
            for (int i = 0; i < messages.size(); i++) {
                ChatData.Message m = messages.get(i);
                if (!isHidden(currentConversation.username(), m.id()) && matches(m)) {
                    ChatData.MessageTarget target = new ChatData.MessageTarget(currentConversation.username(), m.id(), i);
                    addBubble(m, target, bubbleWidth, null, searchMode);
                    visibleCount++;
                }
            }
        }
        if (visibleCount == 0) {
            JLabel empty = new JLabel(
                searchMode ? "Không tìm thấy tin nhắn phù hợp."
                    : "Chưa có tin nhắn hiển thị trong cuộc trò chuyện này."
            );
            empty.setForeground(Ui.MUTED);
            JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER));
            row.setOpaque(false);
            row.add(empty);
            append(row);
        }
        transcript.revalidate();
        transcript.repaint();
    }

    private boolean matches(ChatData.Message message) {
        return message.text().toLowerCase(Locale.ROOT).contains(term);
    }

    private void addBubble(
            ChatData.Message message,
            ChatData.MessageTarget target,
            int width,
            String sourceLabel,
            boolean searchMode
    ) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(10, 0, 6, 0));
        JPanel bubbleGroup = new JPanel(new BorderLayout(0, 5));
        bubbleGroup.setOpaque(false);

        if (sourceLabel != null) {
            JLabel label = new JLabel(sourceLabel);
            label.setForeground(Ui.MUTED);
            label.setFont(label.getFont().deriveFont(Font.BOLD, 12f));
            bubbleGroup.add(label, BorderLayout.NORTH);
        }

        boolean isSelected = !searchMode && selectedMessageIds.contains(message.id());
        boolean isHighlighted = !searchMode && message.id().equals(highlightedMessageId);
        Runnable selectHandler = searchMode ? null : () -> toggleSelection(message.id());
        Runnable jumpHandler = (searchMode && onJumpToMessage != null) ? () -> onJumpToMessage.accept(target) : null;

        MessageBubble bubble = new MessageBubble(
            message,
            width,
            isSelected,
            isHighlighted,
            selectHandler,
            jumpHandler,
            searchMode ? target : null
        );
        bubbleGroup.add(bubble, BorderLayout.CENTER);
        row.add(bubbleGroup, message.outgoing() ? BorderLayout.EAST : BorderLayout.WEST);
        append(row);
        if (!searchMode) {
            renderedRowsById.put(message.id(), row);
        }
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
