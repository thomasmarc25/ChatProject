package com.chatsystem.user;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Selectable incoming/outgoing bubble with multi-select, highlight, and search-jump support. */
final class MessageBubble extends JPanel {
    MessageBubble(
            ChatData.Message message,
            int width,
            boolean selected,
            boolean highlighted,
            Runnable onSelect,
            Runnable onJumpToMessage,
            ChatData.MessageTarget jumpTarget
    ) {
        super(new BorderLayout(0, 6));
        setOpaque(selected || highlighted);
        if (highlighted) {
            setBackground(new Color(0xFF, 0xFB, 0xEB));
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Ui.WARNING, 2, true),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
            ));
        } else if (selected) {
            setBackground(Ui.SOFT_BLUE);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Ui.BLUE, 2, true),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
            ));
        }

        String text = message.text().replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\n", "<br>");
        JLabel body = new JLabel();
        // Swing HTML CSS pixels can differ from component pixels on Windows.
        // Measure the rendered label so a bubble never exceeds its layout width.
        int cssWidth = width;
        for (int pass = 0; pass < 3; pass++) {
            body.setText("<html><div style='width:" + cssWidth + "px'>" + text + "</div></html>");
            int measured = Math.max(1, body.getPreferredSize().width);
            if (measured <= width) break;
            cssWidth = Math.max(80, cssWidth * width / measured);
        }
        body.setForeground(message.outgoing() ? Color.WHITE : Ui.TEXT);
        body.setBackground(message.outgoing() ? Ui.BLUE : new Color(0xEEF0F4));
        body.setOpaque(true);
        body.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        body.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (onSelect != null) {
            body.setToolTipText(selected ? "Nhấp để bỏ chọn tin nhắn này" : "Nhấp để chọn tin nhắn này (hỗ trợ chọn nhiều tin)");
            body.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) { onSelect.run(); }
            });
        }

        JPanel metaRow = new JPanel(new BorderLayout(8, 0));
        metaRow.setOpaque(false);

        StringBuilder metaText = new StringBuilder(message.time());
        if (message.outgoing()) metaText.append("  Đã xem");
        if (selected) metaText.append("  ·  [Đã chọn]");
        if (highlighted) metaText.append("  ·  [Kết quả tìm kiếm]");

        JLabel time = new JLabel(metaText.toString());
        time.setFont(time.getFont().deriveFont(selected || highlighted ? Font.BOLD : Font.PLAIN, 11f));
        time.setForeground(highlighted ? new Color(0xB4, 0x53, 0x09) : selected ? Ui.BLUE : Ui.MUTED);
        time.setHorizontalAlignment(message.outgoing() ? SwingConstants.RIGHT : SwingConstants.LEFT);
        metaRow.add(time, BorderLayout.CENTER);

        if (onJumpToMessage != null) {
            JButton jumpBtn = new JButton("Đi tới tin nhắn");
            jumpBtn.setFont(jumpBtn.getFont().deriveFont(Font.BOLD, 11f));
            jumpBtn.setForeground(Ui.BLUE);
            jumpBtn.setBackground(Color.WHITE);
            jumpBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jumpBtn.setMargin(new Insets(2, 8, 2, 8));
            jumpBtn.putClientProperty("messageTarget", jumpTarget);
            jumpBtn.addActionListener(e -> onJumpToMessage.run());
            metaRow.add(jumpBtn, BorderLayout.EAST);
        }

        add(body, BorderLayout.CENTER);
        add(metaRow, BorderLayout.SOUTH);
    }
}
