package com.chatsystem.admin.theme;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.table.JTableHeader;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.util.Arrays;

/**
 * Centralized design tokens (colors, fonts, dimensions, spacings), FlatLaf setup,
 * and shared component styling helpers for the ChatSystem Admin Swing UI.
 * Visually aligned with Member 1's User application ({@code com.chatsystem.user.Ui} and {@code UserApp}).
 */
public final class AppTheme {

    // Shared color palette aligned with com.chatsystem.user.Ui and AGENTS.md
    public static final Color PRIMARY = new Color(0x25, 0x63, 0xEB);              // #2563EB (Ui.BLUE)
    public static final Color PRIMARY_HOVER = new Color(0x1D, 0x4E, 0xD8);        // #1D4ED8
    public static final Color PRIMARY_SOFT_BG = new Color(0xEA, 0xF0, 0xFE);      // #EAF0FE (Ui.SOFT_BLUE)
    public static final Color PRIMARY_BORDER = new Color(0xBF, 0xDB, 0xFE);       // #BFDBFE

    public static final Color SIDEBAR_BG = new Color(0x11, 0x18, 0x27);           // #111827 (Ui.NAV)
    public static final Color SIDEBAR_SURFACE = new Color(0x1F, 0x29, 0x37);      // #1F2937
    public static final Color SIDEBAR_BORDER = new Color(0x37, 0x41, 0x51);       // #374151 (UserApp separator)
    public static final Color SIDEBAR_TEXT = new Color(0xFF, 0xFF, 0xFF);         // #FFFFFF (UserApp brand/active)
    public static final Color SIDEBAR_NAV_TEXT = new Color(0xD1, 0xD5, 0xDB);     // #D1D5DB (UserApp nav items)
    public static final Color SIDEBAR_FOOTER_TEXT = new Color(0xCB, 0xD5, 0xE1);  // #CBD5E1 (UserApp footer text)
    public static final Color SIDEBAR_TEXT_MUTED = new Color(0x9C, 0xA3, 0xAF);   // #9CA3AF (UserApp footer note)

    public static final Color PAGE_BG = new Color(0xF5, 0xF7, 0xFB);              // #F5F7FB (Ui.BG)
    public static final Color SURFACE_CARD = new Color(0xFF, 0xFF, 0xFF);         // #FFFFFF
    public static final Color TEXT_PRIMARY = new Color(0x1F, 0x29, 0x37);         // #1F2937 (Ui.TEXT)
    public static final Color TEXT_SECONDARY = new Color(0x6B, 0x72, 0x80);       // #6B7280 (Ui.MUTED)
    public static final Color BORDER = new Color(0xE5, 0xE7, 0xEB);               // #E5E7EB (Ui.BORDER)

    public static final Color SUCCESS = new Color(0x16, 0xA3, 0x4A);              // #16A34A (Ui.SUCCESS)
    public static final Color SUCCESS_HOVER = new Color(0x15, 0x80, 0x3D);        // #15803D
    public static final Color SUCCESS_BG = new Color(0xDC, 0xFC, 0xE7);           // #DCFCE7

    public static final Color WARNING = new Color(0xF5, 0x9E, 0x0B);              // #F59E0B (Ui.WARNING)
    public static final Color WARNING_BG = new Color(0xFE, 0xF3, 0xC7);           // #FEF3C7
    public static final Color WARNING_SOFT_SURFACE = new Color(0xFF, 0xFB, 0xEB); // #FFFBEB
    public static final Color WARNING_SOFT_BORDER = new Color(0xFD, 0xE6, 0x8A);  // #FDE68A

    public static final Color DANGER = new Color(0xDC, 0x26, 0x26);               // #DC2626 (Ui.DANGER)
    public static final Color DANGER_BG = new Color(0xFE, 0xE2, 0xE2);            // #FEE2E2
    public static final Color NEUTRAL_BG = new Color(0xF3, 0xF4, 0xF6);           // #F3F4F6

    // Chart canvas tokens
    public static final Color CHART_HOVER_SLOT = new Color(0xF8, 0xFA, 0xFC);     // #F8FAFC
    public static final Color CHART_ZERO_BAR = new Color(0xD1, 0xD5, 0xDB);       // #D1D5DB
    public static final Color CHART_AXIS_LINE = new Color(0x9C, 0xA3, 0xAF);      // #9CA3AF

    // Window dimensions
    public static final int DEFAULT_WINDOW_WIDTH = 1280;
    public static final int DEFAULT_WINDOW_HEIGHT = 800;
    public static final int MIN_WINDOW_WIDTH = 1100;
    public static final int MIN_WINDOW_HEIGHT = 700;

    public static final Dimension DEFAULT_WINDOW_SIZE = new Dimension(DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT);
    public static final Dimension MIN_WINDOW_SIZE = new Dimension(MIN_WINDOW_WIDTH, MIN_WINDOW_HEIGHT);

    // Component dimensions & spacing tokens
    public static final int SIDEBAR_WIDTH = 240;
    public static final int HEADER_HEIGHT = 64;
    public static final int CONTENT_PADDING = 24;
    public static final int CONTROL_HEIGHT = 40;
    public static final int TABLE_ROW_HEIGHT = 44;
    public static final int CORNER_RADIUS = 10;

    public static final int SPACING_SM = 8;
    public static final int SPACING_MD = 16;
    public static final int SPACING_LG = 24;

    // Font family & typography tokens (point sizes: 24, 21, 18, 14, 12, 11)
    public static final String PREFERRED_FONT_FAMILY = "Segoe UI";
    public static final String FONT_FAMILY = resolveFontFamily();

