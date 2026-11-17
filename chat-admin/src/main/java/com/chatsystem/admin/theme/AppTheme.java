package com.chatsystem.admin.theme;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;

/**
 * Centralized design tokens (colors, fonts, dimensions, spacings) and FlatLaf setup
 * for the ChatSystem Admin Swing UI.
 */
public final class AppTheme {

    // Color palette from AGENTS.md
    public static final Color PRIMARY = new Color(0x25, 0x63, 0xEB);        // #2563EB
    public static final Color PRIMARY_HOVER = new Color(0x1D, 0x4E, 0xD8);  // #1D4ED8
    public static final Color SIDEBAR_BG = new Color(0x11, 0x18, 0x27);     // #111827
    public static final Color SIDEBAR_SURFACE = new Color(0x1F, 0x29, 0x37);// #1F2937
    public static final Color SIDEBAR_BORDER = new Color(0x37, 0x41, 0x51); // #374151
    public static final Color SIDEBAR_TEXT = new Color(0xF9, 0xFA, 0xFB);   // #F9FAFB
    public static final Color SIDEBAR_TEXT_MUTED = new Color(0x9C, 0xA3, 0xAF); // #9CA3AF
    public static final Color PAGE_BG = new Color(0xF5, 0xF7, 0xFB);        // #F5F7FB
    public static final Color SURFACE_CARD = new Color(0xFF, 0xFF, 0xFF);   // #FFFFFF
    public static final Color TEXT_PRIMARY = new Color(0x1F, 0x29, 0x37);   // #1F2937
    public static final Color TEXT_SECONDARY = new Color(0x6B, 0x72, 0x80); // #6B7280
    public static final Color BORDER = new Color(0xE5, 0xE7, 0xEB);         // #E5E7EB
    public static final Color SUCCESS = new Color(0x16, 0xA3, 0x4A);        // #16A34A
    public static final Color SUCCESS_BG = new Color(0xDC, 0xFC, 0xE7);     // #DCFCE7
    public static final Color WARNING = new Color(0xF5, 0x9E, 0x0B);        // #F59E0B
    public static final Color WARNING_BG = new Color(0xFE, 0xF3, 0xC7);     // #FEF3C7
    public static final Color DANGER = new Color(0xDC, 0x26, 0x26);         // #DC2626
    public static final Color DANGER_BG = new Color(0xFE, 0xE2, 0xE2);      // #FEE2E2
    public static final Color NEUTRAL_BG = new Color(0xF3, 0xF4, 0xF6);     // #F3F4F6
    public static final Color PRIMARY_SOFT_BG = new Color(0xEF, 0xF6, 0xFF);// #EFF6FF

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

    // Font family & typography tokens (point sizes: 24, 18, 14, 12)
    public static final String PREFERRED_FONT_FAMILY = "Segoe UI";
    public static final String FONT_FAMILY = resolveFontFamily();

    public static final int FONT_SIZE_PAGE_TITLE = 24;
    public static final int FONT_SIZE_SECTION_TITLE = 18;
    public static final int FONT_SIZE_BODY = 14;
    public static final int FONT_SIZE_CAPTION = 12;

    public static final Font FONT_PAGE_TITLE = new Font(FONT_FAMILY, Font.BOLD, FONT_SIZE_PAGE_TITLE);
    public static final Font FONT_SECTION_TITLE = new Font(FONT_FAMILY, Font.BOLD, FONT_SIZE_SECTION_TITLE);
    public static final Font FONT_BODY = new Font(FONT_FAMILY, Font.PLAIN, FONT_SIZE_BODY);
    public static final Font FONT_BODY_BOLD = new Font(FONT_FAMILY, Font.BOLD, FONT_SIZE_BODY);
    public static final Font FONT_CAPTION = new Font(FONT_FAMILY, Font.PLAIN, FONT_SIZE_CAPTION);
    public static final Font FONT_CAPTION_BOLD = new Font(FONT_FAMILY, Font.BOLD, FONT_SIZE_CAPTION);

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
     * Configures FlatLaf Light Look and Feel and applies shared UI defaults.
     * Must be called before creating any Swing components.
     */
    public static void setupLookAndFeel() {
        FlatLightLaf.setup();
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);

        UIManager.put("defaultFont", FONT_BODY);
        UIManager.put("Component.arc", CORNER_RADIUS);
        UIManager.put("Button.arc", CORNER_RADIUS);
        UIManager.put("TextComponent.arc", CORNER_RADIUS);
        UIManager.put("Component.focusColor", PRIMARY);
        UIManager.put("Panel.background", PAGE_BG);
    }
}
