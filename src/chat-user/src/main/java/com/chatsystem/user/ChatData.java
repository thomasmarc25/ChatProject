package com.chatsystem.user;

import java.awt.Color;
import java.util.List;

/**
 * UI-only fixtures for the User workspace, aligned with the shared PostgreSQL seed data
 * ({@code script/02_seed.sql}) and the Admin application ({@code MockUserRepository.java}).
 */
final class ChatData {
    static final String CURRENT_USERNAME = "nguyenvanan";
    static final String CURRENT_FULL_NAME = "Nguyễn Văn An";
    static final String CURRENT_EMAIL = "nguyenvanan@chatsystem.vn";
    static final String CURRENT_ADDRESS = "12 Nguyễn Huệ, Quận 1, TP.HCM";
    static final String CURRENT_DOB = "14/03/2002";

    static final String SAMPLE_SPAM_REPORTED_USERNAME = "phamminhchau";
    static final String SAMPLE_SPAM_REASON = "Phát tán đường dẫn lừa đảo trúng thưởng";
    static final String SAMPLE_SPAM_SNAPSHOT = "Nhận ngay khuyến mãi trúng thưởng 50 triệu tại link lạ!";

    record Conversation(String username, String name, String initials, Color color, String preview,
                        String time, boolean online, boolean group, boolean encrypted, List<String> members) {}
    record Message(String text, String time, boolean outgoing) {}

    static final List<Conversation> CONVERSATIONS = List.of(
        new Conversation(
            "tranthibich", "Trần Thị Bích Ngọc", "N", Ui.BLUE,
            "Chiều nay mình họp nhóm đồ án nhé!", "10:24", true, false, false, List.of()
        ),
        new Conversation(
            "group1", "Cộng đồng Java Việt Nam", "J", new Color(0x7C3AED),
            "Chào cả nhóm! Cùng ôn tập Java Swing nhé.", "09:58", true, true, false,
            List.of(
                "Nguyễn Văn An (@nguyenvanan) · Admin nhóm",
                "Trần Thị Bích Ngọc (@tranthibich) · Admin nhóm",
                "Lê Hoàng Nam (@lehoangnam) · Thành viên",
                "Hoàng Quốc Việt (@hoangquocviet) · Thành viên"
            )
        ),
        new Conversation(
            "lehoangnam", "Lê Hoàng Nam", "N", new Color(0xF97316),
            "Mình đã xem lại câu truy vấn SQL rồi.", "09:12", false, false, false, List.of()
        ),
        new Conversation(
            "phamminhchau", "Phạm Minh Châu", "C", new Color(0xDC2626),
            SAMPLE_SPAM_SNAPSHOT, "08:45", false, false, false, List.of()
        ),
        new Conversation(
            "group3", "CLB Thuật Toán & AI", "A", new Color(0x3B82F6),
            "Tuần này nhóm thảo luận đồ thị bạn bè 2 bậc.", "08:10", false, true, false,
            List.of(
                "Lê Hoàng Nam (@lehoangnam) · Admin nhóm",
                "Ngô Thùy Dương (@ngothuyduong) · Admin nhóm",
                "Nguyễn Văn An (@nguyenvanan) · Thành viên",
                "Đỗ Gia Bảo (@dogiabao) · Thành viên",
                "Hà Quỳnh Như (@haquynhnhu) · Thành viên"
            )
        ),
        new Conversation(
            "group2", "Nhóm Đồ Án Hệ Thống Chat", "Đ", new Color(0x0F766E),
            "Trao đổi trong nhóm mã hóa đầu cuối (E2EE)", "08:00", true, true, true,
            List.of(
                "Nguyễn Văn An (@nguyenvanan) · Admin nhóm",
                "Trần Thị Bích Ngọc (@tranthibich) · Thành viên",
                "Đặng Quang Huy (@dangquanghuy) · Thành viên"
            )
        )
    );

    static List<Message> messages(Conversation conversation) {
        if (conversation.group()) {
            if (conversation.encrypted()) {
                return List.of(
                    new Message("Trần Thị Bích Ngọc\nChào nhóm Đồ Án Hệ Thống Chat! Mình đang kiểm tra giao diện E2EE.", "09:30", false),
                    new Message("Mình đã đồng bộ danh sách thành viên nhóm với phía Admin rồi nhé.", "09:35", true),
                    new Message("Đặng Quang Huy\nMình xác nhận trạng thái mã hóa nhóm hiển thị khớp với bảng chat_groups.", "09:42", false),
                    new Message("Nhóm đang hiển thị tùy chọn mã hóa đầu cuối (E2EE). Đây là dữ liệu mô phỏng phiên bản 1, chưa mã hóa thật.", "10:24", false)
                );
            }
            return List.of(
                new Message("Trần Thị Bích Ngọc\nChào cả nhóm! Chiều nay mình cùng kiểm tra giao diện Swing nhé.", "09:30", false),
                new Message("Mình đã cập nhật giao diện theo bảng màu Modern Blue của nhóm.", "09:35", true),
                new Message("Lê Hoàng Nam\nMình đang kiểm tra thiết kế CSDL PostgreSQL và danh sách thành viên.", "09:38", false),
                new Message("Ok, mình sẽ chuẩn bị ảnh màn hình để đưa vào báo cáo.", "09:50", true),
                new Message("Hoàng Quốc Việt\nTốt quá! Vậy chiều nay gặp nhau nhé.", "10:24", false)
            );
        }
        if (SAMPLE_SPAM_REPORTED_USERNAME.equals(conversation.username())) {
            return List.of(
                new Message(SAMPLE_SPAM_SNAPSHOT, "22:00", false),
                new Message("Tin nhắn này có dấu hiệu lừa đảo, mình sẽ gửi báo cáo spam cho quản trị viên.", "22:05", true),
                new Message("Nhắc lại: cuộc họp báo cáo tiến độ lúc 14:00 ngày mai vẫn diễn ra bình thường nhé.", "14:00", true)
            );
        }
        return List.of(
            new Message("Chào bạn! Chiều nay mình có họp nhóm về tiến độ đồ án, bạn sắp xếp được chứ?", "09:30", false),
            new Message("Chào bạn! Mình sắp xếp được nhé. Mấy giờ mình họp vậy?", "09:35", true),
            new Message("Mình dự kiến họp lúc 14:00 trên phòng họp online. Bạn nhớ chuẩn bị phần báo cáo tiến độ nha.", "09:38", false),
            new Message("Ok, mình đã hoàn thành phần giao diện rồi. Chiều nay mình sẽ trình bày chi tiết.", "09:50", true),
            new Message("Tốt quá! Vậy chiều nay gặp nhau nhé. Nếu có gì cần hỗ trợ thì báo mình nhé.", "10:24", false)
        );
    }

    private ChatData() {}
}
