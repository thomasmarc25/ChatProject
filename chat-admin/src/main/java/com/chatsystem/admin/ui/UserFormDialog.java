package com.chatsystem.admin.ui;

import com.chatsystem.admin.model.User;
import com.chatsystem.admin.theme.AppTheme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Modal dialog for adding or editing a user account in the in-memory UI preview.
 * Displays fields for username, full name, address, date of birth, gender, and email.
 */
public class UserFormDialog extends JDialog {

    public interface UserFormSubmitListener {
        void onSubmit(
                String username,
                String fullName,
                String address,
                LocalDate dateOfBirth,
                String gender,
                String email
        );
    }

    private final JTextField usernameField;
    private final JTextField fullNameField;
    private final JTextField emailField;
    private final JTextField dateOfBirthField;
    private final JComboBox<String> genderCombo;
    private final JTextField addressField;
    private final JLabel validationLabel;
    private final UserFormSubmitListener submitListener;

    public UserFormDialog(Window owner) {
        this(owner, null, null);
    }

    public UserFormDialog(Window owner, User existingUser) {
        this(owner, existingUser, null);
    }

    public UserFormDialog(Window owner, User existingUser, UserFormSubmitListener submitListener) {
        super(
                owner,
                existingUser == null ? "Thêm người dùng mới" : "Cập nhật thông tin người dùng",
                ModalityType.APPLICATION_MODAL
        );
        this.submitListener = submitListener;

        this.usernameField = createStyledTextField("Ví dụ: nguyenvanan");
        this.fullNameField = createStyledTextField("Ví dụ: Nguyễn Văn An");
        this.emailField = createStyledTextField("Ví dụ: nguyenvanan@chatsystem.vn");
        this.dateOfBirthField = createStyledTextField("dd/MM/yyyy (Ví dụ: 14/03/2002)");
        this.genderCombo = new JComboBox<>(new String[]{"Nam", "Nữ", "Khác"});
        this.genderCombo.setFont(AppTheme.FONT_BODY);
        this.genderCombo.setPreferredSize(new Dimension(220, AppTheme.CONTROL_HEIGHT));
        this.addressField = createStyledTextField("Ví dụ: 12 Nguyễn Huệ, Quận 1, TP.HCM");
        this.validationLabel = new JLabel(" ");
        this.validationLabel.setFont(AppTheme.FONT_CAPTION_BOLD);
        this.validationLabel.setForeground(AppTheme.DANGER);

        if (existingUser != null) {
            populateFields(existingUser);
        }

        initDialogLayout(existingUser != null);
        pack();
        setMinimumSize(new Dimension(580, getPreferredSize().height));
        setSize(new Dimension(610, Math.max(520, getPreferredSize().height)));
        setResizable(false);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    private void populateFields(User user) {
        usernameField.setText(user.getUsername());
        fullNameField.setText(user.getFullName());
        emailField.setText(user.getEmail());
        dateOfBirthField.setText(user.getFormattedDateOfBirth());
        genderCombo.setSelectedItem(user.getGender());
        addressField.setText(user.getAddress());
    }

    private void initDialogLayout(boolean editMode) {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppTheme.SURFACE_CARD);

        root.add(createHeader(editMode), BorderLayout.NORTH);
        root.add(createFormBody(), BorderLayout.CENTER);
        root.add(createFooter(), BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel createHeader(boolean editMode) {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(AppTheme.SURFACE_CARD);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.BORDER),
                BorderFactory.createEmptyBorder(
                        AppTheme.SPACING_MD + 4,
                        AppTheme.CONTENT_PADDING,
                        AppTheme.SPACING_MD,
                        AppTheme.CONTENT_PADDING
                )
        ));

        JLabel titleLabel = new JLabel(
                editMode ? "Cập nhật thông tin người dùng" : "Thêm người dùng mới"
        );
        titleLabel.setFont(AppTheme.FONT_SECTION_TITLE);
        titleLabel.setForeground(AppTheme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel(
                "Điền đầy đủ các thông tin hồ sơ tài khoản bên dưới (Tên đăng nhập, Họ tên, Địa chỉ, Ngày sinh, Giới tính, Email)"
        );
        subtitleLabel.setFont(AppTheme.FONT_CAPTION);
        subtitleLabel.setForeground(AppTheme.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(titleLabel);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitleLabel);
        return header;
    }

    private JPanel createFormBody() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(AppTheme.SURFACE_CARD);
        formPanel.setBorder(BorderFactory.createEmptyBorder(
                AppTheme.SPACING_MD + 4,
                AppTheme.CONTENT_PADDING,
                AppTheme.SPACING_MD + 4,
                AppTheme.CONTENT_PADDING
        ));

        // Row 0: Labels for Username & Full Name
        addFormLabel(formPanel, "Tên đăng nhập *", 0, 0, 0.5, new Insets(0, 0, 6, 8));
        addFormLabel(formPanel, "Họ và tên *", 1, 0, 0.5, new Insets(0, 8, 6, 0));

        // Row 1: Fields for Username & Full Name
        addFormControl(formPanel, usernameField, 0, 1, 1, 0.5, new Insets(0, 0, 14, 8));
        addFormControl(formPanel, fullNameField, 1, 1, 1, 0.5, new Insets(0, 8, 14, 0));

        // Row 2: Label for Email (full width)
        addFormLabel(formPanel, "Địa chỉ Email *", 0, 2, 1.0, new Insets(0, 0, 6, 0), 2);

        // Row 3: Field for Email (full width)
        addFormControl(formPanel, emailField, 0, 3, 2, 1.0, new Insets(0, 0, 14, 0));

