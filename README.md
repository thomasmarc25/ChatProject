# ChatSystem Admin — User Management (Java Swing UI Assignment)

Individual university Java Swing assignment (due **12 November 2026**): **Admin — User Management (`Quản lý người dùng`)** single-screen desktop interface.

---

## 1. Technology Stack & Requirements

- **Language / Runtime:** Java 21 LTS (`maven.compiler.release` = `21`)
- **Desktop Framework:** Java Swing
- **Look & Feel:** FlatLaf `3.7.2` (Light theme)
- **Build System:** Apache Maven 3.6+ / 3.9+
- **Encoding:** `UTF-8`
- **Window Dimensions:** `1280 x 800` px default; `1100 x 700` px minimum; resizable

---

## 2. Build & Run Instructions

From the `chat-admin/` directory:

```bash
cd chat-admin
mvn -DskipTests clean package
mvn exec:java
```

Or run the main class directly:

```bash
cd chat-admin
mvn compile exec:java -Dexec.mainClass="com.chatsystem.admin.AdminApp"
```

---

## 3. Repository & Submission Layout

- `chat-admin/` — Standalone Maven project containing `pom.xml`, `README.md`, and Java 21 Swing source code under `src/main/java/com/chatsystem/admin/`.
- `screenshots/` — Captured screenshots of the running Swing application (`01_UserManagement.png`, `02_AddUserDialog.png`) and capture instructions (`screenshots/README.md`).
- `Swing_Assignment/` — Packaged submission directory structure:
  ```text
  Swing_Assignment/
    source_code/
      chat-admin/
    screenshots/
      01_UserManagement.png
      02_AddUserDialog.png
      README.md
    AI-LOG.md
    evidence/
      chat_history/
  ```

---

## 4. Honest Disclosure of UI-Only Scope

This deliverable implements **one Java Swing screen (`Quản lý người dùng`)** with an optional UI-only `UserFormDialog` backed by 24 deterministic in-memory Vietnamese mock user records (`MockUserRepository`):

- **Working UI Features:**
  - Responsive Swing layout (`BorderLayout`, `GridBagLayout`, `GridLayout`, `BoxLayout`, `CardLayout`) tested at `1280x800` and `1100x700` without `null` layouts or `setBounds`.
  - Four summary stat cards (`Tổng người dùng: 24`, `Đang hoạt động: 11`, `Ngoại tuyến: 10`, `Tài khoản bị khóa: 3`) computed from `MockUserRepository` with independent `online` and `locked` boolean properties.
  - Non-editable `JTable` (`44px` row height) displaying 24 Vietnamese mock users with custom status badge pills (`Đang hoạt động`, `Ngoại tuyến`, `Đã khóa`) and row action pills (`Sửa`, `Thao tác`).
  - Clicking `+ Thêm người dùng` or `Sửa` opens `UserFormDialog` (with fields for `Tên đăng nhập`, `Họ và tên`, `Địa chỉ Email`, `Ngày sinh`, `Giới tính`, `Địa chỉ liên hệ`); `Hủy` closes the dialog.
  - Clicking `Thao tác` on any row opens a `JPopupMenu` showing `Sửa`, `Xóa`, `Khóa/Mở khóa`, `Đổi mật khẩu`, `Lịch sử đăng nhập`, and `Danh sách bạn bè`.
  - Clicking other sidebar navigation items shows a `"Tính năng đang phát triển"` placeholder view.

- **UI-Only Placeholders (Not Implemented as Business Logic):**
  - Search field, status filter dropdown, sort dropdown, `Làm mới` button, and pagination buttons (`« Trước`, `1`, `2`, `3`, `Sau »`) are static visual controls.
  - Row actions (`Xóa`, `Khóa/Mở khóa`, `Đổi mật khẩu`, `Lịch sử đăng nhập`, `Danh sách bạn bè`), `Lưu thông tin` in `UserFormDialog`, and `Đăng xuất` in the sidebar are visual-only placeholders that do not mutate or persist data.
  - No backend server, sockets, Spring Boot, or PostgreSQL/JDBC database connections are included.

## Phiên bản 1 (20%) — phần Người 1

Phần người dùng trên nhánh `Phúc`: `src/chat-user/` (Java 21+, Swing, FlatLaf). Chạy `mvn -f src/chat-user/pom.xml compile exec:java` hoặc `run-user.cmd` sau khi đặt JAVA_HOME tới JDK 21+.

- Bàn giao, thiết kế CSDL, đối chiếu yêu cầu và ảnh: [docs/NGUOI_1_DEADLINE_1.md](docs/NGUOI_1_DEADLINE_1.md).
- Script Người 1: [script/01_person1.sql](script/01_person1.sql), yêu cầu bảng `app_users(id BIGINT PRIMARY KEY)` của Người 2 trước khi ghép.
- Minh chứng commit local: [git/commits-phuc.png](git/commits-phuc.png); 17 ảnh UI tại `git/screenshots/`.
- Kiểm tra SQL: `node script/check_person1.mjs` sau khi cài dependency như tài liệu.

Phạm vi deadline 1 là thiết kế UI và CSDL; dữ liệu mẫu, chưa có backend. Tài liệu bài cá nhân Admin ở phía trên là nội dung cũ. Phần đóng gói nhóm, tỷ lệ đóng góp thực tế, thông tin hai thành viên và hợp đồng CSDL cuối cùng cần nhóm xác nhận; báo cáo bàn giao đánh dấu rõ các mục đó.

### Giao diện Modern Blue theo mẫu nhóm (17/11/2026)

Màn chat Java Swing đã được đổi sang sidebar tối có icon, danh sách cuộc trò chuyện có avatar và tin nhắn dạng bong bóng trái/phải. Dùng đúng bảng màu Modern Blue. Nút Tìm tin mở thanh tìm nội dung; menu Chi tiết chứa LLM, xóa tin/lịch sử, báo cáo spam và quản lý nhóm. Cài đặt mở các tab tài khoản; Nhóm chat lọc danh sách nhóm.

Chạy tại thư mục gốc bằng Maven đã cài: mvn -f src/chat-user/pom.xml compile exec:java.
Ảnh giao diện hiện tại: git/screenshots/01-chat.png. Đây là bản UI dùng dữ liệu mẫu của deadline 1.
