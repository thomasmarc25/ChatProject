package com.chatsystem.user;

import java.awt.Color;
import java.util.List;

/** UI-only fixtures for the reference chat layout. */
final class ChatData {
    record Conversation(String name, String initials, Color color, String preview,
                        String time, boolean online, boolean group, boolean encrypted) {}
    record Message(String text, String time, boolean outgoing) {}

    static final List<Conversation> CONVERSATIONS = List.of(
        new Conversation("Nguyễn Văn A", "A", Ui.BLUE, "Chiều nay mình họp nhé!", "10:24", true, false, false),
        new Conversation("Nhóm Dự án", "N", new Color(0x7C3AED), "Chiều nay họp nhóm nhé!", "09:58", true, true, false),
        new Conversation("Trần Thị B", "B", new Color(0xF97316), "Bạn gửi file báo cáo chưa?", "09:12", false, false, false),
        new Conversation("Lê Minh C", "C", new Color(0x16A34A), "Đã xong phần giao diện.", "08:45", true, false, false),
        new Conversation("Nhóm Thiết kế", "T", new Color(0x3B82F6), "Cập nhật lại màu sắc nhé.", "08:10", false, true, false),
        new Conversation("Nhóm bảo mật", "M", new Color(0x0F766E), "Trao đổi trong nhóm mã hóa", "08:00", true, true, true)
    );

    static List<Message> messages(Conversation conversation) {
        if (conversation.group()) return List.of(
            new Message("Minh An\nChào cả nhóm! Chiều nay mình cùng kiểm tra giao diện nhé.", "09:30", false),
            new Message("Mình đã cập nhật giao diện theo bảng màu Modern Blue của nhóm.", "09:35", true),
            new Message("Bảo Trân\nMình đang kiểm tra thiết kế CSDL và danh sách thành viên.", "09:38", false),
            new Message("Ok, mình sẽ chuẩn bị ảnh màn hình để đưa vào báo cáo.", "09:50", true),
            new Message(conversation.encrypted() ? "Nhóm đang hiển thị tùy chọn mã hóa. Đây là dữ liệu mô phỏng, chưa mã hóa thật."
                    : "Minh An\nTốt quá! Vậy chiều nay gặp nhau nhé.", "10:24", false)
        );
        return List.of(
            new Message("Chào bạn! Chiều nay mình có họp nhóm về tiến độ đồ án, bạn sắp xếp được chứ?", "09:30", false),
            new Message("Chào bạn! Mình sắp xếp được nhé. Mấy giờ mình họp vậy?", "09:35", true),
            new Message("Mình dự kiến họp lúc 14:00 trên phòng họp online. Bạn nhớ chuẩn bị phần báo cáo tiến độ nha.", "09:38", false),
            new Message("Ok, mình đã hoàn thành khoảng 80% phần giao diện rồi. Chiều nay mình sẽ trình bày chi tiết.", "09:50", true),
            new Message("Tốt quá! Vậy chiều nay gặp nhau nhé. Nếu có gì cần hỗ trợ thì báo mình nhé.", "10:24", false)
        );
    }
    private ChatData() {}
}