        // Row 4: Labels for Date of Birth & Gender
        addFormLabel(formPanel, "Ngày sinh (dd/MM/yyyy)", 0, 4, 0.5, new Insets(0, 0, 6, 8));
        addFormLabel(formPanel, "Giới tính", 1, 4, 0.5, new Insets(0, 8, 6, 0));

        // Row 5: Fields for Date of Birth & Gender
        addFormControl(formPanel, dateOfBirthField, 0, 5, 1, 0.5, new Insets(0, 0, 14, 8));
        addFormControl(formPanel, genderCombo, 1, 5, 1, 0.5, new Insets(0, 8, 14, 0));

        // Row 6: Label for Address (full width)
        addFormLabel(formPanel, "Địa chỉ liên hệ", 0, 6, 1.0, new Insets(0, 0, 6, 0), 2);

        // Row 7: Field for Address (full width)
        addFormControl(formPanel, addressField, 0, 7, 2, 1.0, new Insets(0, 0, 8, 0));

        // Row 8: Validation feedback message
        addFormControl(formPanel, validationLabel, 0, 8, 2, 1.0, new Insets(0, 0, 0, 0));

        return formPanel;
    }

    private void addFormLabel(JPanel panel, String text, int gridx, int gridy, double weightx, Insets insets) {
        addFormLabel(panel, text, gridx, gridy, weightx, insets, 1);
    }

    private void addFormLabel(
            JPanel panel,
            String text,
            int gridx,
            int gridy,
            double weightx,
            Insets insets,
            int gridwidth
    ) {
        JLabel label = new JLabel(text);
        label.setFont(AppTheme.FONT_BODY_BOLD);
        label.setForeground(AppTheme.TEXT_PRIMARY);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        gbc.gridwidth = gridwidth;
        gbc.weightx = weightx;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = insets;
        panel.add(label, gbc);
    }

    private void addFormControl(
            JPanel panel,
            Component control,
            int gridx,
            int gridy,
            int gridwidth,
            double weightx,
            Insets insets
    ) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        gbc.gridwidth = gridwidth;
        gbc.weightx = weightx;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = insets;
        panel.add(control, gbc);
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(AppTheme.PAGE_BG);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, AppTheme.BORDER),
                BorderFactory.createEmptyBorder(
                        AppTheme.SPACING_MD - 2,
                        AppTheme.CONTENT_PADDING,
                        AppTheme.SPACING_MD - 2,
                        AppTheme.CONTENT_PADDING
                )
        ));

        JLabel noteLabel = new JLabel("Chế độ xem trước (in-memory)");
        noteLabel.setFont(AppTheme.FONT_CAPTION);
        noteLabel.setForeground(AppTheme.TEXT_SECONDARY);

        JPanel buttonGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, AppTheme.SPACING_SM, 0));
        buttonGroup.setOpaque(false);

        JButton cancelButton = new JButton("Hủy");
        cancelButton.setFont(AppTheme.FONT_BODY_BOLD);
        cancelButton.setForeground(AppTheme.TEXT_PRIMARY);
        cancelButton.setBackground(AppTheme.SURFACE_CARD);
        cancelButton.setFocusPainted(false);
        cancelButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancelButton.setPreferredSize(new Dimension(100, AppTheme.CONTROL_HEIGHT));
        cancelButton.addActionListener(e -> dispose());

        JButton saveButton = createPrimaryButton("Lưu thông tin");
        saveButton.setPreferredSize(new Dimension(140, AppTheme.CONTROL_HEIGHT));
        saveButton.addActionListener(e -> handleSave());

        buttonGroup.add(cancelButton);
        buttonGroup.add(saveButton);

        footer.add(noteLabel, BorderLayout.WEST);
        footer.add(buttonGroup, BorderLayout.EAST);
        return footer;
    }

    private void handleSave() {
        String username = usernameField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String dobText = dateOfBirthField.getText().trim();
        String gender = (String) genderCombo.getSelectedItem();
        String address = addressField.getText().trim();

        if (username.isEmpty() || fullName.isEmpty() || email.isEmpty()) {
            validationLabel.setText("Vui lòng nhập đầy đủ Tên đăng nhập, Họ và tên và Địa chỉ Email.");
            return;
        }

        LocalDate dob = LocalDate.of(2002, 1, 1);
        if (!dobText.isEmpty()) {
            try {
                dob = LocalDate.parse(dobText, User.DATE_FORMATTER);
            } catch (DateTimeParseException ex) {
                validationLabel.setText("Ngày sinh không đúng định dạng dd/MM/yyyy (Ví dụ: 14/03/2002).");
                return;
            }
        }

        if (submitListener != null) {
            submitListener.onSubmit(username, fullName, address, dob, gender != null ? gender : "Khác", email);
        }
        dispose();
    }

    private JTextField createStyledTextField(String placeholder) {
        JTextField textField = new JTextField();
        textField.setFont(AppTheme.FONT_BODY);
        textField.setForeground(AppTheme.TEXT_PRIMARY);
        textField.setPreferredSize(new Dimension(220, AppTheme.CONTROL_HEIGHT));
        textField.putClientProperty("JTextField.placeholderText", placeholder);
        return textField;
    }

    private JButton createPrimaryButton(String text) {
        return new JButton(text) {
            private boolean hovered = false;

            {
                setFont(AppTheme.FONT_BODY_BOLD);
                setForeground(AppTheme.SURFACE_CARD);
                setFocusPainted(false);
                setBorderPainted(false);
                setContentAreaFilled(false);
                setOpaque(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hovered = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hovered = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hovered ? AppTheme.PRIMARY_HOVER : AppTheme.PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), AppTheme.CORNER_RADIUS, AppTheme.CORNER_RADIUS);
                g2.dispose();
                super.paintComponent(g);
            }
        };
    }
}
