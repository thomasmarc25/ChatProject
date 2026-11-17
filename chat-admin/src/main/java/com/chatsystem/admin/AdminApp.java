package com.chatsystem.admin;

import com.chatsystem.admin.theme.AppTheme;
import com.chatsystem.admin.ui.AdminMainFrame;

import javax.swing.SwingUtilities;

/**
 * Entry point for the ChatSystem Admin Swing UI application.
 */
public final class AdminApp {

    private AdminApp() {
        // Entry-point class
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AppTheme.setupLookAndFeel();
            AdminMainFrame mainFrame = new AdminMainFrame();
            mainFrame.setVisible(true);
        });
    }
}
