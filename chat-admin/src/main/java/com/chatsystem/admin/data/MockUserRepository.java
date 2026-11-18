package com.chatsystem.admin.data;

import com.chatsystem.admin.model.ChatGroupRecord;
import com.chatsystem.admin.model.FriendStatRecord;
import com.chatsystem.admin.model.GroupMemberRecord;
import com.chatsystem.admin.model.LoginRecord;
import com.chatsystem.admin.model.RegistrationRecord;
import com.chatsystem.admin.model.SpamReportRecord;
import com.chatsystem.admin.model.User;
import com.chatsystem.admin.model.UserActivityRecord;
import com.chatsystem.admin.model.UserFriendRecord;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Deterministic in-memory mock repository providing 24 representative Vietnamese user accounts
 * (plus historical soft-deleted user #26 and admin #25 where applicable), login history,
 * accepted friendship edges, chat groups, spam reports, registration timestamps,
 * app-open events, direct-message events, and group-message events aligned 1:1 with
 * {@code script/02_seed.sql} and {@code script/03_admin_queries.sql}.
 */
public class MockUserRepository {

    private record FriendshipEdge(long userLowId, long userHighId, LocalDate createdAt) {
    }

    private record GroupSeed(
            long id,
            String groupName,
            long creatorUserId,
            boolean encrypted,
            LocalDateTime createdAt
    ) {
    }

    private record GroupMemberSeed(
            long groupId,
            long userId,
            String groupRole,
            String systemRole,
            LocalDateTime joinedAt,
            LocalDateTime leftAt
    ) {
    }

    private record AppOpenSeed(long id, String launchId, long userId, LocalDateTime openedAt) {
    }

    private record DirectMessageSeed(long id, long senderId, long receiverId, LocalDateTime sentAt) {
    }

    private record GroupMessageSeed(long id, long groupId, long senderId, LocalDateTime sentAt) {
    }

    private final List<User> users;
    private final Map<Long, LocalDateTime> exactRegistrationTimestamps;
    private final List<LoginRecord> loginRecords;
    private final List<FriendshipEdge> friendshipEdges;
    private final List<GroupSeed> groupSeeds;
    private final List<GroupMemberSeed> groupMemberSeeds;
    private final List<SpamReportRecord> spamReports;
    private final List<AppOpenSeed> appOpenSeeds;
    private final List<DirectMessageSeed> directMessageSeeds;
    private final List<GroupMessageSeed> groupMessageSeeds;
    private final List<Runnable> dataChangeListeners = new ArrayList<>();
    private long nextUserId = 27L;

    public MockUserRepository() {
        this.users = new ArrayList<>(List.of(
                new User(1L, "nguyenvanan", "Nguyễn Văn An", "12 Nguyễn Huệ, Quận 1, TP.HCM",
                        LocalDate.of(2002, 3, 14), "Nam", "nguyenvanan@chatsystem.vn",
                        LocalDate.of(2026, 1, 1), true, false),
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
        ));

        // Exact Asia/Ho_Chi_Minh registration timestamps matching script/02_seed.sql
        this.exactRegistrationTimestamps = new HashMap<>();
        exactRegistrationTimestamps.put(1L, LocalDateTime.of(2026, 1, 1, 6, 30, 0));
        exactRegistrationTimestamps.put(2L, LocalDateTime.of(2026, 1, 15, 9, 0, 0));
        exactRegistrationTimestamps.put(3L, LocalDateTime.of(2026, 1, 18, 10, 15, 0));
        exactRegistrationTimestamps.put(4L, LocalDateTime.of(2026, 1, 24, 14, 20, 0));
        exactRegistrationTimestamps.put(5L, LocalDateTime.of(2026, 2, 2, 8, 30, 0));
        exactRegistrationTimestamps.put(6L, LocalDateTime.of(2026, 2, 8, 11, 0, 0));
        exactRegistrationTimestamps.put(7L, LocalDateTime.of(2026, 2, 14, 16, 45, 0));
        exactRegistrationTimestamps.put(8L, LocalDateTime.of(2026, 2, 20, 19, 10, 0));
        exactRegistrationTimestamps.put(9L, LocalDateTime.of(2026, 3, 1, 7, 50, 0));
        exactRegistrationTimestamps.put(10L, LocalDateTime.of(2026, 3, 6, 13, 15, 0));
        exactRegistrationTimestamps.put(11L, LocalDateTime.of(2026, 3, 12, 15, 40, 0));
        exactRegistrationTimestamps.put(12L, LocalDateTime.of(2026, 3, 19, 20, 5, 0));
        exactRegistrationTimestamps.put(13L, LocalDateTime.of(2026, 3, 25, 10, 0, 0));
        exactRegistrationTimestamps.put(14L, LocalDateTime.of(2026, 4, 2, 9, 25, 0));
        exactRegistrationTimestamps.put(15L, LocalDateTime.of(2026, 4, 9, 11, 30, 0));
        exactRegistrationTimestamps.put(16L, LocalDateTime.of(2026, 4, 15, 14, 50, 0));
        exactRegistrationTimestamps.put(17L, LocalDateTime.of(2026, 4, 21, 17, 10, 0));
        exactRegistrationTimestamps.put(18L, LocalDateTime.of(2026, 5, 3, 8, 45, 0));
        exactRegistrationTimestamps.put(19L, LocalDateTime.of(2026, 5, 11, 12, 0, 0));
        exactRegistrationTimestamps.put(20L, LocalDateTime.of(2026, 5, 18, 15, 20, 0));
        exactRegistrationTimestamps.put(21L, LocalDateTime.of(2026, 5, 25, 18, 30, 0));
        exactRegistrationTimestamps.put(22L, LocalDateTime.of(2026, 6, 1, 9, 10, 0));
        exactRegistrationTimestamps.put(23L, LocalDateTime.of(2026, 6, 7, 10, 40, 0));
        exactRegistrationTimestamps.put(24L, LocalDateTime.of(2026, 6, 14, 16, 0, 0));

        // Login history records aligned with script/02_seed.sql (IDs 1..13) + extended sessions (14..18)
        this.loginRecords = List.of(
                new LoginRecord(18L, 22L, "chuthanhhuyen", "Chu Thanh Huyền",
                        LocalDateTime.of(2026, 6, 2, 10, 0, 0), null, "10.0.0.32",
                        "Phiên xác thực đang mở"),
                new LoginRecord(17L, 20L, "maiphuongvy", "Mai Phương Vy",
                        LocalDateTime.of(2026, 5, 19, 17, 0, 0), LocalDateTime.of(2026, 5, 19, 18, 30, 0), "10.0.0.30",
                        "Phiên tiêu chuẩn"),
                new LoginRecord(16L, 18L, "lamngocmai", "Lâm Ngọc Mai",
                        LocalDateTime.of(2026, 5, 5, 9, 40, 0), LocalDateTime.of(2026, 5, 5, 11, 20, 0), "10.0.0.28",
                        "Phiên tiêu chuẩn"),
                new LoginRecord(15L, 15L, "phanminhkhoi", "Phan Minh Khôi",
                        LocalDateTime.of(2026, 4, 12, 13, 30, 0), LocalDateTime.of(2026, 4, 12, 15, 10, 0), "10.0.0.25",
                        "Phiên tiêu chuẩn"),
                new LoginRecord(14L, 14L, "haquynhnhu", "Hà Quỳnh Như",
                        LocalDateTime.of(2026, 4, 4, 8, 15, 0), LocalDateTime.of(2026, 4, 4, 9, 45, 0), "10.0.0.24",
                        "Phiên tiêu chuẩn"),
                new LoginRecord(13L, 1L, "nguyenvanan", "Nguyễn Văn An",
                        LocalDateTime.of(2026, 3, 25, 20, 15, 3), null, "10.0.0.11",
                        "Phiên xác thực đang mở"),
                new LoginRecord(12L, 10L, "ngothuyduong", "Ngô Thùy Dương",
                        LocalDateTime.of(2026, 3, 22, 16, 45, 2), LocalDateTime.of(2026, 3, 22, 18, 0, 0), "10.0.0.20",
                        "Phiên tiêu chuẩn"),
                new LoginRecord(11L, 5L, "hoangquocviet", "Hoàng Quốc Việt",
                        LocalDateTime.of(2026, 3, 15, 11, 20, 5), LocalDateTime.of(2026, 3, 15, 13, 0, 0), "10.0.0.15",
                        "Phiên tiêu chuẩn"),
                new LoginRecord(10L, 1L, "nguyenvanan", "Nguyễn Văn An",
                        LocalDateTime.of(2026, 3, 12, 18, 30, 2), LocalDateTime.of(2026, 3, 12, 20, 0, 0), "10.0.0.11",
                        "Phiên tiêu chuẩn"),
                new LoginRecord(9L, 3L, "lehoangnam", "Lê Hoàng Nam",
                        LocalDateTime.of(2026, 3, 8, 10, 0, 4), LocalDateTime.of(2026, 3, 8, 12, 0, 0), "10.0.0.13",
                        "Phiên tiêu chuẩn"),
                new LoginRecord(8L, 2L, "tranthibich", "Trần Thị Bích Ngọc",
                        LocalDateTime.of(2026, 3, 6, 9, 10, 3), LocalDateTime.of(2026, 3, 6, 11, 0, 0), "10.0.0.12",
                        "Phiên tiêu chuẩn"),
                new LoginRecord(7L, 1L, "nguyenvanan", "Nguyễn Văn An",
                        LocalDateTime.of(2026, 3, 5, 11, 15, 0), LocalDateTime.of(2026, 3, 5, 12, 30, 0), "10.0.0.11",
                        "Đăng nhập lần 3 trong cùng 1 lần mở app (launch #104)"),
                new LoginRecord(6L, 1L, "nguyenvanan", "Nguyễn Văn An",
                        LocalDateTime.of(2026, 3, 5, 9, 30, 0), LocalDateTime.of(2026, 3, 5, 10, 45, 0), "10.0.0.11",
                        "Đăng nhập lần 2 trong cùng 1 lần mở app (launch #104)"),
                new LoginRecord(5L, 1L, "nguyenvanan", "Nguyễn Văn An",
                        LocalDateTime.of(2026, 3, 5, 8, 0, 6), LocalDateTime.of(2026, 3, 5, 9, 15, 0), "10.0.0.11",
                        "Đăng nhập lần 1 trong cùng 1 lần mở app (launch #104)"),
                new LoginRecord(4L, 26L, "deleteduser01", "Đặng Văn Cựu (Đã xóa)",
                        LocalDateTime.of(2026, 2, 12, 14, 0, 5), LocalDateTime.of(2026, 2, 12, 15, 10, 0), "10.0.0.26",
                        "Tài khoản đã xóa mềm (giữ nguyên lịch sử)"),
                new LoginRecord(3L, 2L, "tranthibich", "Trần Thị Bích Ngọc",
                        LocalDateTime.of(2026, 1, 16, 8, 30, 5), LocalDateTime.of(2026, 1, 16, 10, 0, 0), "10.0.0.12",
                        "Phiên tiêu chuẩn"),
                new LoginRecord(2L, 1L, "nguyenvanan", "Nguyễn Văn An",
                        LocalDateTime.of(2026, 1, 1, 6, 45, 2), LocalDateTime.of(2026, 1, 1, 7, 30, 0), "10.0.0.11",
                        "Ranh giới năm theo múi giờ Asia/Ho_Chi_Minh"),
                new LoginRecord(1L, 25L, "admin", "Quản Trị Hệ Thống",
                        LocalDateTime.of(2025, 12, 31, 23, 10, 2), LocalDateTime.of(2025, 12, 31, 23, 55, 0), "192.168.1.10",
                        "Tài khoản Quản trị hệ thống (ADMIN)")
        );

        // Canonical accepted friendships matching script/02_seed.sql (active non-deleted users)
        this.friendshipEdges = List.of(
                new FriendshipEdge(1L, 2L, LocalDate.of(2026, 1, 16)),
                new FriendshipEdge(1L, 3L, LocalDate.of(2026, 1, 19)),
                new FriendshipEdge(1L, 4L, LocalDate.of(2026, 1, 25)),
                new FriendshipEdge(2L, 3L, LocalDate.of(2026, 1, 20)),
                new FriendshipEdge(2L, 5L, LocalDate.of(2026, 2, 3)),
                new FriendshipEdge(3L, 5L, LocalDate.of(2026, 2, 4)),
                new FriendshipEdge(3L, 6L, LocalDate.of(2026, 2, 9)),
                new FriendshipEdge(5L, 7L, LocalDate.of(2026, 2, 15)),
                new FriendshipEdge(10L, 12L, LocalDate.of(2026, 3, 20)),
                new FriendshipEdge(14L, 15L, LocalDate.of(2026, 4, 10))
        );

        // Chat groups matching script/02_seed.sql
        this.groupSeeds = List.of(
                new GroupSeed(1L, "Cộng đồng Java Việt Nam", 1L, false, LocalDateTime.of(2026, 1, 20, 9, 0, 0)),
                new GroupSeed(2L, "Nhóm Đồ Án Hệ Thống Chat", 1L, true, LocalDateTime.of(2026, 2, 10, 14, 0, 0)),
                new GroupSeed(3L, "CLB Thuật Toán & AI", 3L, false, LocalDateTime.of(2026, 3, 5, 10, 0, 0)),
                new GroupSeed(4L, "Nhóm Học Tập KHTN", 5L, false, LocalDateTime.of(2026, 4, 12, 8, 30, 0))
        );

        // Group memberships matching script/02_seed.sql (with groupRole separate from systemRole)
        this.groupMemberSeeds = List.of(
                new GroupMemberSeed(1L, 1L, "ADMIN", "USER", LocalDateTime.of(2026, 1, 20, 9, 0, 0), null),
                new GroupMemberSeed(1L, 2L, "ADMIN", "USER", LocalDateTime.of(2026, 1, 20, 9, 5, 0), null),
                new GroupMemberSeed(1L, 3L, "MEMBER", "USER", LocalDateTime.of(2026, 1, 20, 9, 10, 0), null),
                new GroupMemberSeed(1L, 5L, "MEMBER", "USER", LocalDateTime.of(2026, 2, 3, 11, 0, 0), null),
                new GroupMemberSeed(1L, 6L, "MEMBER", "USER", LocalDateTime.of(2026, 2, 10, 8, 0, 0),
                        LocalDateTime.of(2026, 3, 20, 18, 0, 0)),
                new GroupMemberSeed(2L, 1L, "ADMIN", "USER", LocalDateTime.of(2026, 2, 10, 14, 0, 0), null),
                new GroupMemberSeed(2L, 2L, "MEMBER", "USER", LocalDateTime.of(2026, 2, 10, 14, 2, 0), null),
                new GroupMemberSeed(2L, 7L, "MEMBER", "USER", LocalDateTime.of(2026, 2, 15, 10, 0, 0), null),
                new GroupMemberSeed(3L, 3L, "ADMIN", "USER", LocalDateTime.of(2026, 3, 5, 10, 0, 0), null),
                new GroupMemberSeed(3L, 10L, "ADMIN", "USER", LocalDateTime.of(2026, 3, 6, 14, 0, 0), null),
                new GroupMemberSeed(3L, 1L, "MEMBER", "USER", LocalDateTime.of(2026, 3, 6, 14, 5, 0), null),
                new GroupMemberSeed(3L, 12L, "MEMBER", "USER", LocalDateTime.of(2026, 3, 20, 11, 0, 0), null),
                new GroupMemberSeed(3L, 14L, "MEMBER", "USER", LocalDateTime.of(2026, 4, 3, 9, 0, 0), null),
                new GroupMemberSeed(4L, 5L, "ADMIN", "USER", LocalDateTime.of(2026, 4, 12, 8, 30, 0), null),
                new GroupMemberSeed(4L, 9L, "MEMBER", "USER", LocalDateTime.of(2026, 4, 12, 8, 35, 0), null),
                new GroupMemberSeed(4L, 11L, "MEMBER", "USER", LocalDateTime.of(2026, 4, 12, 8, 40, 0), null),
                new GroupMemberSeed(4L, 15L, "MEMBER", "USER", LocalDateTime.of(2026, 4, 12, 8, 45, 0), null)
        );

        // Spam reports matching script/02_seed.sql (IDs 1..5)
        this.spamReports = new ArrayList<>(List.of(
                new SpamReportRecord(
                        1L,
                        1L, "nguyenvanan", "Nguyễn Văn An",
                        4L, "phamminhchau", "Phạm Minh Châu", true,
                        7L, LocalDateTime.of(2026, 3, 9, 22, 0, 0),
                        "Phát tán đường dẫn lừa đảo trúng thưởng",
                        "Nhận ngay khuyến mãi trúng thưởng 50 triệu tại link lạ!",
                        "RESOLVED",
                        LocalDateTime.of(2026, 3, 10, 8, 0, 0),
                        "admin",
                        LocalDateTime.of(2026, 3, 10, 11, 0, 0)
                ),
                new SpamReportRecord(
                        2L,
                        2L, "tranthibich", "Trần Thị Bích Ngọc",
                        8L, "buithanhha", "Bùi Thanh Hà", true,
                        8L, LocalDateTime.of(2026, 2, 21, 20, 0, 0),
                        "Gửi tin nhắn rác mời gọi đầu tư tài chính trái phép",
                        "Cơ hội đầu tư lợi nhuận 300% mỗi tuần, tham gia ngay!",
                        "RESOLVED",
                        LocalDateTime.of(2026, 3, 15, 9, 30, 0),
                        "admin",
                        LocalDateTime.of(2026, 3, 15, 15, 0, 0)
                ),
                new SpamReportRecord(
                        3L,
                        6L, "vuthithao", "Vũ Thị Phương Thảo",
                        13L, "truongducthang", "Trương Đức Thắng", true,
                        9L, LocalDateTime.of(2026, 3, 27, 11, 0, 0),
                        "Spam quảng cáo bán hàng liên tục",
                        "Tin nhắn quảng cáo bán hàng tự động gửi hàng loạt.",
                        "RESOLVED",
                        LocalDateTime.of(2026, 3, 28, 14, 0, 0),
                        "admin",
                        LocalDateTime.of(2026, 4, 1, 9, 0, 0)
                ),
                new SpamReportRecord(
                        4L,
                        10L, "ngothuyduong", "Ngô Thùy Dương",
                        17L, "doanduckien", "Đoàn Trung Kiên", false,
                        10L, LocalDateTime.of(2026, 4, 24, 19, 30, 0),
                        "Gửi liên kết lạ nghi ngờ mã độc",
                        "Click vào đường dẫn nhận quà tặng miễn phí ngay hôm nay!",
                        "PENDING",
                        LocalDateTime.of(2026, 4, 25, 8, 15, 0),
                        null,
                        null
                ),
                new SpamReportRecord(
                        5L,
                        3L, "lehoangnam", "Lê Hoàng Nam",
                        19L, "trinhhoanglong", "Trịnh Hoàng Long", false,
                        11L, LocalDateTime.of(2026, 5, 14, 8, 20, 0),
                        "Tin nhắn rác tuyển cộng tác viên không rõ nguồn gốc",
                        "Tuyển cộng tác viên việc nhẹ lương cao không cần kinh nghiệm.",
                        "PENDING",
                        LocalDateTime.of(2026, 5, 15, 10, 0, 0),
                        null,
                        null
                )
        ));

        // Exact 26 app_open_events from script/02_seed.sql (interpreted in Asia/Ho_Chi_Minh)
        this.appOpenSeeds = List.of(
                // Dec 2025
                new AppOpenSeed(1L, "11111111-1111-4111-8111-111111110001", 25L, LocalDateTime.of(2025, 12, 31, 23, 10, 0)),
                // Jan 2026 (2025-12-31 23:45:00Z = 2026-01-01 06:45:00+07)
                new AppOpenSeed(2L, "11111111-1111-4111-8111-111111110101", 1L, LocalDateTime.of(2026, 1, 1, 6, 45, 0)),
                new AppOpenSeed(3L, "11111111-1111-4111-8111-111111110102", 2L, LocalDateTime.of(2026, 1, 16, 8, 30, 0)),
                new AppOpenSeed(4L, "11111111-1111-4111-8111-111111110103", 3L, LocalDateTime.of(2026, 1, 20, 19, 0, 0)),
                // Feb 2026 (includes soft-deleted user 26)
                new AppOpenSeed(5L, "11111111-1111-4111-8111-111111110201", 1L, LocalDateTime.of(2026, 2, 3, 9, 0, 0)),
                new AppOpenSeed(6L, "11111111-1111-4111-8111-111111110202", 5L, LocalDateTime.of(2026, 2, 5, 10, 0, 0)),
                new AppOpenSeed(7L, "11111111-1111-4111-8111-111111110203", 26L, LocalDateTime.of(2026, 2, 12, 14, 0, 0)),
                // Mar 2026 (8 events across 5 distinct users: 1, 2, 3, 5, 10)
                new AppOpenSeed(8L, "11111111-1111-4111-8111-111111110301", 1L, LocalDateTime.of(2026, 3, 5, 8, 0, 5)),
                new AppOpenSeed(9L, "11111111-1111-4111-8111-111111110302", 1L, LocalDateTime.of(2026, 3, 12, 18, 30, 0)),
                new AppOpenSeed(10L, "11111111-1111-4111-8111-111111110303", 1L, LocalDateTime.of(2026, 3, 25, 20, 15, 0)),
                new AppOpenSeed(11L, "11111111-1111-4111-8111-111111110304", 2L, LocalDateTime.of(2026, 3, 6, 9, 10, 0)),
                new AppOpenSeed(12L, "11111111-1111-4111-8111-111111110305", 2L, LocalDateTime.of(2026, 3, 18, 21, 0, 0)),
                new AppOpenSeed(13L, "11111111-1111-4111-8111-111111110306", 3L, LocalDateTime.of(2026, 3, 8, 10, 0, 0)),
                new AppOpenSeed(14L, "11111111-1111-4111-8111-111111110307", 5L, LocalDateTime.of(2026, 3, 15, 11, 20, 0)),
                new AppOpenSeed(15L, "11111111-1111-4111-8111-111111110308", 10L, LocalDateTime.of(2026, 3, 22, 16, 45, 0)),
                // Apr 2026
                new AppOpenSeed(16L, "11111111-1111-4111-8111-111111110401", 14L, LocalDateTime.of(2026, 4, 4, 8, 15, 0)),
                new AppOpenSeed(17L, "11111111-1111-4111-8111-111111110402", 15L, LocalDateTime.of(2026, 4, 12, 13, 30, 0)),
                // May 2026
                new AppOpenSeed(18L, "11111111-1111-4111-8111-111111110501", 18L, LocalDateTime.of(2026, 5, 5, 9, 40, 0)),
                new AppOpenSeed(19L, "11111111-1111-4111-8111-111111110502", 20L, LocalDateTime.of(2026, 5, 19, 17, 0, 0)),
                // Jun 2026
                new AppOpenSeed(20L, "11111111-1111-4111-8111-111111110601", 22L, LocalDateTime.of(2026, 6, 2, 10, 0, 0)),
                // Jul 2026
                new AppOpenSeed(21L, "11111111-1111-4111-8111-111111110701", 1L, LocalDateTime.of(2026, 7, 10, 9, 0, 0)),
                // Aug 2026: 0 events
                // Sep 2026
                new AppOpenSeed(22L, "11111111-1111-4111-8111-111111110901", 2L, LocalDateTime.of(2026, 9, 5, 14, 0, 0)),
                // Oct 2026
                new AppOpenSeed(23L, "11111111-1111-4111-8111-111111111001", 3L, LocalDateTime.of(2026, 10, 11, 15, 30, 0)),
                // Nov 2026
                new AppOpenSeed(24L, "11111111-1111-4111-8111-111111111101", 1L, LocalDateTime.of(2026, 11, 1, 8, 0, 0)),
                new AppOpenSeed(25L, "11111111-1111-4111-8111-111111111102", 2L, LocalDateTime.of(2026, 11, 3, 19, 20, 0)),
                // Dec 2026
                new AppOpenSeed(26L, "11111111-1111-4111-8111-111111111201", 1L, LocalDateTime.of(2026, 12, 15, 20, 0, 0))
        );

        // Exact 11 direct_messages from script/02_seed.sql (hidden messages remain counted per Rule 7)
        this.directMessageSeeds = List.of(
                new DirectMessageSeed(1L, 1L, 2L, LocalDateTime.of(2026, 3, 5, 8, 10, 0)),
                new DirectMessageSeed(2L, 2L, 1L, LocalDateTime.of(2026, 3, 5, 8, 12, 0)),
                new DirectMessageSeed(3L, 1L, 2L, LocalDateTime.of(2026, 3, 5, 8, 15, 0)),
                new DirectMessageSeed(4L, 1L, 2L, LocalDateTime.of(2026, 3, 12, 18, 40, 0)),
                new DirectMessageSeed(5L, 1L, 3L, LocalDateTime.of(2026, 3, 12, 19, 0, 0)),
                new DirectMessageSeed(6L, 26L, 1L, LocalDateTime.of(2026, 2, 12, 14, 15, 0)),
                new DirectMessageSeed(7L, 4L, 1L, LocalDateTime.of(2026, 3, 9, 22, 0, 0)),
                new DirectMessageSeed(8L, 8L, 2L, LocalDateTime.of(2026, 2, 21, 20, 0, 0)),
                new DirectMessageSeed(9L, 13L, 6L, LocalDateTime.of(2026, 3, 27, 11, 0, 0)),
                new DirectMessageSeed(10L, 17L, 10L, LocalDateTime.of(2026, 4, 24, 19, 30, 0)),
                new DirectMessageSeed(11L, 19L, 3L, LocalDateTime.of(2026, 5, 14, 8, 20, 0))
        );

        // Exact 8 group_messages from script/02_seed.sql
        this.groupMessageSeeds = List.of(
                new GroupMessageSeed(1L, 1L, 1L, LocalDateTime.of(2026, 3, 5, 8, 20, 0)),
                new GroupMessageSeed(2L, 1L, 2L, LocalDateTime.of(2026, 3, 5, 8, 25, 0)),
                new GroupMessageSeed(3L, 1L, 1L, LocalDateTime.of(2026, 3, 12, 18, 45, 0)),
                new GroupMessageSeed(4L, 1L, 1L, LocalDateTime.of(2026, 3, 25, 20, 20, 0)),
                new GroupMessageSeed(5L, 1L, 3L, LocalDateTime.of(2026, 3, 25, 20, 30, 0)),
                new GroupMessageSeed(6L, 2L, 1L, LocalDateTime.of(2026, 3, 5, 8, 30, 0)),
                new GroupMessageSeed(7L, 2L, 1L, LocalDateTime.of(2026, 3, 12, 18, 50, 0)),
                new GroupMessageSeed(8L, 3L, 10L, LocalDateTime.of(2026, 3, 22, 17, 0, 0))
        );
    }

    public void addDataChangeListener(Runnable listener) {
        if (listener != null) {
            dataChangeListeners.add(listener);
        }
    }

    private void notifyDataChanged() {
        for (Runnable listener : dataChangeListeners) {
            listener.run();
        }
    }

    public List<User> getUsers() {
        return Collections.unmodifiableList(users);
    }

    public Optional<User> findUserById(long id) {
        return users.stream().filter(u -> u.getId() == id).findFirst();
    }

    public LocalDateTime getExactCreatedAtForUser(User u) {
        return exactRegistrationTimestamps.getOrDefault(
                u.getId(),
                u.getCreatedAt().atTime(9, 0, 0)
        );
    }

    public User addUser(
            String username,
            String fullName,
            String address,
            LocalDate dateOfBirth,
            String gender,
            String email
    ) {
        long newId = nextUserId++;
        LocalDate today = LocalDate.now();
        User created = new User(
                newId,
                username,
                fullName,
                address,
                dateOfBirth,
                gender,
                email,
                today,
                false,
                false
        );
        users.add(created);
        exactRegistrationTimestamps.put(newId, LocalDateTime.now().withNano(0));
        notifyDataChanged();
        return created;
    }

    public void updateUser(
            long userId,
            String username,
            String fullName,
            String address,
            LocalDate dateOfBirth,
            String gender,
            String email
    ) {
        for (int i = 0; i < users.size(); i++) {
            User existing = users.get(i);
            if (existing.getId() == userId) {
                users.set(i, existing.withUpdatedProfile(
                        username, fullName, address, dateOfBirth, gender, email
                ));
                notifyDataChanged();
                return;
            }
        }
    }

    public void toggleLockUser(long userId) {
        boolean newLockedState = false;
        boolean found = false;
        for (int i = 0; i < users.size(); i++) {
            User existing = users.get(i);
            if (existing.getId() == userId) {
                newLockedState = !existing.isLocked();
                users.set(i, existing.withLocked(newLockedState));
                found = true;
                break;
            }
        }
        if (found) {
            syncSpamReportLockState(userId, newLockedState, false);
            notifyDataChanged();
        }
    }

    public void toggleLockReportedUserFromSpamReport(long reportId) {
        for (int i = 0; i < spamReports.size(); i++) {
            SpamReportRecord report = spamReports.get(i);
            if (report.getId() == reportId) {
                long targetUserId = report.getReportedUserId();
                Optional<User> userOpt = findUserById(targetUserId);
                if (userOpt.isPresent()) {
                    boolean newLockState = !userOpt.get().isLocked();
                    for (int uIdx = 0; uIdx < users.size(); uIdx++) {
                        if (users.get(uIdx).getId() == targetUserId) {
                            users.set(uIdx, users.get(uIdx).withLocked(newLockState));
                            break;
                        }
                    }
                    syncSpamReportLockState(targetUserId, newLockState, newLockState);
                    notifyDataChanged();
                }
                return;
            }
        }
    }

    private void syncSpamReportLockState(long reportedUserId, boolean locked, boolean resolvePendingIfLocked) {
        for (int i = 0; i < spamReports.size(); i++) {
            SpamReportRecord r = spamReports.get(i);
            if (r.getReportedUserId() == reportedUserId) {
                String newStatus = r.getStatus();
                String resolvedBy = r.getResolvedByUsername().equals("—") ? null : r.getResolvedByUsername();
                LocalDateTime resolvedTime = r.getResolvedAt();
                if (resolvePendingIfLocked && locked && r.isPending()) {
                    newStatus = "RESOLVED";
                    resolvedBy = "admin";
                    resolvedTime = LocalDateTime.of(2026, 6, 15, 9, 0, 0);
                }
                spamReports.set(i, r.withResolutionAndLockState(locked, newStatus, resolvedBy, resolvedTime));
            }
        }
    }

    public void deleteUser(long userId) {
        if (users.removeIf(u -> u.getId() == userId)) {
            notifyDataChanged();
        }
    }

    public List<LoginRecord> getLoginHistoryForUser(long userId) {
        return loginRecords.stream()
                .filter(r -> r.getUserId() == userId)
                .sorted(Comparator.comparing(LoginRecord::getLoginAt).reversed())
                .toList();
    }

    public List<LoginRecord> getAllLoginHistory() {
        return loginRecords.stream()
                .sorted(Comparator.comparing(LoginRecord::getLoginAt).reversed())
                .toList();
    }

    public List<UserFriendRecord> getDirectFriendsForUser(long userId) {
        List<UserFriendRecord> result = new ArrayList<>();
        for (FriendshipEdge edge : friendshipEdges) {
            long friendId = -1L;
            if (edge.userLowId() == userId) {
                friendId = edge.userHighId();
            } else if (edge.userHighId() == userId) {
                friendId = edge.userLowId();
            }
            if (friendId > 0) {
                findUserById(friendId).ifPresent(friend -> result.add(new UserFriendRecord(
                        friend.getId(),
                        friend.getUsername(),
                        friend.getFullName(),
                        friend.getEmail(),
                        edge.createdAt(),
                        friend.isLocked(),
                        friend.isOnline()
                )));
            }
        }
        result.sort(Comparator.comparing(UserFriendRecord::getFriendFullName));
        return result;
    }

    public List<ChatGroupRecord> getChatGroups() {
        List<ChatGroupRecord> result = new ArrayList<>();
        for (GroupSeed seed : groupSeeds) {
            User creator = findUserById(seed.creatorUserId()).orElse(null);
            String creatorUsername = creator != null ? creator.getUsername() : "unknown";
            String creatorFullName = creator != null ? creator.getFullName() : "Không xác định";

            int currentMembers = 0;
            int currentAdmins = 0;
            int departedMembers = 0;
            for (GroupMemberSeed ms : groupMemberSeeds) {
                if (ms.groupId() == seed.id()) {
                    if (ms.leftAt() == null) {
                        currentMembers++;
                        if ("ADMIN".equalsIgnoreCase(ms.groupRole())) {
                            currentAdmins++;
                        }
                    } else {
                        departedMembers++;
                    }
                }
            }

            result.add(new ChatGroupRecord(
                    seed.id(),
                    seed.groupName(),
                    seed.createdAt(),
                    seed.creatorUserId(),
                    creatorUsername,
                    creatorFullName,
                    seed.encrypted(),
                    currentMembers,
                    currentAdmins,
                    departedMembers
            ));
        }
        return result;
    }

    public List<GroupMemberRecord> getCurrentMembersForGroup(long groupId) {
        List<GroupMemberRecord> result = new ArrayList<>();
        for (GroupMemberSeed ms : groupMemberSeeds) {
            if (ms.groupId() == groupId && ms.leftAt() == null) {
                User u = findUserById(ms.userId()).orElse(null);
                String username = u != null ? u.getUsername() : "user" + ms.userId();
                String fullName = u != null ? u.getFullName() : "Người dùng #" + ms.userId();
                String email = u != null ? u.getEmail() : "";
                boolean locked = u != null && u.isLocked();
                boolean online = u != null && u.isOnline();

                result.add(new GroupMemberRecord(
                        ms.groupId(),
                        ms.userId(),
                        username,
                        fullName,
                        email,
                        ms.groupRole(),
                        ms.systemRole(),
                        locked,
                        online,
                        ms.joinedAt(),
                        null
                ));
            }
        }
        return result;
    }

    public List<GroupMemberRecord> getCurrentAdminsForGroup(long groupId) {
        return getCurrentMembersForGroup(groupId).stream()
                .filter(GroupMemberRecord::isGroupAdmin)
                .toList();
    }

    public List<SpamReportRecord> getSpamReports() {
        List<SpamReportRecord> synced = new ArrayList<>();
        for (SpamReportRecord r : spamReports) {
            boolean currentlyLocked = findUserById(r.getReportedUserId())
                    .map(User::isLocked)
                    .orElse(r.isReportedUserLocked());
            synced.add(r.withResolutionAndLockState(
                    currentlyLocked,
                    r.getStatus(),
                    r.getResolvedByUsername().equals("—") ? null : r.getResolvedByUsername(),
                    r.getResolvedAt()
            ));
        }
        return synced;
    }

    /**
     * Returns all regular-user registration records (QUERY 6: {@code role = 'USER'}),
     * including active/locked users and historical soft-deleted user #26 ({@code deleteduser01}).
     */
    public List<RegistrationRecord> getRegistrationRecords() {
        List<RegistrationRecord> records = new ArrayList<>();
        for (User u : users) {
            records.add(new RegistrationRecord(
                    u.getId(),
                    u.getUsername(),
                    u.getFullName(),
                    u.getEmail(),
                    getExactCreatedAtForUser(u),
                    "USER",
                    u.isLocked(),
                    false
            ));
        }
        // Historical soft-deleted account #26 ('deleteduser01') retained per Rule 8 & QUERY 6
        records.add(new RegistrationRecord(
                26L,
                "deleteduser01",
                "Đặng Văn Cựu (Đã xóa)",
                "deleted01@chatsystem.vn",
                LocalDateTime.of(2026, 2, 10, 9, 0, 0),
                "USER",
                false,
                true
        ));
        records.sort(Comparator.comparing(RegistrationRecord::getCreatedAt)
                .thenComparingLong(RegistrationRecord::getUserId));
        return records;
    }

    /**
     * Returns a 12-element array (indices 0..11 for Months 1..12) of new user registrations
     * for the given year in {@code Asia/Ho_Chi_Minh}, aligned with {@code QUERY 7}.
     */
    public int[] getMonthlyRegistrationCounts(int year) {
        int[] counts = new int[12];
        for (RegistrationRecord r : getRegistrationRecords()) {
            LocalDateTime ts = r.getCreatedAt();
            if (ts != null && ts.getYear() == year) {
                int monthIndex = ts.getMonthValue() - 1;
                if (monthIndex >= 0 && monthIndex < 12) {
                    counts[monthIndex]++;
                }
            }
        }
        return counts;
    }

    /**
     * Computes friend statistics for all active (non-deleted) regular users,
     * aligned with {@code QUERY 8} in {@code script/03_admin_queries.sql}:
     * 1-hop direct friends and DISTINCT 1-or-2-hop friends excluding self and soft-deleted accounts.
     */
    public List<FriendStatRecord> getFriendStatistics() {
        Set<Long> activeUserIds = users.stream().map(User::getId).collect(Collectors.toSet());
        Map<Long, Set<Long>> adjacency = new HashMap<>();
        for (Long id : activeUserIds) {
            adjacency.put(id, new HashSet<>());
        }
        for (FriendshipEdge edge : friendshipEdges) {
            if (activeUserIds.contains(edge.userLowId()) && activeUserIds.contains(edge.userHighId())) {
                adjacency.get(edge.userLowId()).add(edge.userHighId());
                adjacency.get(edge.userHighId()).add(edge.userLowId());
            }
        }

        List<FriendStatRecord> stats = new ArrayList<>();
        for (User u : users) {
            long uid = u.getId();
            Set<Long> oneHop = adjacency.getOrDefault(uid, Collections.emptySet());
            Set<Long> oneOrTwoHop = new LinkedHashSet<>(oneHop);
            for (Long friendId : oneHop) {
                Set<Long> secondHopNeighbors = adjacency.getOrDefault(friendId, Collections.emptySet());
                for (Long fofId : secondHopNeighbors) {
                    if (fofId != uid) {
                        oneOrTwoHop.add(fofId);
                    }
                }
            }

            String reachableSummary = oneOrTwoHop.stream()
                    .sorted()
                    .map(id -> findUserById(id).map(usr -> "@" + usr.getUsername()).orElse("#" + id))
                    .collect(Collectors.joining(", "));
            if (reachableSummary.isEmpty()) {
                reachableSummary = "Chưa có kết nối bạn bè";
            }

            stats.add(new FriendStatRecord(
                    uid,
                    u.getUsername(),
                    u.getFullName(),
                    getExactCreatedAtForUser(u),
                    u.isLocked(),
                    oneHop.size(),
                    oneOrTwoHop.size(),
                    reachableSummary
            ));
        }
        return stats;
    }

    /**
     * Computes per-user activity metrics within the inclusive date interval {@code [fromDate, toDate]}
     * (evaluated as {@code [fromDate 00:00:00, toDate + 1 day 00:00:00)} in {@code Asia/Ho_Chi_Minh}),
     * aligned with {@code QUERY 9A} & {@code QUERY 9B} in {@code script/03_admin_queries.sql}.
     * Aggregates each activity table separately before joining to users, and returns users with
     * at least one activity event in the interval.
     */
    public List<UserActivityRecord> getUserActivityRecords(LocalDate fromDate, LocalDate toDate) {
        Map<Long, Integer> appOpenCounts = new HashMap<>();
        for (AppOpenSeed aoe : appOpenSeeds) {
            LocalDate d = aoe.openedAt().toLocalDate();
            if ((fromDate == null || !d.isBefore(fromDate)) && (toDate == null || !d.isAfter(toDate))) {
                appOpenCounts.merge(aoe.userId(), 1, Integer::sum);
            }
        }

        Map<Long, Set<Long>> directRecipientsByUser = new HashMap<>();
        for (DirectMessageSeed dm : directMessageSeeds) {
            LocalDate d = dm.sentAt().toLocalDate();
            if ((fromDate == null || !d.isBefore(fromDate)) && (toDate == null || !d.isAfter(toDate))) {
                directRecipientsByUser.computeIfAbsent(dm.senderId(), k -> new HashSet<>()).add(dm.receiverId());
            }
        }

        Map<Long, Set<Long>> groupsMessagedByUser = new HashMap<>();
        for (GroupMessageSeed gm : groupMessageSeeds) {
            LocalDate d = gm.sentAt().toLocalDate();
            if ((fromDate == null || !d.isBefore(fromDate)) && (toDate == null || !d.isAfter(toDate))) {
                groupsMessagedByUser.computeIfAbsent(gm.senderId(), k -> new HashSet<>()).add(gm.groupId());
            }
        }

        List<UserActivityRecord> result = new ArrayList<>();
        for (RegistrationRecord reg : getRegistrationRecords()) {
            long uid = reg.getUserId();
            int opens = appOpenCounts.getOrDefault(uid, 0);
            int dmRecipients = directRecipientsByUser.getOrDefault(uid, Collections.emptySet()).size();
            int gmGroups = groupsMessagedByUser.getOrDefault(uid, Collections.emptySet()).size();

            if (opens > 0 || dmRecipients > 0 || gmGroups > 0) {
                result.add(new UserActivityRecord(
                        uid,
                        reg.getUsername(),
                        reg.getFullName(),
                        reg.getCreatedAt(),
                        reg.isLocked(),
                        reg.isSoftDeleted(),
                        opens,
                        dmRecipients,
                        gmGroups
                ));
            }
        }
        return result;
    }

    /**
     * Returns a 12-element array (indices 0..11 for Months 1..12) of DISTINCT users who opened
     * the application ({@code COUNT(DISTINCT aoe.user_id)}) in each month of {@code year},
     * aligned with {@code QUERY 10} in {@code script/03_admin_queries.sql}.
     */
    public int[] getMonthlyDistinctAppOpeningUsers(int year) {
        List<Set<Long>> distinctUsersPerMonth = new ArrayList<>(12);
        for (int i = 0; i < 12; i++) {
            distinctUsersPerMonth.add(new HashSet<>());
        }
        for (AppOpenSeed aoe : appOpenSeeds) {
            LocalDateTime ts = aoe.openedAt();
            if (ts != null && ts.getYear() == year) {
                int monthIdx = ts.getMonthValue() - 1;
                if (monthIdx >= 0 && monthIdx < 12) {
                    distinctUsersPerMonth.get(monthIdx).add(aoe.userId());
                }
            }
        }
        int[] counts = new int[12];
        for (int i = 0; i < 12; i++) {
            counts[i] = distinctUsersPerMonth.get(i).size();
        }
        return counts;
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
