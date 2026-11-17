package com.chatsystem.user;

/** Deterministic fixtures, deliberately independent from a real account service. */
final class MockData {
    private MockData() {}
    static Object[][] friends() { return new Object[][]{
        {"@minhan", "Nguyễn Minh An", "Trực tuyến"}, {"@baotran", "Trần Bảo Trân", "Ngoại tuyến"},
        {"@hoangnam", "Lê Hoàng Nam", "Trực tuyến"}, {"@thuyduong", "Phạm Thùy Dương", "Ngoại tuyến"},
        {"@giabao", "Võ Gia Bảo", "Trực tuyến"}, {"@khanhlinh", "Đỗ Khánh Linh", "Ngoại tuyến"}}; }
    static Object[][] requests() { return new Object[][]{{"@anhthu","Nguyễn Anh Thư","Xin chào, mình học cùng lớp Java!","17/11/2026"},{"@quanghuy","Trần Quang Huy","Cùng trao đổi bài tập nhé.","16/11/2026"}}; }
    static Object[][] people() { return new Object[][]{{"@anhthu","Nguyễn Anh Thư","Chưa kết bạn"},{"@minhan","Nguyễn Minh An","Bạn bè"},{"@quanghuy","Trần Quang Huy","Đang chờ"},{"@lamson","Lê Lâm Sơn","Chưa kết bạn"}}; }
    static Object[][] messages() { return new Object[][]{{"10:20","Minh An","Chào Phúc! Bạn đã xem yêu cầu deadline 1 chưa?"},{"10:22","Bạn","Mình đang hoàn thiện giao diện và thiết kế CSDL."},{"10:24","Minh An","Nhớ chuẩn bị ảnh màn hình để đưa vào báo cáo nhé."},{"10:25","Bạn","Chiều nay cùng kiểm tra thiết kế nhóm chat nhé!"}}; }
}