    public static final int FONT_SIZE_PAGE_TITLE = 24;
    public static final int FONT_SIZE_BRAND_TITLE = 21;
    public static final int FONT_SIZE_SECTION_TITLE = 18;
    public static final int FONT_SIZE_BODY = 14;
    public static final int FONT_SIZE_CAPTION = 12;
    public static final int FONT_SIZE_SIDEBAR_NOTE = 11;

    public static final Font FONT_PAGE_TITLE = new Font(FONT_FAMILY, Font.BOLD, FONT_SIZE_PAGE_TITLE);
    public static final Font FONT_BRAND_TITLE = new Font(FONT_FAMILY, Font.BOLD, FONT_SIZE_BRAND_TITLE);
    public static final Font FONT_SECTION_TITLE = new Font(FONT_FAMILY, Font.BOLD, FONT_SIZE_SECTION_TITLE);
    public static final Font FONT_BODY = new Font(FONT_FAMILY, Font.PLAIN, FONT_SIZE_BODY);
    public static final Font FONT_BODY_BOLD = new Font(FONT_FAMILY, Font.BOLD, FONT_SIZE_BODY);
    public static final Font FONT_CAPTION = new Font(FONT_FAMILY, Font.PLAIN, FONT_SIZE_CAPTION);
    public static final Font FONT_CAPTION_BOLD = new Font(FONT_FAMILY, Font.BOLD, FONT_SIZE_CAPTION);
    public static final Font FONT_SIDEBAR_NOTE = new Font(FONT_FAMILY, Font.PLAIN, FONT_SIZE_SIDEBAR_NOTE);

    private AppTheme() {
        // Utility class
    }

    private static String resolveFontFamily() {
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            if (ge != null) {
                String[] families = ge.getAvailableFontFamilyNames();
                if (families != null && Arrays.asList(families).contains(PREFERRED_FONT_FAMILY)) {
                    return PREFERRED_FONT_FAMILY;
                }
            }
        } catch (Exception ignored) {
            // Fallback to logical SansSerif if environment is headless or restricted
        }
        return Font.SANS_SERIF;
    }

    /**
     * Configures FlatLaf Light Look and Feel and applies shared UI defaults
     * matching {@code com.chatsystem.user.Ui.setup()}.
     * Must be called before creating any Swing components.
     */
    public static void setupLookAndFeel() {
        FlatLightLaf.setup();

        UIManager.put("defaultFont", FONT_BODY);
        UIManager.put("Component.arc", CORNER_RADIUS);
        UIManager.put("Button.arc", CORNER_RADIUS);
        UIManager.put("TextComponent.arc", CORNER_RADIUS);
        UIManager.put("Table.rowHeight", TABLE_ROW_HEIGHT);
        UIManager.put("Panel.background", PAGE_BG);
        UIManager.put("Label.foreground", TEXT_PRIMARY);
        UIManager.put("Button.background", SURFACE_CARD);
        UIManager.put("Button.foreground", TEXT_PRIMARY);
        UIManager.put("Component.borderColor", BORDER);
        UIManager.put("Component.focusColor", PRIMARY);
        UIManager.put("Table.selectionBackground", PRIMARY_SOFT_BG);
        UIManager.put("Table.selectionForeground", TEXT_PRIMARY);
        UIManager.put("TableHeader.background", SURFACE_CARD);
        UIManager.put("TableHeader.foreground", TEXT_PRIMARY);
        UIManager.put("TabbedPane.background", SURFACE_CARD);
        UIManager.put("ScrollBar.width", 10);
    }

    /**
     * Creates a secondary white rounded button matching {@code com.chatsystem.user.Ui.button(...)}.
     */
    public static JButton createSecondaryButton(String text, int width) {
        return configureButton(new JButton(text), FONT_BODY, TEXT_PRIMARY, SURFACE_CARD, width);
    }

    /**
     * Creates a primary blue rounded button matching {@code com.chatsystem.user.Ui.primaryButton(...)}.
     */
    public static JButton createPrimaryButton(String text, int width) {
        return configureButton(new JButton(text), FONT_BODY_BOLD, SURFACE_CARD, PRIMARY, width);
    }

    /**
     * Creates a soft-blue outline action button for secondary toolbar/header actions.
     */
    public static JButton createPrimaryOutlineButton(String text, int width) {
        return configureButton(new JButton(text), FONT_BODY_BOLD, PRIMARY, SURFACE_CARD, width);
    }

    /**
     * Creates a solid semantic accent button (e.g. Danger red or Success green).
     */
    public static JButton createAccentButton(String text, int width, Color accentBg) {
        return configureButton(new JButton(text), FONT_BODY_BOLD, SURFACE_CARD, accentBg, width);
    }

    private static JButton configureButton(JButton btn, Font font, Color fg, Color bg, int minWidth) {
        btn.setFont(font);
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(0, 10, 0, 10));
        int targetW = Math.max(minWidth, btn.getPreferredSize().width + 12);
        btn.setPreferredSize(new Dimension(targetW, CONTROL_HEIGHT));
        btn.setMinimumSize(new Dimension(targetW, CONTROL_HEIGHT));
        return btn;
    }

    /**
     * Applies shared table and table-header styling aligned with {@code com.chatsystem.user.Ui.table(...)}.
     */
    public static void configureSharedTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setForeground(TEXT_PRIMARY);
        table.setBackground(SURFACE_CARD);
        table.setRowHeight(TABLE_ROW_HEIGHT);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(PRIMARY_SOFT_BG);
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setGridColor(BORDER);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        if (header != null) {
            header.setReorderingAllowed(false);
            header.setPreferredSize(new Dimension(0, 40));
            header.setFont(FONT_CAPTION_BOLD);
            header.setBackground(SURFACE_CARD);
            header.setForeground(TEXT_PRIMARY);
            header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        }
    }
}
