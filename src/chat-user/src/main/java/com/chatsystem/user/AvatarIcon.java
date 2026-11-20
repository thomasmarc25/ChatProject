package com.chatsystem.user;

import javax.swing.Icon;
import java.awt.*;

/** Resolution-independent circular avatar, drawn with Java2D. */
final class AvatarIcon implements Icon {
    private final String initials;
    private final Color color;
    private final int size;

    AvatarIcon(String initials, Color color, int size) {
        this.initials = initials;
        this.color = color;
        this.size = size;
    }

    public int getIconWidth() { return size; }
    public int getIconHeight() { return size; }

    public void paintIcon(Component owner, Graphics graphics, int x, int y) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(color);
        g.fillOval(x, y, size, size);
        g.setFont(new Font("Segoe UI", Font.BOLD, size / 3));
        g.setColor(Color.WHITE);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(initials, x + (size - fm.stringWidth(initials)) / 2,
                y + (size - fm.getHeight()) / 2 + fm.getAscent());
        g.dispose();
    }
}
