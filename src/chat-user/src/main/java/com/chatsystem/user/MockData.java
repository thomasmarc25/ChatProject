package com.chatsystem.user;

/**
 * Deterministic UI-only fixtures aligned with {@code script/02_seed.sql}
 * and {@code MockUserRepository.java} for logged-in demo user {@code @nguyenvanan}.
 */
final class MockData {
    private MockData() {}

    static Object[][] friends() {
        return new Object[][]{
            {"@tranthibich", "Trần Thị Bích Ngọc", "Trực tuyến"},
            {"@lehoangnam", "Lê Hoàng Nam", "Ngoại tuyến"},
            {"@phamminhchau", "Phạm Minh Châu", "Ngoại tuyến"},
            {"@hoangquocviet", "Hoàng Quốc Việt", "Trực tuyến"},
            {"@vuthithao", "Vũ Thị Phương Thảo", "Ngoại tuyến"},
            {"@dangquanghuy", "Đặng Quang Huy", "Trực tuyến"}
        };
    }

    static Object[][] requests() {
        return new Object[][]{
            {"@ngothuyduong", "Ngô Thùy Dương", "Chào An, kết bạn để trao đổi đồ án Java nhé!", "10/03/2026"},
            {"@dogiabao", "Đỗ Gia Bảo", "Mình cùng ở CLB Thuật Toán & AI nè.", "20/03/2026"}
        };
    }

    static Object[][] people() {
        return new Object[][]{
            {"@tranthibich", "Trần Thị Bích Ngọc", "Bạn bè"},
            {"@lehoangnam", "Lê Hoàng Nam", "Bạn bè"},
            {"@ngothuyduong", "Ngô Thùy Dương", "Đang chờ"},
            {"@dogiabao", "Đỗ Gia Bảo", "Đang chờ"},
            {"@haquynhnhu", "Hà Quỳnh Như", "Chưa kết bạn"}
        };
    }

    static Object[][] messages() {
        return new Object[][]{
            {"10:20", "Trần Thị Bích Ngọc", "Chào An! Bạn đã xem yêu cầu Version 1 chưa?"},
            {"10:22", "Bạn", "Mình đang hoàn thiện giao diện và thiết kế CSDL."},
            {"10:24", "Trần Thị Bích Ngọc", "Nhớ chuẩn bị ảnh màn hình để đưa vào báo cáo nhé."},
            {"10:25", "Bạn", "Chiều nay cùng kiểm tra nhóm Cộng đồng Java Việt Nam nhé!"}
        };
    }
}
