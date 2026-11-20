package com.chatsystem.user;

import javax.swing.Icon;
import java.awt.*;

/** Small outline icons drawn locally; no icon fonts or image downloads. */
final class NavigationIcon implements Icon {
    private final String kind;
    NavigationIcon(String kind) { this.kind = kind; }
    public int getIconWidth() { return 24; }
    public int getIconHeight() { return 24; }

    public void paintIcon(Component c, Graphics graphics, int x, int y) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.translate(x, y);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(c.getForeground());
        g.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        switch (kind) {
            case "chat" -> {
                g.drawRoundRect(2, 3, 20, 15, 5, 5);
                g.drawLine(6, 18, 6, 22); g.drawLine(6, 22, 11, 18);
            }
            case "search" -> { g.drawOval(3, 2, 13, 13); g.drawLine(15, 14, 22, 21); }
            case "friends", "group", "requests" -> {
                g.drawOval(4, 2, 7, 7); g.drawOval(14, 4, 6, 6);
                g.drawArc(1, 11, 15, 16, 0, 180); g.drawArc(12, 12, 11, 12, 0, 140);
                if (kind.equals("requests")) { g.drawLine(18, 17, 24, 17); g.drawLine(21, 14, 21, 20); }
            }
            default -> {
                g.drawOval(5, 5, 14, 14); g.drawOval(9, 9, 6, 6);
                for (int a = 0; a < 8; a++) {
                    double t = a * Math.PI / 4;
                    g.drawLine((int)(12 + 8 * Math.cos(t)), (int)(12 + 8 * Math.sin(t)),
                            (int)(12 + 11 * Math.cos(t)), (int)(12 + 11 * Math.sin(t)));
                }
            }
        }
        g.dispose();
    }
}
