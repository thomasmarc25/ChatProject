package com.chatsystem.admin.data;

import com.chatsystem.admin.model.ChatGroupRecord;
import com.chatsystem.admin.model.GroupMemberRecord;
import com.chatsystem.admin.model.LoginRecord;
import com.chatsystem.admin.model.SpamReportRecord;
import com.chatsystem.admin.model.User;
import com.chatsystem.admin.model.UserFriendRecord;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Deterministic in-memory mock repository providing 24 representative Vietnamese user accounts,
 * system-wide and per-user login history records, accepted friendship edges, chat groups with
 * group-level vs. system-level member roles, and spam reports aligned with {@code script/02_seed.sql}.
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

    private final List<User> users;
    private final List<LoginRecord> loginRecords;
    private final List<FriendshipEdge> friendshipEdges;
    private final List<GroupSeed> groupSeeds;
    private final List<GroupMemberSeed> groupMemberSeeds;
    private final List<SpamReportRecord> spamReports;
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
                new LoginRecord(4L, 26L, "deleteduser01", "Người Dùng Đã Xóa (Lưu lịch sử)",
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
                // Group 1: 2 active admins (1, 2), 2 active members (3, 5), 1 departed member (6)
                new GroupMemberSeed(1L, 1L, "ADMIN", "USER", LocalDateTime.of(2026, 1, 20, 9, 0, 0), null),
                new GroupMemberSeed(1L, 2L, "ADMIN", "USER", LocalDateTime.of(2026, 1, 20, 9, 5, 0), null),
                new GroupMemberSeed(1L, 3L, "MEMBER", "USER", LocalDateTime.of(2026, 1, 20, 9, 10, 0), null),
                new GroupMemberSeed(1L, 5L, "MEMBER", "USER", LocalDateTime.of(2026, 2, 3, 11, 0, 0), null),
                new GroupMemberSeed(1L, 6L, "MEMBER", "USER", LocalDateTime.of(2026, 2, 10, 8, 0, 0),
                        LocalDateTime.of(2026, 3, 20, 18, 0, 0)),
                // Group 2 (Encrypted): 1 active admin (1), 2 active members (2, 7)
                new GroupMemberSeed(2L, 1L, "ADMIN", "USER", LocalDateTime.of(2026, 2, 10, 14, 0, 0), null),
                new GroupMemberSeed(2L, 2L, "MEMBER", "USER", LocalDateTime.of(2026, 2, 10, 14, 2, 0), null),
                new GroupMemberSeed(2L, 7L, "MEMBER", "USER", LocalDateTime.of(2026, 2, 15, 10, 0, 0), null),
                // Group 3: 2 active admins (3, 10), 3 active members (1, 12, 14)
                new GroupMemberSeed(3L, 3L, "ADMIN", "USER", LocalDateTime.of(2026, 3, 5, 10, 0, 0), null),
                new GroupMemberSeed(3L, 10L, "ADMIN", "USER", LocalDateTime.of(2026, 3, 6, 14, 0, 0), null),
                new GroupMemberSeed(3L, 1L, "MEMBER", "USER", LocalDateTime.of(2026, 3, 6, 14, 5, 0), null),
                new GroupMemberSeed(3L, 12L, "MEMBER", "USER", LocalDateTime.of(2026, 3, 20, 11, 0, 0), null),
                new GroupMemberSeed(3L, 14L, "MEMBER", "USER", LocalDateTime.of(2026, 4, 3, 9, 0, 0), null),
                // Group 4: 1 active admin (5), 3 active members (9, 11, 15)
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

    public User addUser(
            String username,
            String fullName,
            String address,
            LocalDate dateOfBirth,
            String gender,
            String email
    ) {
        User created = new User(
                nextUserId++,
                username,
                fullName,
                address,
                dateOfBirth,
                gender,
                email,
                LocalDate.now(),
                false,
                false
        );
        users.add(created);
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
