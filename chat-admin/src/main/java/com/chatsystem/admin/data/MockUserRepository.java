package com.chatsystem.admin.data;

import com.chatsystem.admin.model.User;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * Deterministic in-memory mock repository providing 24 representative Vietnamese user accounts
 * for the UI-only ChatSystem Admin User Management screen.
 */
public class MockUserRepository {

    private final List<User> users;

    public MockUserRepository() {
        this.users = List.of(
                new User(1L, "nguyenvanan", "Nguyễn Văn An", "12 Nguyễn Huệ, Quận 1, TP.HCM",
                        LocalDate.of(2002, 3, 14), "Nam", "nguyenvanan@chatsystem.vn",
                        LocalDate.of(2026, 1, 10), true, false),
                new User(2L, "tranthibich", "Trần Thị Bích Ngọc", "45 Lê Lợi, Quận Hải Châu, Đà Nẵng",
                        LocalDate.of(2003, 7, 22), "Nữ", "bichngoc.tran@chatsystem.vn",
                        LocalDate.of(2026, 1, 15), true, false),
                new User(3L, "lehoangnam", "Lê Hoàng Nam", "88 Cầu Giấy, Quận Cầu Giấy, Hà Nội",
                        LocalDate.of(2001, 11, 5), "Nam", "hoangnam.le@chatsystem.vn",
                        LocalDate.of(2026, 1, 18), false, false),
                new User(4L, "phamminhchau", "Phạm Minh Châu", "210 Điện Biên Phủ, Quận Bình Thạnh, TP.HCM",
                        LocalDate.of(2003, 5, 30), "Nữ", "minhchau.pham@chatsystem.vn",
                        LocalDate.of(2026, 1, 24), false, true),
                new User(5L, "hoangquocviet", "Hoàng Quốc Việt", "19 Trần Phú, TP. Nha Trang, Khánh Hòa",
                        LocalDate.of(2000, 9, 12), "Nam", "quocviet.hoang@chatsystem.vn",
                        LocalDate.of(2026, 2, 2), true, false),
                new User(6L, "vuthithao", "Vũ Thị Phương Thảo", "63 Lạch Tray, Quận Ngô Quyền, Hải Phòng",
                        LocalDate.of(2004, 2, 18), "Nữ", "phuongthao.vu@chatsystem.vn",
                        LocalDate.of(2026, 2, 8), false, false),
                new User(7L, "dangquanghuy", "Đặng Quang Huy", "105 Nguyễn Văn Cừ, Quận 5, TP.HCM",
                        LocalDate.of(2002, 12, 1), "Nam", "quanghuy.dang@chatsystem.vn",
                        LocalDate.of(2026, 2, 14), true, false),
                new User(8L, "buithanhha", "Bùi Thanh Hà", "34 Kim Mã, Quận Ba Đình, Hà Nội",
                        LocalDate.of(2003, 4, 9), "Nữ", "thanhha.bui@chatsystem.vn",
                        LocalDate.of(2026, 2, 20), true, true),
                new User(9L, "dinhgiabao", "Đinh Gia Bảo", "77 Nguyễn Trãi, Quận Ninh Kiều, Cần Thơ",
                        LocalDate.of(2001, 8, 25), "Nam", "giabao.dinh@chatsystem.vn",
                        LocalDate.of(2026, 3, 1), false, false),
                new User(10L, "ngothuyduong", "Ngô Thùy Dương", "152 Võ Văn Ngân, TP. Thủ Đức, TP.HCM",
                        LocalDate.of(2004, 6, 17), "Nữ", "thuyduong.ngo@chatsystem.vn",
                        LocalDate.of(2026, 3, 6), true, false),
                new User(11L, "duongtuananh", "Dương Tuấn Anh", "28 Hùng Vương, TP. Huế, Thừa Thiên Huế",
                        LocalDate.of(2002, 1, 29), "Nam", "tuananh.duong@chatsystem.vn",
                        LocalDate.of(2026, 3, 12), false, false),
                new User(12L, "lykhanhlinh", "Lý Khánh Linh", "91 Nguyễn Thị Minh Khai, Quận 3, TP.HCM",
                        LocalDate.of(2003, 10, 11), "Nữ", "khanhlinh.ly@chatsystem.vn",
                        LocalDate.of(2026, 3, 19), true, false),
                new User(13L, "truongducthang", "Trương Đức Thắng", "56 Giải Phóng, Quận Hai Bà Trưng, Hà Nội",
                        LocalDate.of(2000, 4, 3), "Nam", "ducthang.truong@chatsystem.vn",
                        LocalDate.of(2026, 3, 25), false, true),
                new User(14L, "haquynhnhu", "Hà Quỳnh Như", "18 Lê Duẩn, TP. Vinh, Nghệ An",
                        LocalDate.of(2004, 8, 7), "Nữ", "quynhnhu.ha@chatsystem.vn",
                        LocalDate.of(2026, 4, 2), true, false),
                new User(15L, "phanminhkhoi", "Phan Minh Khôi", "134 Cộng Hòa, Quận Tân Bình, TP.HCM",
                        LocalDate.of(2002, 5, 19), "Nam", "minhkhoi.phan@chatsystem.vn",
                        LocalDate.of(2026, 4, 9), false, false),
                new User(16L, "vothuytrang", "Võ Thùy Trang", "40 Nguyễn Văn Linh, Quận Thanh Khê, Đà Nẵng",
                        LocalDate.of(2003, 9, 23), "Nữ", "thuytrang.vo@chatsystem.vn",
                        LocalDate.of(2026, 4, 15), true, false),
                new User(17L, "doanduckien", "Đoàn Trung Kiên", "72 Trần Hưng Đạo, TP. Nam Định, Nam Định",
                        LocalDate.of(2001, 2, 14), "Nam", "trungkien.doan@chatsystem.vn",
                        LocalDate.of(2026, 4, 21), false, false),
                new User(18L, "lamngocmai", "Lâm Ngọc Mai", "205 Cách Mạng Tháng 8, Quận 10, TP.HCM",
                        LocalDate.of(2004, 11, 28), "Nữ", "ngocmai.lam@chatsystem.vn",
                        LocalDate.of(2026, 5, 3), true, false),
                new User(19L, "trinhhoanglong", "Trịnh Hoàng Long", "89 Xuân Thủy, Quận Cầu Giấy, Hà Nội",
                        LocalDate.of(2002, 7, 6), "Nam", "hoanglong.trinh@chatsystem.vn",
                        LocalDate.of(2026, 5, 11), false, false),
                new User(20L, "maiphuongvy", "Mai Phương Vy", "15 Đại lộ Bình Dương, TP. Thủ Dầu Một, Bình Dương",
                        LocalDate.of(2003, 3, 16), "Nữ", "phuongvy.mai@chatsystem.vn",
                        LocalDate.of(2026, 5, 18), true, false),
                new User(21L, "caothevinh", "Cao Thế Vinh", "64 Quang Trung, Quận Gò Vấp, TP.HCM",
                        LocalDate.of(2001, 10, 20), "Nam", "thevinh.cao@chatsystem.vn",
                        LocalDate.of(2026, 5, 25), false, false),
                new User(22L, "chuthanhhuyen", "Chu Thanh Huyền", "112 Hoàng Quốc Việt, Quận Bắc Từ Liêm, Hà Nội",
                        LocalDate.of(2004, 1, 8), "Nữ", "thanhhuyen.chu@chatsystem.vn",
                        LocalDate.of(2026, 6, 1), true, false),
                new User(23L, "luonggiahan", "Lương Gia Hân", "29 Phạm Văn Đồng, TP. Thủ Đức, TP.HCM",
                        LocalDate.of(2003, 6, 12), "Nữ", "giahan.luong@chatsystem.vn",
                        LocalDate.of(2026, 6, 7), false, false),
                new User(24L, "taquocdat", "Tạ Quốc Đạt", "51 Lê Hồng Phong, TP. Vũng Tàu, Bà Rịa - Vũng Tàu",
                        LocalDate.of(2002, 9, 4), "Nam", "quocdat.ta@chatsystem.vn",
                        LocalDate.of(2026, 6, 14), false, false)
        );
    }

    public List<User> getUsers() {
        return Collections.unmodifiableList(users);
    }

    public int getTotalCount() {
        return users.size();
    }

    public int getActiveOnlineCount() {
        return (int) users.stream()
                .filter(u -> !u.isLocked() && u.isOnline())
                .count();
    }

    public int getOfflineCount() {
        return (int) users.stream()
                .filter(u -> !u.isLocked() && !u.isOnline())
                .count();
    }

    public int getLockedCount() {
        return (int) users.stream()
                .filter(User::isLocked)
                .count();
    }
}
