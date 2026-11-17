package com.chatsystem.user;

import javax.swing.*;
import java.awt.*;

final class AccountPanel extends JPanel {
    final JTabbedPane tabs = new JTabbedPane();
    AccountPanel(Runnable openChat) {
        super(new BorderLayout()); JPanel page=Ui.page("Tài khoản của bạn", "Kết nối và trò chuyện cùng ChatSystem · Bản xem trước giao diện");
        tabs.addTab("Đăng nhập", login(openChat));
        tabs.addTab("Đăng ký", fields(new String[]{"Tên đăng nhập","Họ và tên","Email","Mật khẩu","Xác nhận mật khẩu"},new String[]{"","","","",""},"Tạo tài khoản"));
        tabs.addTab("Quên mật khẩu", fields(new String[]{"Email khôi phục"},new String[]{""},"Gửi liên kết khôi phục"));
        JPanel profile=fields(new String[]{"Tên đăng nhập","Họ và tên","Email","Địa chỉ","Ngày sinh (dd/MM/yyyy)"},new String[]{"ldphuc","Lê Đình Phúc","phuc@example.test","TP. Hồ Chí Minh","01/01/2005"},"Lưu thông tin");
        JComboBox<String> gender=new JComboBox<>(new String[]{"Nam","Nữ","Khác","Không muốn chia sẻ"}); Ui.field(profile,5,"Giới tính",gender); tabs.addTab("Thông tin cá nhân",profile);
        tabs.addTab("Đổi mật khẩu",fields(new String[]{"Mật khẩu hiện tại","Mật khẩu mới","Xác nhận mật khẩu mới"},new String[]{"","",""},"Đổi mật khẩu"));
        page.add(tabs,BorderLayout.CENTER); add(page);
    }
    private JPanel login(Runnable openChat) {
        JPanel p=fields(new String[]{"Tên đăng nhập","Mật khẩu"},new String[]{"",""},"Đăng nhập");
        Ui.field(p,4,"Khám phá giao diện",Ui.button("Xem dữ liệu mẫu",openChat)); return p;
    }
    private JPanel fields(String[] labels,String[] values,String action) {
        JPanel p=Ui.form(); for(int i=0;i<labels.length;i++){
            JTextField f=labels[i].toLowerCase().contains("mật khẩu")?new JPasswordField(22):Ui.input(labels[i]); f.setText(values[i]); Ui.field(p,i,labels[i],f);
        }
        Ui.field(p,labels.length+1,"",Ui.button(action,()->Ui.demo(this))); return p;
    }
}
