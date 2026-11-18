package com.chatsystem.user;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Selectable incoming/outgoing bubble using Swing layout managers. */
final class MessageBubble extends JPanel {
    MessageBubble(ChatData.Message message, int width, Runnable onSelect) {
        super(new BorderLayout(0, 7));
        setOpaque(false);
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
        body.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        body.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        body.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { onSelect.run(); }
        });
        JLabel time = new JLabel(message.time() + (message.outgoing() ? "  Đã xem" : ""));
        time.setFont(time.getFont().deriveFont(11f)); time.setForeground(Ui.MUTED);
        time.setHorizontalAlignment(message.outgoing() ? SwingConstants.RIGHT : SwingConstants.LEFT);
        add(body); add(time, BorderLayout.SOUTH);
    }
}
