package com.chatsystem.user;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;

final class AccountPanel extends JPanel {
    final JTabbedPane tabs = new JTabbedPane();

    AccountPanel(Runnable openChat, Runnable openAdmin) {
        this(UserSessionProfile.DEMO_USER, false, openChat, openAdmin);
    }

    AccountPanel(UserSessionProfile session, boolean signedInSettingsOnly, Runnable openChat, Runnable openAdmin) {
        super(new BorderLayout());
        UserSessionProfile activeSession = session != null ? session : UserSessionProfile.DEMO_USER;
        String subtitle = signedInSettingsOnly
                ? "Hồ sơ cá nhân & bảo mật tài khoản của " + activeSession.displayLabel() + " · Bản xem trước giao diện"
                : "Kết nối và trò chuyện cùng ChatSystem · Bản xem trước giao diện";
        JPanel page = Ui.page(signedInSettingsOnly ? "Cài đặt tài khoản" : "Tài khoản của bạn", subtitle);

        if (!signedInSettingsOnly) {
            tabs.addTab("Đăng nhập", login(openChat, openAdmin));
            tabs.addTab("Đăng ký", fields(new String[]{"Tên đăng nhập", "Họ và tên", "Email", "Mật khẩu", "Xác nhận mật khẩu"}, new String[]{"", "", "", "", ""}, "Tạo tài khoản"));
            tabs.addTab("Quên mật khẩu", fields(new String[]{"Email khôi phục"}, new String[]{""}, "Gửi liên kết khôi phục"));
        }

        JPanel profile = fields(
            new String[]{"Tên đăng nhập", "Họ và tên", "Email", "Địa chỉ", "Ngày sinh (dd/MM/yyyy)"},
            new String[]{
                activeSession.username(),
                activeSession.fullName(),
                activeSession.email(),
                activeSession.address(),
                activeSession.dateOfBirth()
            },
            "Lưu thông tin"
        );
        JComboBox<String> gender = new JComboBox<>(new String[]{"Nam", "Nữ", "Khác", "Không muốn chia sẻ"});
        gender.setSelectedItem(activeSession.gender());
        Ui.field(profile, 5, "Giới tính", gender);
        tabs.addTab("Thông tin cá nhân", profile);
        tabs.addTab("Đổi mật khẩu", fields(new String[]{"Mật khẩu hiện tại", "Mật khẩu mới", "Xác nhận mật khẩu mới"}, new String[]{"", "", ""}, "Đổi mật khẩu"));
        page.add(tabs, BorderLayout.CENTER);
        add(page);
    }

    private JPanel login(Runnable openChat, Runnable openAdmin) {
        JPanel p = Ui.form();
        JTextField userField = Ui.input("Tên đăng nhập (nguyenvanan hoặc admin)");
        userField.setText(ChatData.CURRENT_USERNAME);
        JPasswordField passField = new JPasswordField(22);
        passField.setText("UserDemo#2026");
        Ui.field(p, 0, "Tên đăng nhập", userField);
        Ui.field(p, 1, "Mật khẩu", passField);
        JLabel hint = new JLabel("Mẫu USER: nguyenvanan  ·  Mẫu ADMIN: admin (mở giao diện Quản trị)");
        hint.setForeground(Ui.MUTED);
        Ui.field(p, 2, "Tài khoản mẫu", hint);
        JButton loginBtn = Ui.primaryButton("Đăng nhập", () -> {
            String u = userField.getText().strip().toLowerCase(Locale.ROOT);
            if ("admin".equals(u)) {
                openAdmin.run();
            } else {
                openChat.run();
            }
        });
        Ui.field(p, 3, "", loginBtn);
        Ui.field(p, 4, "Khám phá nhanh", Ui.row(
            Ui.button("Vào User (@nguyenvanan)", openChat),
            Ui.button("Chuyển sang Admin (@admin)", openAdmin)
        ));
        return p;
    }

    private JPanel fields(String[] labels, String[] values, String action) {
        JPanel p = Ui.form();
        for (int i = 0; i < labels.length; i++) {
            JTextField f = labels[i].toLowerCase().contains("mật khẩu") ? new JPasswordField(22) : Ui.input(labels[i]);
            f.setText(values[i]);
            Ui.field(p, i, labels[i], f);
        }
        Ui.field(p, labels.length + 1, "", Ui.button(action, () -> Ui.demo(this)));
        return p;
    }
}
