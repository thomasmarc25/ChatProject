package com.chatsystem.user;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/** Shared design tokens and small layout helpers. */
final class Ui {
    static final Color BLUE = new Color(0x2563EB), NAV = new Color(0x111827), BG = new Color(0xF5F7FB);
    static final Color MUTED = new Color(0x6B7280);
    static final Color TEXT = new Color(0x1F2937), BORDER = new Color(0xE5E7EB);
    static final Color SUCCESS = new Color(0x16A34A), DANGER = new Color(0xDC2626);
    static final Color WARNING = new Color(0xF59E0B), SOFT_BLUE = new Color(0xEAF0FE);
    private Ui() {}
    static void setup() {
        try { UIManager.setLookAndFeel((javax.swing.LookAndFeel)Class.forName("com.formdev.flatlaf.FlatLightLaf").getConstructor().newInstance()); }
        catch (ReflectiveOperationException | UnsupportedLookAndFeelException e) { throw new IllegalStateException("FlatLaf dependency required; run Maven exec:java", e); }
        UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("Component.arc", 10); UIManager.put("Button.arc", 10);
        UIManager.put("TextComponent.arc", 10); UIManager.put("Table.rowHeight", 44);
        UIManager.put("Panel.background", BG);
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("Button.background", Color.WHITE);
        UIManager.put("Button.foreground", TEXT);
        UIManager.put("Component.borderColor", BORDER);
        UIManager.put("Component.focusColor", BLUE);
        UIManager.put("Table.selectionBackground", SOFT_BLUE);
        UIManager.put("Table.selectionForeground", TEXT);
        UIManager.put("TabbedPane.background", Color.WHITE);
        UIManager.put("ScrollBar.width", 10);
    }
    static JPanel page(String title, String subtitle) {
        JPanel p = new JPanel(new BorderLayout(16,16)); p.setBorder(BorderFactory.createEmptyBorder(24,24,24,24));
        JPanel heading = new JPanel(new GridLayout(0,1,0,6));
        JLabel h = new JLabel(title); h.setFont(h.getFont().deriveFont(Font.BOLD,24)); heading.add(h);
        JLabel s = new JLabel(subtitle); s.setForeground(MUTED); heading.add(s); p.add(heading,BorderLayout.NORTH); return p;
    }
    static JPanel row(Component... items) { JPanel p=new JPanel(new FlowLayout(FlowLayout.LEFT,8,8)); p.setOpaque(false); for(Component c:items)p.add(c); return p; }
    static JButton button(String text, Runnable action) { JButton b=new JButton(text); b.setPreferredSize(new Dimension(Math.max(110,b.getPreferredSize().width+16),40)); b.addActionListener(e->action.run()); return b; }
    static JButton primaryButton(String text, Runnable action) {
        JButton b = button(text, action); b.setBackground(BLUE); b.setForeground(Color.WHITE);
        b.setFont(b.getFont().deriveFont(Font.BOLD)); return b;
    }
    static JTextField input(String name) { JTextField f=new JTextField(22); f.setName(name); f.getAccessibleContext().setAccessibleName(name); f.putClientProperty("JTextField.placeholderText", name); return f; }
    static JTable table(String[] columns, Object[][] rows) {
        JTable t=new JTable(new DefaultTableModel(rows,columns){ public boolean isCellEditable(int r,int c){return false;} });
        t.setRowHeight(44); t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); t.setAutoCreateRowSorter(true);
        t.setFillsViewportHeight(true); if(rows.length>0)t.setRowSelectionInterval(0,0); return t;
    }
    static JPanel form() { JPanel p=new JPanel(new GridBagLayout()); p.setBackground(Color.WHITE); p.setBorder(BorderFactory.createEmptyBorder(16,16,16,16)); return p; }
    static void field(JPanel p,int row,String label,Component value) {
        GridBagConstraints g=new GridBagConstraints(); g.gridy=row; g.gridx=0; g.anchor=GridBagConstraints.NORTHWEST; g.insets=new Insets(8,8,8,16); p.add(new JLabel(label),g);
        g.gridx=1; g.weightx=1; g.fill=GridBagConstraints.HORIZONTAL; p.add(value,g);
    }
    static void demo(Component owner) { JOptionPane.showMessageDialog(owner,"Bản thiết kế giao diện — dữ liệu mẫu, chưa lưu hoặc gửi dữ liệu.","Phiên bản 1",JOptionPane.INFORMATION_MESSAGE); }
}
