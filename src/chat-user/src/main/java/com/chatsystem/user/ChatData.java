package com.chatsystem.user;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

    static final String DUPLICATE_TEXT_SAMPLE = "Đã xác nhận lịch họp!";

    /** Typed group membership fixture aligned with {@code group_members.role} (ADMIN / MEMBER). */
    record GroupMember(String username, String fullName, String groupRole) {
        boolean isGroupAdmin() {
            return "ADMIN".equalsIgnoreCase(groupRole);
        }

        String displayLabel() {
            return fullName + " (@" + username + ") · " + (isGroupAdmin() ? "Admin nhóm" : "Thành viên");
        }
    }

    record Conversation(String username, String name, String initials, Color color, String preview,
                        String time, boolean online, boolean group, boolean encrypted,
                        List<GroupMember> groupMembers) {
        List<String> members() {
            return groupMembers.stream().map(GroupMember::displayLabel).toList();
        }

        boolean isGroupAdmin(String accountUsername) {
            if (accountUsername == null || accountUsername.isBlank()) return false;
            String normalized = accountUsername.strip().replace("@", "").toLowerCase(Locale.ROOT);
            for (GroupMember m : groupMembers) {
                if (m.username().equalsIgnoreCase(normalized) && m.isGroupAdmin()) {
                    return true;
                }
            }
            return false;
        }

        String roleDescriptionFor(String accountUsername) {
            if (accountUsername == null || accountUsername.isBlank()) return "Không phải thành viên nhóm";
            String normalized = accountUsername.strip().replace("@", "").toLowerCase(Locale.ROOT);
            for (GroupMember m : groupMembers) {
                if (m.username().equalsIgnoreCase(normalized)) {
                    return m.isGroupAdmin() ? "Quản trị viên nhóm (ADMIN)" : "Thành viên thường (MEMBER)";
                }
            }
            return "Không phải quản trị viên nhóm";
        }
    }

    /** Stable fixture message identified by deterministic {@code id}. */
    record Message(String id, String text, String time, boolean outgoing) {}

    /** Stable pointer to an exact message occurrence inside a specific conversation. */
    record MessageTarget(String conversationUsername, String messageId, int fixtureIndex) {}

    private static final Map<String, String> DIRECTORY_BY_USERNAME = new LinkedHashMap<>();
    static {
        DIRECTORY_BY_USERNAME.put("nguyenvanan", "Nguyễn Văn An");
        DIRECTORY_BY_USERNAME.put("tranthibich", "Trần Thị Bích Ngọc");
        DIRECTORY_BY_USERNAME.put("lehoangnam", "Lê Hoàng Nam");
        DIRECTORY_BY_USERNAME.put("phamminhchau", "Phạm Minh Châu");
        DIRECTORY_BY_USERNAME.put("hoangquocviet", "Hoàng Quốc Việt");
        DIRECTORY_BY_USERNAME.put("vuthithao", "Vũ Thị Phương Thảo");
        DIRECTORY_BY_USERNAME.put("dangquanghuy", "Đặng Quang Huy");
        DIRECTORY_BY_USERNAME.put("ngothuyduong", "Ngô Thùy Dương");
        DIRECTORY_BY_USERNAME.put("dogiabao", "Đỗ Gia Bảo");
        DIRECTORY_BY_USERNAME.put("haquynhnhu", "Hà Quỳnh Như");
        DIRECTORY_BY_USERNAME.put("admin", "Quản Trị Hệ Thống");
    }

    static String formatMemberEntry(String rawUsername, String roleLabel) {
        String clean = rawUsername == null ? "" : rawUsername.strip().replace("@", "").toLowerCase(Locale.ROOT);
        if (clean.isEmpty()) return "";
        String fullName = DIRECTORY_BY_USERNAME.getOrDefault(clean, clean);
        return fullName + " (@" + clean + ") · " + roleLabel;
    }

    static final List<Conversation> CONVERSATIONS = List.of(
        new Conversation(
            "tranthibich", "Trần Thị Bích Ngọc", "N", Ui.BLUE,
            "Chiều nay mình họp nhóm đồ án nhé!", "10:24", true, false, false, List.of()
        ),
        new Conversation(
            "group1", "Cộng đồng Java Việt Nam", "J", new Color(0x7C3AED),
            "Chào cả nhóm! Cùng ôn tập Java Swing nhé.", "09:58", true, true, false,
            List.of(
                new GroupMember("nguyenvanan", "Nguyễn Văn An", "ADMIN"),
                new GroupMember("tranthibich", "Trần Thị Bích Ngọc", "ADMIN"),
                new GroupMember("lehoangnam", "Lê Hoàng Nam", "MEMBER"),
                new GroupMember("hoangquocviet", "Hoàng Quốc Việt", "MEMBER")
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
                new GroupMember("lehoangnam", "Lê Hoàng Nam", "ADMIN"),
                new GroupMember("ngothuyduong", "Ngô Thùy Dương", "ADMIN"),
                new GroupMember("nguyenvanan", "Nguyễn Văn An", "MEMBER"),
                new GroupMember("dogiabao", "Đỗ Gia Bảo", "MEMBER"),
                new GroupMember("haquynhnhu", "Hà Quỳnh Như", "MEMBER")
            )
        ),
        new Conversation(
            "group2", "Nhóm Đồ Án Hệ Thống Chat", "Đ", new Color(0x0F766E),
            "Trao đổi trong nhóm mã hóa đầu cuối (E2EE)", "08:00", true, true, true,
            List.of(
                new GroupMember("nguyenvanan", "Nguyễn Văn An", "ADMIN"),
                new GroupMember("tranthibich", "Trần Thị Bích Ngọc", "MEMBER"),
                new GroupMember("dangquanghuy", "Đặng Quang Huy", "MEMBER")
            )
        )
    );

    static Conversation findConversationByUsername(String username) {
        if (username == null || username.isBlank()) return null;
        String clean = username.strip().replace("@", "");
        for (Conversation c : CONVERSATIONS) {
            if (c.username().equalsIgnoreCase(clean)) return c;
        }
        return null;
    }

    static List<Message> messages(Conversation conversation) {
        String prefix = conversation.username() + "-m";
        if (conversation.group()) {
            if (conversation.encrypted()) {
                return List.of(
                    new Message(prefix + "1", "Trần Thị Bích Ngọc\nChào nhóm Đồ Án Hệ Thống Chat! Mình đang kiểm tra giao diện E2EE.", "09:30", false),
                    new Message(prefix + "2", "Mình đã đồng bộ danh sách thành viên nhóm với phía Admin rồi nhé.", "09:35", true),
                    new Message(prefix + "3", "Đặng Quang Huy\nMình xác nhận trạng thái mã hóa nhóm hiển thị khớp với bảng chat_groups.", "09:42", false),
                    new Message(prefix + "4", "Nhóm đang hiển thị tùy chọn mã hóa đầu cuối (E2EE). Đây là dữ liệu mô phỏng phiên bản 1, chưa mã hóa thật.", "10:24", false)
                );
            }
            return List.of(
                new Message(prefix + "1", "Trần Thị Bích Ngọc\nChào cả nhóm! Chiều nay mình cùng kiểm tra giao diện Swing nhé.", "09:30", false),
                new Message(prefix + "2", "Mình đã cập nhật giao diện theo bảng màu Modern Blue của nhóm.", "09:35", true),
                new Message(prefix + "3", DUPLICATE_TEXT_SAMPLE, "09:36", true),
                new Message(prefix + "4", "Lê Hoàng Nam\nMình đang kiểm tra thiết kế CSDL PostgreSQL và danh sách thành viên.", "09:38", false),
                new Message(prefix + "5", "Ok, mình sẽ chuẩn bị ảnh màn hình để đưa vào báo cáo.", "09:50", true),
                new Message(prefix + "6", "Hoàng Quốc Việt\nTốt quá! Vậy chiều nay gặp nhau nhé.", "10:24", false),
                new Message(prefix + "7", DUPLICATE_TEXT_SAMPLE, "10:30", false)
            );
        }
        if (SAMPLE_SPAM_REPORTED_USERNAME.equals(conversation.username())) {
            return List.of(
                new Message(prefix + "1", SAMPLE_SPAM_SNAPSHOT, "22:00", false),
                new Message(prefix + "2", "Tin nhắn này có dấu hiệu lừa đảo, mình sẽ gửi báo cáo spam cho quản trị viên.", "22:05", true),
                new Message(prefix + "3", "Nhắc lại: cuộc họp báo cáo tiến độ lúc 14:00 ngày mai vẫn diễn ra bình thường nhé.", "14:00", true)
            );
        }
        return List.of(
            new Message(prefix + "1", "Chào bạn! Chiều nay mình có họp nhóm về tiến độ đồ án, bạn sắp xếp được chứ?", "09:30", false),
            new Message(prefix + "2", "Chào bạn! Mình sắp xếp được nhé. Mấy giờ mình họp vậy?", "09:35", true),
            new Message(prefix + "3", "Mình dự kiến họp lúc 14:00 trên phòng họp online. Bạn nhớ chuẩn bị phần báo cáo tiến độ nha.", "09:38", false),
            new Message(prefix + "4", "Ok, mình đã hoàn thành phần giao diện rồi. Chiều nay mình sẽ trình bày chi tiết.", "09:50", true),
            new Message(prefix + "5", "Tốt quá! Vậy chiều nay gặp nhau nhé. Nếu có gì cần hỗ trợ thì báo mình nhé.", "10:24", false)
        );
    }

    private ChatData() {}
}
